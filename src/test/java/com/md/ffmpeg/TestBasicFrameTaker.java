package com.md.ffmpeg;


import com.md.api.IFrameTaker;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestBasicFrameTaker {

    @Test
    public void test1()
    {
        IFrameTaker taker=new BasicFrameTaker("d:\\ffmpeg\\b5.mp4",10);
        Assert.assertTrue(taker.takeFrames().isEmpty());
        taker.start();
        sleep(3000);
        Assert.assertFalse(taker.takeFrames().isEmpty());
        sleep(3000);
        long firstTimeStamp=taker.takeFirstFrame().timestamp;
        long lastTimeStamp=taker.takeFirstFrame().timestamp;
        Assert.assertTrue(firstTimeStamp<lastTimeStamp);
        taker.stop();
        sleep(1000);
        taker.takeFrames();
        Assert.assertTrue(taker.takeFrames().isEmpty());
    }

    @Test
    public void test2()
    {
        IFrameTaker taker=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream13",10);
        Assert.assertTrue(taker.takeFrames().isEmpty());
        taker.start();
        sleep(3000);
        List<Frame> list=taker.takeFrames();
        Assert.assertFalse(list.size()>10);

        sleep(2000);
        taker.stop();
        sleep(3000);
        list=taker.takeFrames();
        for(int i=list.size()-1;i>=1;i--)
        {
            long nextTimeStamp=list.get(i).timestamp;
            long previousTimeStamp=list.get(i-1).timestamp;
            System.out.println(nextTimeStamp+">"+previousTimeStamp);
            Assert.assertTrue(nextTimeStamp>previousTimeStamp);
        }
        Assert.assertTrue(taker.takeFrames().isEmpty());
    }

    @Test
    public void testPush()throws Exception
    {
        IFrameTaker taker=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",10);
        Assert.assertTrue(taker.takeFrames().isEmpty());
        taker.start();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream15", 300, 200);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFormat("flv");
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(1);
        recorder.setFrameRate(20);
        recorder.start();
        for(int i=0;i<999999;i++)
        {
            Frame frame=taker.takeLastFrame();
            if(frame==null)
                continue;
            recorder.setTimestamp(frame.timestamp);
            recorder.record(frame);
        }
        taker.stop();
        recorder.close();
    }

    @Test
    public void test3()
    {
        IFrameTaker taker=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream13",10);
        taker.start();
        sleep(30000000);
//        sleep(2000);
        taker.stop();
//        sleep(3000);

    }

    @Test
    public void testQue()
    {
        ConcurrentLinkedQueue<Integer> que=new ConcurrentLinkedQueue<Integer>();
        que.add(3);
        que.add(5);
        que.add(9);
        while(!que.isEmpty())
            System.out.println(que.poll());
    }

    private void sleep(long millSeconds) {
        try {
            Thread.sleep(millSeconds);
        } catch (Exception e) {
        }
    }
}
