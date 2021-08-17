package com.md.ffmpeg;

import com.md.api.IFrameTaker;
import com.md.util.ThreadUtil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FakedFrameTaker implements IFrameTaker {
    private static final int DEFAULT_CAPACITY = 100;
    private String streamPath;//流地址
    private int bufferMaxCapacity = DEFAULT_CAPACITY;//缓冲区最大容量
    private ConcurrentLinkedQueue<Frame> frameQue;
    private boolean exit = false;
    private FFmpegFrameGrabber grabber;
    private long orginStartTimeStamp = -1, newStartTimeStamp = -1;
    private long orginLastTimeStamp = -1, newLastTimeStamp = -1;
    private boolean isNewGrabber = true;
    private Runnable worker = null;
    private GrabFrameCotroller controller;
    private boolean disconnected=true;

    public FakedFrameTaker(String streamPath) {
        this.streamPath = streamPath;
        frameQue = new ConcurrentLinkedQueue<Frame>();
    }

    public FakedFrameTaker(String streamPath, int capacity) {
        this(streamPath);
        this.bufferMaxCapacity = capacity;
    }


    private void openGrabber() {
        try {

            System.out.println("------------新建一个faked grabber...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeGrabber() {
        try {
            System.out.println("------------关闭一个faked grabber...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized boolean canGrab()
    {
        double ratio=0.2;
        synchronized (frameQue)
        {
            int size=frameQue.size();
            if(size*1.0/bufferMaxCapacity>=ratio)
                return false;
            return true;
        }
    }

    private void grabFrameRepeatly() {

    }

    private Runnable createWorker() {
        return new Runnable() {
            @Override
            public void run() {
                openGrabber();
                grabFrameRepeatly();
                closeGrabber();
            }
        };
    }


    @Override
    public void start() {
        exit = false;
        if (null == worker) {
            worker = createWorker();
            ThreadUtil.startThread(worker);
        }
    }

    @Override
    public void stop() {
        exit = true;
    }

    @Override
    public synchronized Frame takeFirstFrame() {
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
    public synchronized List<Frame> takeFrames() {
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
        return disconnected;
    }

    @Override
    public synchronized void clearFrames() {
        synchronized (frameQue) {
            frameQue.clear();
        }
    }
}
