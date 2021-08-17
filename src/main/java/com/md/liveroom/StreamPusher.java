package com.md.liveroom;

import com.md.api.IFrameMerger;
import com.md.api.IFramePusher;
import com.md.api.IFrameTaker;
import com.md.ffmpeg.BasicFramePusherForStream;
import com.md.ffmpeg.TwoStreamFrameMergerForStream4;
import com.md.ffmpeg.VideoAudioFrameTaker;

//双录推流
public class StreamPusher {
    private String clientRtmpPath;
    private String managerRtmpPath;
    private String pushPath;

    private Runnable worker;
    private IFrameTaker clientTaker;
    private IFrameTaker managerTaker;
    private IFrameMerger merger;
    private IFramePusher pusher;

    public StreamPusher(String clientRtmpPath, String managerRtmpPath, String pushPath) {
        this.clientRtmpPath = clientRtmpPath;
        this.managerRtmpPath = managerRtmpPath;
        this.pushPath = pushPath;
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
            clientTaker.stop();
            managerTaker.stop();
            merger.stop();
            pusher.stop();
        }catch (Exception e)
        {
        }
    }


    private Runnable createWorker()
    {
        return new Runnable(){
            @Override
            public void run()  {
                clientTaker=new VideoAudioFrameTaker(clientRtmpPath,300);
                managerTaker=new VideoAudioFrameTaker(managerRtmpPath,300);

                merger=new TwoStreamFrameMergerForStream4(300);
                merger.setFrameTakers(clientTaker,managerTaker);

               pusher=new BasicFramePusherForStream(pushPath);
                pusher.setFrameMerger(merger);
                pusher.start();
            }
        };
    }
}
