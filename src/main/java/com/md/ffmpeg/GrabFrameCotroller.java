package com.md.ffmpeg;

//抓帧控制器：控制取帧器的工作频率，避免抓取太多的帧
public class GrabFrameCotroller {
    private double minGrabFrameSpeed =90;//定义抓帧速度：每秒最多抓取的帧数
    private long grabCount=0;//已抓取的总数量
    private long startTime=0;//开始抓帧计数时的时刻（毫秒）

    public GrabFrameCotroller(double grabFrameSpeed)
    {
        this.minGrabFrameSpeed =grabFrameSpeed;
        startTime=System.currentTimeMillis();
    }

    //开始抓帧计数
    public void start()
    {
        startTime=System.currentTimeMillis();
        grabCount=0;
    }

    //判断当前能否抓取一帧
    public boolean canGrab()
    {
        if(grabCount<=0)
            return true;
        long now=System.currentTimeMillis();
        long time=now-startTime;
        double speed=(1.0*time)/(1.0*grabCount);
        return speed>= (1000.0/minGrabFrameSpeed);
    }

    //更新计数
    public void updateGrabCount()
    {
        grabCount++;
    }
}
