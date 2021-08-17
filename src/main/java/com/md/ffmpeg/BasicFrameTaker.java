package com.md.ffmpeg;

import com.md.api.IFrameTaker;
import com.md.util.ThreadUtil;
import com.md.util.TimeUtil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BasicFrameTaker implements IFrameTaker {
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

    public BasicFrameTaker(String streamPath) {
        this.streamPath = streamPath;
        frameQue = new ConcurrentLinkedQueue<Frame>();
    }

    public BasicFrameTaker(String streamPath, int capacity) {
        this(streamPath);
        this.bufferMaxCapacity = capacity;
    }


    private void openGrabber() {
        try {
            disconnected=true;
            grabber = new FFmpegFrameGrabber(streamPath);
            int audioChannels=1;
            int sampleRate=48000;
            grabber.setAudioChannels(audioChannels);
            grabber.setSampleRate(sampleRate);
            grabber.setFrameRate(30);
//            grabber.setVideoCodecName("h264_amf");
//            grabber.setVideoCodecName("hevc_amf");
            grabber.start();
            grabber.flush();
            isNewGrabber = true;
            controller = new GrabFrameCotroller(190);
            controller.start();
            disconnected=false;
            System.out.println("------------新建一个grabber...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeGrabber() {
        try {
            grabber.stop();
            grabber.close();
            grabber.release();
            disconnected=true;
            System.out.println("------------关闭一个grabber...");
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
        Frame frame = null;
        TimeUtil totalTimer = new TimeUtil();
        totalTimer.start();
        int grabCount = 0;
        int deleteCount=0;
        Random random=new Random();

        while (!exit) {
            TimeUtil timer = new TimeUtil();
            timer.start();
            if (!canGrab())
                continue;
            try {
//                Thread.sleep(5);
//                if(random.nextInt(3)==0)
//                    frame = grabber.grabImage();
//                else
//                    frame=grabber.grabSamples();
                frame=grabber.grab();
            } catch (Exception e) {
                System.out.println("------------抓帧失败..." + e.getMessage());
                closeGrabber();
                openGrabber();
                continue;
            }
            if (frame == null) {
                System.out.println("------------抓取到一个null帧...");
                closeGrabber();
                openGrabber();
                continue;
            }
            if (isNewGrabber) {
                if (newStartTimeStamp < 0)//初次开始抓取
                {
                    newStartTimeStamp = 0;
                    newLastTimeStamp = 0;
                    orginStartTimeStamp = frame.timestamp;
                    orginLastTimeStamp = frame.timestamp;
                } else//新开的grabber接着以前的抓取
                {
                    orginStartTimeStamp = frame.timestamp;
                    orginLastTimeStamp = frame.timestamp;
                }
                isNewGrabber = false;
            }
            //上次已经有一次抓取帧
            long gap = frame.timestamp - orginLastTimeStamp;
//            if (gap < 0) {
//                System.out.println("------------抓取到的帧时间戳错误..." + frame.timestamp);
//                continue;
//            }
            orginLastTimeStamp = frame.timestamp;
            newLastTimeStamp += gap;
            frame.timestamp = newLastTimeStamp;

            controller.updateGrabCount();
            synchronized (frameQue) {
                while (frameQue.size() >= bufferMaxCapacity)
                {
                    frameQue.poll();
                    deleteCount++;
                }
                if(frame.image!=null&&frame.samples!=null)
                {
                    Frame copyFrame1 = frame.clone();;
                    copyFrame1.samples=null;
                    Frame copyFrame2 = frame.clone();;
                    copyFrame2.image=null;
                    frameQue.add(copyFrame1);
                    frameQue.add(copyFrame2);
                }else
                {
                    Frame copyFrame = frame.clone();;
                    frameQue.add(copyFrame);
                }
                timer.end();
                System.out.println("------------抓取到一帧..timestamp=" + frame.timestamp + "  ,耗时：" + timer.getTimeInMillSecond() + "毫秒,bufferSize="+frameQue.size());
                frame=null;
                grabCount++;
                totalTimer.end();
                if (totalTimer.getTimeInSecond() >= 1.0) {
                    System.out.println("1秒内抓取到总帧数：" + grabCount+" ,删除掉的总帧数："+deleteCount);
                    grabCount = 0;
                    deleteCount=0;
                    totalTimer = new TimeUtil();
                    totalTimer.start();
                }
            }

        }
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
