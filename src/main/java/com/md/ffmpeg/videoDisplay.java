package com.md.ffmpeg;

/**
 *  视频播放线程
 */
public class videoDisplay extends Thread{
    private stoG sg;
    private myVideoFrame mv;
    private videoPlayer player;
    private volatile boolean run,flag, pausePlay,stopPlay;
    public videoDisplay(stoG sg, videoPlayer jl){
        this.sg = sg;
        this.run = false;
        this.preTime = 0;
        this.nextTime = 0;
        this.player = jl;
        this.flag = false;
        this.pausePlay = false;
        this.stopPlay = false;
    }
    public synchronized  void setPausePlay(boolean f){
        pausePlay = f;
        notifyAll();
    }
    public synchronized void setFlag(boolean f){
            flag = f;
            notifyAll();
    }
    public synchronized  void setStopPlay(){
            stopPlay = true;
            notifyAll();
    }
    private long preTime,nextTime;//当前视频时间戳之前的一个音频的时间戳，那个音频的下一个音频时间戳
    public  synchronized void setRun(long preTime,long nextTime){
            this.preTime = preTime;
            this.nextTime = nextTime;
            if(Util.DEBUG3)
                System.out.println("设置当前音频帧"+preTime+"下一帧音频"+nextTime+"     ");
            run = true;
            notifyAll();
    }
    /**
     * 进行图像显示
     */
    private void displayImage(){
//        player.setIcon(mv.imgIcon);
        System.out.println("推送了一帧视频："+mv.f.timestamp);
        player.pushFrame(mv.f);
    }
    @Override
    public void run() {
        while (!stopPlay) {
            this.run = false;//尽量回到初态。
            this.preTime = 0;
            this.nextTime = 0;
            mv = sg.consume();
            displayImage();
            while (!flag) {
                synchronized (this) {
                    while ((pausePlay) && (!flag)) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized (this) {
                    while (!run) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            if(Util.DEBUG5)
                             System.out.println("视频线程被中断");
                        }
                        if (flag) {
                            break;
                        }
                    }
                    run = false;
                }
                if (!flag)
                    player.getAP().setSleepFlag(true);
                do {
                    synchronized (this) {
                        /**
                         * 视频帧不再当前音频帧和下一帧音频帧之间，所以视频需要赶上去
                         */
                        while ((mv.timeStamp < preTime) && (!flag)) {
                            if(Util.DEBUG5)
                                System.out.println("视频赶超中" + mv.timeStamp);
                            if (!flag)
                                mv = sg.consume();
                        }
                    }
                    /**
                     * 防止赶上pre帧时也不小心超过了next帧，这发生在两个音频帧之间无视频帧的时候
                     */
                    if ((mv.timeStamp > nextTime) && (!flag)) {
                        break;
                    }
                    long c1 = System.currentTimeMillis();
                    try {
                        double a = (double) (mv.timeStamp - preTime);
                        double b = a / 1000.0;
                        int c = (int) b;
                        double d = a - c * 1000;
                        int f = (int) (d * 1000);
                        if (Util.DEBUG3)
                            System.out.println("  curG  " + mv.timeStamp + "  preA  " + preTime + "  nextA  " + nextTime + "    sleep   " + c + "ms" + f + "ns");
                        try {
                            Thread.sleep(c, f);//
                        } catch (IllegalArgumentException e) {
                            if(Util.DEBUG5)
                            System.out.println("************************值负数******************");
                        }
                    } catch (InterruptedException e) {
                        if(Util.DEBUG5)
                        System.out.println("视频线程被中断");
                    }
                    displayImage();
                    mv.f = null;
                    preTime = mv.timeStamp;
                    if (!flag)
                        mv = sg.consume();
                } while ((mv.timeStamp <= nextTime) && (!flag));
                if (!flag)
                    player.getAP().setSleepFlag(false);
            }
            if(Util.DEBUG5)
            System.out.println("视频退出循环，等待重新启动");
            synchronized (this){
                if(stopPlay){
                    player.vpEnd();
                    if(Util.DEBUG5)
                    System.out.println("*********************************************视频线程结束");
                    return;
                }
                setFlag(false);
                if(Util.DEBUG5)
                System.out.println("///////////////////视频等待结束");
            }
        }
    }
}
