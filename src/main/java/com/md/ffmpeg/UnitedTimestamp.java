package com.md.ffmpeg;

import com.md.api.IFrameTaker;
import org.bytedeco.javacv.Frame;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnitedTimestamp implements IFrameTaker {
    private Map<Integer,Long> subChannelMap =new HashMap<Integer, Long>();
    private long startUnityTimestamp =-1;
    private long latestUnityTimestamp=-1;
    private long latestRealTimestamp=-1;
    private static final int DEFAULT_CAPACITY = 100;
    private int bufferMaxCapacity = DEFAULT_CAPACITY;//缓冲区最大容量
    private ConcurrentLinkedQueue<Frame> frameQue;

    public UnitedTimestamp()
    {
        frameQue = new ConcurrentLinkedQueue<Frame>();
    }

    public UnitedTimestamp(int capacity)
    {
       this();
       bufferMaxCapacity=capacity;
    }

    private boolean haveUnityChannel()
    {
        return startUnityTimestamp >=0;
    }

    private boolean isFirstFrameForChannel(int channelNumber)
    {
        Long value= subChannelMap.get(channelNumber);
        return null==value;
    }

    private void createUnityChannel(Frame frame,int channelNumber)
    {
        startUnityTimestamp =0;
        latestUnityTimestamp=frame.timestamp-startUnityTimestamp;
        latestRealTimestamp=System.currentTimeMillis()*1000L;
    }

    private void createSubChannelUsingFirstFrame(Frame frame,int channelNumber)
    {
        long now=System.currentTimeMillis()*1000L;
        long d=latestUnityTimestamp+(now-latestRealTimestamp);
        subChannelMap.put(channelNumber,d);
    }

    private UnityFrame genUnityFrame(Frame frame,int channelNumber)
    {
        long d=subChannelMap.get(channelNumber);
        frame.timestamp+=d;
        return new UnityFrame(frame,channelNumber);
    }

    private void addToFrameQue(UnityFrame frame)
    {
        synchronized (frameQue) {
            while (frameQue.size() >= bufferMaxCapacity) {
                frameQue.poll();
            }
            frameQue.add(frame);
        }
    }


    //更新给定通道中一帧的时间戳
    public synchronized  Frame addUnityFrame(Frame frame, int channelNumber)
    {
        if(isFirstFrameForChannel(channelNumber))
        {
            if(!haveUnityChannel())
            {
                createUnityChannel(frame,channelNumber);
            }
            createSubChannelUsingFirstFrame(frame,channelNumber);
        }
        addToFrameQue(genUnityFrame(frame,channelNumber));
        return frame;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
    @Override
    public Frame takeFirstFrame() {
        synchronized (frameQue) {
            if (!frameQue.isEmpty())
                return frameQue.poll();
        }
        return null;
    }

    @Override
    public synchronized Frame takeLastFrame() {
        Frame lastFrame = null;
        synchronized (frameQue) {
            while (!frameQue.isEmpty()) {
                lastFrame = frameQue.poll();
            }
        }
        return lastFrame;
    }

    @Override
    public List<Frame> takeFrames() {
        List<Frame> resultList = Collections.synchronizedList(new ArrayList<Frame>());
        synchronized (frameQue) {
            while (!frameQue.isEmpty()) {
                Frame frame = frameQue.poll();
                System.out.println("take 1 frame...timestamp=" + frame.timestamp);
                resultList.add(frame);
            }
        }
        return resultList;
    }


    @Override
    public synchronized boolean isDisconnected() {
        return false;
    }

    @Override
    public synchronized void clearFrames() {
        synchronized (frameQue) {
            frameQue.clear();
        }
    }
}
