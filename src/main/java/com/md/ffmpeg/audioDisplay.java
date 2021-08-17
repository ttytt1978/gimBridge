package com.md.ffmpeg;

import org.bytedeco.javacpp.avutil;

import javax.sound.sampled.SourceDataLine;

/**
 * 音频播放线程
 */
public class audioDisplay extends Thread{
    private stoA sa;
    private myAudioFrame curf,nextf;
    private SourceDataLine sourceDataLine;
    private int sampleFormat;
    private long curTimeStamp;
    private videoPlayer player;
    private int lineBufSize;
    private int lowLimit,highLimit;
    private volatile boolean stopPlay, pausePlay,flag,sleepFlag;
    public audioDisplay(stoA sa, videoPlayer player, SourceDataLine sourceDataLine){
        this.sa = sa;
        this.sourceDataLine = sourceDataLine;
        this.sampleFormat = player.getSampleFormat();
        this.curTimeStamp = 0;
        this.player = player;
        lineBufSize = sourceDataLine.getBufferSize();
        lowLimit = lineBufSize*7/10;
        highLimit = lineBufSize*9/10;
        this.stopPlay = false;
        this.pausePlay = false;
        this.flag = false;
        this.sleepFlag  = false;
    }
    public synchronized void setStopPlay(){
            stopPlay = true;
            notifyAll();
    }
    public synchronized void setPausePlay(boolean f){
            pausePlay = f;
            notifyAll();
    }
    public synchronized void setFlag(boolean f){
            flag = f;
            notifyAll();
    }
    /**
     * 当前播放的音频的时间戳
     */
    public long getCurTimeStamp(){
        synchronized (this){
            return curTimeStamp;
        }
    }
    public synchronized void setSleepFlag(boolean f){
            sleepFlag = f;
            notifyAll();
    }

    long c1 = 0,c2 = 0, nextTimeStamp;
    long factor = 0;
    @Override
    public void run() {
        while (!stopPlay) {
            this.sleepFlag = false;//尽量回到初态。
            curf = sa.consume();
            curTimeStamp = curf.timeStamp;//
            c1 = System.currentTimeMillis();
            while(!flag){//这个是设置新的时间戳时，需要回到的状态
            synchronized (this) {
                while (pausePlay) {
                    try {
                        sourceDataLine.flush();
                        wait();
                    } catch (InterruptedException e) {
                        if(Util.DEBUG5)
                            System.out.println("音频线程被中断");
                    }
                }
            }
            nextf = sa.consume();//获取下一帧
            nextTimeStamp = nextf.timeStamp;
            synchronized (this) {
                while (sleepFlag) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (flag) {
                        break;
                    }
                }
                sleepFlag = true;
                notifyAll();
            }
//            ----开始实际推送当前视频Frame和音频Frame
            player.getVP().setRun(curTimeStamp, nextTimeStamp);
            setFactor();
            c2 = System.currentTimeMillis();
            writeData();
            double a = (double) (nextTimeStamp - curTimeStamp + factor);
            double b = a / 1000.0;
            int c = (int) b;
            double d = a - c * 1000;
            int e = (int) (d * 1000);
            if ((c - (c2 - c1)) > 0)
                try {
                    Thread.sleep(c - (c2 - c1), e);
                } catch (InterruptedException g) {
                    if(Util.DEBUG5)
                        System.out.println("音频线程被中断");
                }
            else {
                if(Util.DEBUG5)
                    System.out.println("不延时，直接播放下一帧音频，本来需要延时" + (c - (c2 - c1)) + "ms");
            }
            c1 = System.currentTimeMillis();
            curf = nextf;
            curTimeStamp = nextTimeStamp;
        }
        if(Util.DEBUG5)
            System.out.println("音频退出循环，等待重新启动");
        synchronized (this){
            if(stopPlay){
                player.apEnd();
                if(Util.DEBUG5)
                    System.out.println("*********************************************音频线程结束");
                return;
            }
               setFlag(false);
               if(Util.DEBUG5)
                    System.out.println("///////////////////音频等待结束");
        }
    }
    }
    private void setFactor() {
        if(Util.DEBUG1)
            System.out.println("Line内部缓冲区可写入"+sourceDataLine.available()+"字节内部缓冲区总大小"+sourceDataLine.getBufferSize()+"字节");
        if(sourceDataLine.available()> (lineBufSize-100)) {
            if(Util.DEBUG5)
                System.out.println("音频卡了");
        }
        if(sourceDataLine.available()> highLimit){
            factor -= 1000;//这是1ms
            //System.out.println("+++++++++++++++++++++++++++++++++调整因子为"+factor);
        }else{
            factor = 0;
        }

    }

    //推送curf当前音频帧
    private void writeData() {
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_FLTP://平面型左右声道分开。
                sourceDataLine.write(curf.combine,0,curf.combine.length);
                //System.out.println(curf.combine.length);
                break;
            case avutil.AV_SAMPLE_FMT_S16://非平面型左右声道在一个buffer中。
                sourceDataLine.write(curf.tl,0,curf.tl.length);
                //System.out.println(curf.tl.length);
                break;
            case avutil.AV_SAMPLE_FMT_S16P://平面型左右声道分开
                sourceDataLine.write(curf.combine,0,curf.combine.length);
                break;
            case avutil.AV_SAMPLE_FMT_FLT://float非平面型
                sourceDataLine.write(curf.tl,0,curf.tl.length);
                break;
            default:
//                JOptionPane.showMessageDialog(null,"unsupport audio format","unsupport audio format",JOptionPane.ERROR_MESSAGE);
                break;
        }
        System.out.println("推送了一帧音频：-------"+curf.f.timestamp);
//        player.pushFrame(curf.f);
        curf.f = null;//释放Buffer

    }
}
