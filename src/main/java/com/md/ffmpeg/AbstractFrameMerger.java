package com.md.ffmpeg;

import com.md.api.IFrameMerger;
import com.md.api.IFrameTaker;
import com.md.util.ThreadUtil;
import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

//合帧器基类
public abstract  class AbstractFrameMerger implements IFrameMerger {

    private static final int DEFAULT_CAPACITY = 100;
    protected int bufferMaxCapacity = DEFAULT_CAPACITY;//帧缓冲区最大容量
    protected ConcurrentLinkedQueue<Frame> frameQue;
    private Runnable worker = null;
    private List<IFrameTaker> frameTakers = new ArrayList<IFrameTaker>();
    private boolean exit = false;
    protected int deleteCount=0;

    public AbstractFrameMerger() {
        frameTakers = Collections.synchronizedList(new ArrayList<IFrameTaker>());
        frameQue = new ConcurrentLinkedQueue<Frame>();
    }

    public AbstractFrameMerger(int capacity) {
        this();
        this.bufferMaxCapacity = capacity;
    }

    //合帧处理（子类实现）
    protected abstract void mergeFrameRepeatly(List<IFrameTaker> frameTakers);

    protected synchronized void addMergedFrameToQue(Frame mergedFrame)
    {
        if(null==mergedFrame)
            return;
        synchronized (frameQue) {
            while (frameQue.size() >= bufferMaxCapacity)
            {
                frameQue.poll();
                deleteCount++;
            }
            frameQue.add(mergedFrame);
//            System.out.println("------------合并处理一帧..timestamp=" + mergedFrame.timestamp);
        }
    }

    protected void sleep(long millSeconds) {
        try {
            Thread.sleep(millSeconds);
        } catch (Exception e) {
        }
    }

    protected boolean isStopped()
    {
        return exit;
    }

    @Override
    public void setFrameTakers(IFrameTaker... takers) {
        frameTakers = Collections.synchronizedList(new ArrayList<IFrameTaker>());
        for (IFrameTaker taker : takers)
            if (null != taker)
                frameTakers.add(taker);
        if (frameTakers.isEmpty())
            throw new RuntimeException("合帧器需要至少一个取帧器！");
    }

    private Runnable createWorker() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("合帧器启动了...");
                startAllFrameTakers();
                mergeFrameRepeatly(frameTakers);
                stopAllFrameTakers();
                System.out.println("合帧器停止了...");
            }
        };
    }

    private void startAllFrameTakers() {
        for (IFrameTaker taker : frameTakers)
            taker.start();
    }

    private void stopAllFrameTakers() {
        for (IFrameTaker taker : frameTakers)
            taker.stop();
    }




    @Override
    public void start() {
        if (frameTakers.isEmpty())
            throw new RuntimeException("合帧器需要至少一个取帧器！");
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
    public synchronized List<Frame> takeMergedFrames() {
        List<Frame> resultList = Collections.synchronizedList(new ArrayList<Frame>());
        synchronized (frameQue) {
            while (!frameQue.isEmpty()) {
                Frame frame = frameQue.poll();
//                System.out.println("take 1 frame...timestamp=" + frame.timestamp);
                resultList.add(frame);
            }
        }
        return resultList;
    }

    @Override
    public  synchronized Frame takeFirstFrame() {
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
    public synchronized void clearFrames() {
        synchronized (frameQue) {
            frameQue.clear();
        }
    }

}
