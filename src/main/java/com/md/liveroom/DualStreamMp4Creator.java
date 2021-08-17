package com.md.liveroom;

import com.md.api.IFrameMerger;
import com.md.api.IFramePusher;
import com.md.api.IFrameTaker;
import com.md.ffmpeg.BasicFramePusherForMp4;
import com.md.ffmpeg.CameraFrameTaker;
import com.md.ffmpeg.TwoStreamFrameMergerForStream4;

//双录推流进行录制
public class DualStreamMp4Creator {
    private String clientRtmpPath;
    private String managerRtmpPath;
    private String pushPath;

    private Runnable worker;
    private IFrameTaker clientTaker;
    private IFrameTaker managerTaker;
    private IFrameMerger merger;
    private IFramePusher pusher;

    public DualStreamMp4Creator(String clientRtmpPath, String managerRtmpPath, String mp4FilePath) {
        this.clientRtmpPath = clientRtmpPath;
        this.managerRtmpPath = managerRtmpPath;
        this.pushPath = mp4FilePath;
    }

    public void start()
    {
        if(worker!=null)
            return;
        worker=createWorker();
        new Thread(worker).start();
    }

    public void stop()
    {
        if(worker==null)
            return;
        try
        {
            Runnable lateWork=new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        Thread.sleep(10*1000);
                        clientTaker.stop();
                        managerTaker.stop();
                        merger.stop();
                        pusher.stop();
                    }catch (Exception e)
                    {
                    }
                }
            };
            new Thread(lateWork).start();
        }catch (Exception e)
        {
        }
    }


    private Runnable createWorker()
    {
        return new Runnable(){
            @Override
            public void run()  {
                clientTaker=new CameraFrameTaker(clientRtmpPath,400);
                managerTaker=new CameraFrameTaker(managerRtmpPath,400);

                merger=new TwoStreamFrameMergerForStream4(800);
                merger.setFrameTakers(clientTaker,managerTaker);

               pusher=new BasicFramePusherForMp4(pushPath);
                pusher.setFrameMerger(merger);
                pusher.start();
            }
        };
    }
}
