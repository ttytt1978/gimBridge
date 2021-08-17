package com.md.ffmpeg;

import com.md.api.IFrameMerger;
import com.md.api.IFrameTaker;
import org.bytedeco.javacv.Frame;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestTwoStreamFrameMergerForMp4 {

    private void sleep(long millSeconds) {
        try {
            Thread.sleep(millSeconds);
        } catch (Exception e) {
        }
    }

    @Test
    public void test1()
    {
        IFrameTaker taker1=new BasicFrameTaker("d:\\ffmpeg\\b6.mp4",10);
        IFrameTaker taker2=new BasicFrameTaker("d:\\ffmpeg\\b5.mp4",20);
        IFrameMerger merger=new TwoStreamFrameMergerForMp4(5);
        merger.setFrameTakers(taker1,taker2);
        Assert.assertTrue(merger.takeMergedFrames().isEmpty());
        merger.start();
        sleep(5000);
        List<Frame> list=merger.takeMergedFrames();
        Assert.assertFalse(list.isEmpty());
        for(int i=list.size()-1;i>=1;i--)
        {
            long nextTimeStamp=list.get(i).timestamp;
            long previousTimeStamp=list.get(i-1).timestamp;
            System.out.println(nextTimeStamp+">"+previousTimeStamp);
            Assert.assertTrue(nextTimeStamp>=previousTimeStamp);
        }
        merger.stop();
        sleep(3000);
        merger.takeMergedFrames();
        sleep(1000);
        Assert.assertTrue(merger.takeMergedFrames().isEmpty());
    }

}
