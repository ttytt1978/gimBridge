package com.md.ffmpeg;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;

/**
 * 视频播放器JLabel组件
 */
public class videoPlayer extends JLabel {
    private String videoFilePath;
    private FFmpegFrameGrabber fg;
    private long totalTime;
    public String totalString;
    private int sampleFormat,audioSampleRate;
    private AudioFormat af;
    private DataLine.Info dataLineInfo;
    private SourceDataLine sourceDataLine;
    private videoDisplay vp;
    private audioDisplay ap;
    private stoA sa;
    private stoG sg;
    private volatile boolean stopPlay,waitSet,startPlay;

    private FFmpegFrameRecorder recorder;

    public synchronized void pushFrame(Frame vf)
    {
        try {
            synchronized (recorder)
            {
                            recorder.record(vf);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public videoPlayer(String videoFilePath){
        this(videoFilePath,0,0);
    }

    public videoPlayer(String videoFilePath,String rtmpPath){
        this(videoFilePath,0,0);
        recorder=new FFmpegFrameRecorder(rtmpPath,800,600,1);
        try {
            recorder.setInterleaved(true);
            recorder.setVideoOption("crf","28");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 28
            recorder.setAudioOption("crf","0");
            // Highest quality
            recorder.setAudioQuality(0);
            // 192 Kbps
            recorder.setAudioBitrate(192000);
            recorder.setSampleRate(44100);
            recorder.setAudioChannels(1);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setFormat("flv"); // rtmp的类型
            recorder.setFrameRate(20);
            recorder.setVideoBitrate(3000000);
            recorder.setImageWidth(800);
            recorder.setImageHeight(600);
            recorder.setPixelFormat(0); // yuv420p
            System.out.println("recorder start");
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        initThread();
        stopPlay = false;
        waitSet = false;
        startPlay = false;
        initPlayerThread();

    }
    public videoPlayer(String videoFilePath,int w,int h){
        this.videoFilePath = videoFilePath;
        initGrabber(w,h);//初始化帧捕捉器
        getInfoFromVideo();
        initJL();
        initLine();//初始化音频数据线
        initStorage();//初始化帧仓库和音频消费者和视频消费者线程


    }
    private void initGrabber(int w,int h){
        fg = new FFmpegFrameGrabber(videoFilePath);
        if((w!=0) && (h!=0)){
            fg.setImageHeight(w);
            fg.setImageWidth(h);
        }
        try {
            fg.start();//启动后才能够获得视频的各项参数
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        grabCover();
    }
    private void grabCover(){
        /**
         * 捕捉第cnt帧图像作为开始播放前的封面
         */
        int cnt = 0;
        long cnt0t = 0;
        Frame f = null;
        while(cnt<60){
            do {
                try {
                    f = fg.grab();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }while(f.image==null);
            if(cnt == 0) cnt0t = fg.getTimestamp();
            cnt++;
        }
        setIcon(new ImageIcon((new Java2DFrameConverter()).getBufferedImage(f)));
        try {
            fg.setTimestamp(cnt0t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getInfoFromVideo(){
        totalTime = fg.getLengthInTime();
        totalString = Util.getTimeString(totalTime);
        sampleFormat = fg.getSampleFormat();
        audioSampleRate = fg.getSampleRate();
    }
    private void initJL(){
        setOpaque(true);//JLabel默认是透明的，所以设置背景色和是无效的，所以需要设置成不透明
        setPreferredSize(new Dimension(getVideoWidth(),getVideoHeight()));
        setSize(getVideoWidth(),getVideoHeight());
    }
    private void getAudioFormat(){
        switch(sampleFormat){
            case avutil.AV_SAMPLE_FMT_U8://无符号short 8bit
                break;
            case avutil.AV_SAMPLE_FMT_S16://有符号short 16bit
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_S32:
                break;
            case avutil.AV_SAMPLE_FMT_FLT:
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_DBL:
                break;
            case avutil.AV_SAMPLE_FMT_U8P:
                break;
            case avutil.AV_SAMPLE_FMT_S16P://有符号short 16bit,平面型
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_S32P://有符号short 32bit，平面型，但是32bit的话可能电脑声卡不支持，这种音乐也少见
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),32,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_FLTP://float 平面型 需转为16bit short
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_DBLP:
                break;
            case avutil.AV_SAMPLE_FMT_S64://有符号short 64bit 非平面型
                break;
            case avutil.AV_SAMPLE_FMT_S64P://有符号short 64bit平面型
                break;
            default:
                // you can inspect samples of other format audio file to process them.
                JOptionPane.showMessageDialog(null,"unsupport audio format from video","unsupport audio format from video",JOptionPane.ERROR_MESSAGE);
                break;
        }
    }
    private void initLine(){
        getAudioFormat();
        dataLineInfo = new DataLine.Info(SourceDataLine.class,
                af, AudioSystem.NOT_SPECIFIED);
        try {
            sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        try {
            sourceDataLine.open(af);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        sourceDataLine.start();
    }
    private void initStorage(){
        sa = new stoA();
        sg = new stoG();
    }
    private void initThread(){
        vp = new videoDisplay(sg,this);
        vp.setPriority(Thread.MAX_PRIORITY);
        ap = new audioDisplay(sa,this,sourceDataLine);
        ap.setPriority(Thread.MAX_PRIORITY);
    }
    private void startThread(){
        vp.start();
        ap.start();
    }
    public int getVideoWidth(){
        return fg.getImageWidth();
    }
    public int getVideoHeight(){
        return fg.getImageHeight();
    }
    /**
     * 得到当前播放的时间，单位微秒
     */
    public long getCurPlayTime(){
        return ap.getCurTimeStamp();
    }
    private boolean clearEnd;
    private void clearEnd(){
        synchronized (this){
            clearEnd = true;
            notifyAll();
        }
    }
    /**
     * 设置播放的时间，单位秒
     */
    private int newTime;
    public void setPlayTime(int sec){
        newTime = sec;
        clearEnd = false;
        setWaitSet(true);
        synchronized (this){
            while(!clearEnd){
                try {
                    wait();//等待捕获线程清空FIFO。
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(Util.DEBUG5)
        System.out.println("清空FIFO成功");
        ap.setFlag(true);//暂停音频线程
        vp.setFlag(true);
        ap.interrupt();
        vp.interrupt();
        setWaitSet(false);
    }
    private volatile float f = 1;
    public synchronized void setVol(float f){
        this.f = f;
        notifyAll();
    }
    public synchronized  float getVol(){
        return f;
    }
    /**
     *在播放前设置，设置视频长宽，如果不设置
     * 就是默认是视频原本的分辨率。
     */
    public void setVideoWH(int w,int h){
        try {
            fg.stop();//必须先关闭然后再设置
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        fg.setImageWidth(w);
        fg.setImageHeight(h);
        try {
            fg.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        setSize(fg.getImageWidth(),fg.getImageHeight());//重新设置JLabel
        grabCover();//重新捕获封面
    }
    /**
     * 视频总时长，单位微秒
     */
    public long getVideoTotalLength(){
        return totalTime;
    }
    public String getTotalString(){
        return totalString;
    }
    /**
     * 设置视频开始播放
     */
    public void setStartPlay(){
         startPlay = true;
          startThread();//开始后才启动三个线程。
          grab.setStart();
    }
    private void setWaitSet(boolean f){
        synchronized (this){
            if(f){
                waitSet = true;
            }else{
                waitSet = false;
            }
            notifyAll();
        }
    }
    /**
     * 设置停止播放
     */
    public void setStopPlay(){
        if(!isStart()){
            sourceDataLine.close();
            try {
                fg.stop();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            return;
        }
        vpEnd = false;
        apEnd = false;
        synchronized (this){
            stopPlay = true;
            vp.setStopPlay();
            vp.setFlag(true);
            ap.setStopPlay();
            ap.setFlag(true);
            vp.interrupt();
            ap.interrupt();
            notifyAll();
        }
        /**
         * 在这里等待线程结束的信息。
         */
        synchronized (this){
            while(!vpEnd){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (this){
            while(!apEnd){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(Util.DEBUG5)
        System.out.println("----------------音频和视频线程结束--------------------");
    }
    private boolean vpEnd,apEnd;
    public void vpEnd(){
        synchronized (this){
            vpEnd = true;
            notifyAll();
        }
    }
    public void apEnd(){
        synchronized (this){
            apEnd = true;
            notifyAll();
        }
    }
    /**
     * 设置暂停播放
     */
    public void setPausePlay() {
        synchronized (this){
            ap.setPausePlay(true);
            vp.setPausePlay(true);
        }
    }
    /**
     * 设置继续播放
     */
    public void setResumePlay() {
         synchronized (this){
             ap.setPausePlay(false);
             vp.setPausePlay(false);
         }
    }

    public boolean isStart() {
        return startPlay;
    }

    public int getSampleFormat() {
        return sampleFormat;
    }


    private class grabThread extends Thread{
        private volatile boolean run = false;
        @Override
        public void run() {
            synchronized (this){
                while(!run){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            startPlay();
        }
        public void setStart() {
            synchronized (this) {
                run = true;
                notifyAll();
            }
        }
    }
    private grabThread grab;
    private void initPlayerThread(){
        grab = new grabThread();
        grab.start();
    }
    private void startPlay(){
        Frame curFrame = null;
        int curIDv = 0,curIDa = 0;
         while(true){
             try {
                 curFrame = fg.grabFrame();
             } catch (FrameGrabber.Exception e) {
                 e.printStackTrace();
             }
             if(curFrame==null){ //没有捕捉到帧表示视频结束了
                 if(Util.DEBUG5)
                 System.out.println("帧捕捉结束");
                 setStopPlay();
                 break;
             }
             if(curFrame.image != null) {
                 if(Util.DEBUG4){
                     System.out.println("捕捉到视频帧,时间戳为"+fg.getTimestamp()+"微秒");
                 }
                 /**
                  * 缓冲区太大会占内存。
                  *太小会造成卡顿
                  *为了防止太小卡顿，生产延时只发生在满足最小值20的条件下设置上限50
                  */
                 while((sg.getCur()>50) && (sa.getCur()>20)&&(!stopPlay) ) {
                     try {
                         Thread.sleep(10);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
                 sg.produce(new myVideoFrame(this,curFrame, curIDv++,fg.getTimestamp()));
             }
             if(curFrame.samples!=null){
                 if(Util.DEBUG4){
                     System.out.println("捕捉到音频帧,时间戳为"+fg.getTimestamp()+"微秒");
                 }
                 while((sa.getCur()>50) && (sg.getCur()>20) && (!stopPlay)){
                     try {
                         Thread.sleep(10);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
                 sa.produce(new myAudioFrame(curFrame,this,curIDa++,fg.getTimestamp(), sampleFormat));
             }
             synchronized (this){
                 while(waitSet){
                     sg.clearFIFO();
                     sa.clearFIFO();
                     clearEnd();
                     while(waitSet)
                        try {
                            wait();//等待waitSet为false
                         } catch (InterruptedException e) {
                           e.printStackTrace();
                      }
                     try {
                         int i = (int)(((double)newTime)*fg.getFrameRate());
                         fg.setFrameNumber(i);
                         if(Util.DEBUG5)
                         System.out.println("设置新时间戳成功"+newTime+"秒，第"+i+"帧");
                         /**
                          * 下面开始捕获帧，丢弃所有的视频帧，直到捕获到音频帧。
                          */
                         do{
                             curFrame = fg.grabFrame();
                         }while(curFrame.samples == null);
                         sa.produce(new myAudioFrame(curFrame,this,curIDa++,fg.getTimestamp(), sampleFormat));
                     } catch (FrameGrabber.Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
             synchronized (this){
                 if(stopPlay){
                     break;
                 }
             }
         }
         synchronized (this){
             sourceDataLine.flush();
             sourceDataLine.close();
         }
        try {
            fg.stop();//stop() should have help you free the direct buffer.
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        if(Util.DEBUG5)
        System.out.println("结束帧捕获线程");
    }
    /**
     * 返回视频播放线程对象
     */
    public videoDisplay getVP() {
        return vp;
    }
    /**
     * 返回音频播放线程对象
     */
    public audioDisplay getAP() {
        return ap;
    }
    /**
     * 在视频播放前调用，设置视频开始播放的时间戳
     */
    public void setStartTime(int hour,int min,int sec){
        long timeStamp = (((long)hour)*3600+((long)min)*60+sec)*1000000;
        try {
            fg.setTimestamp(timeStamp);//这里似乎不需要先关闭fg。
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}
