package com.md.ffmpeg;

import com.md.api.IFrameMerger;
import com.md.api.IFramePusher;
import com.md.util.ThreadUtil;
import com.md.util.TimeUtil;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//基本的推流器
public class BasicFramePusherForStream1 implements IFramePusher {
    private IFrameMerger frameMerger;
    private boolean exit = false;
    private String rtmpPath;//推流地址
    private Runnable worker = null;
    private FFmpegFrameRecorder recorder;
    private GrabFrameCotroller audioFrameController;
    private GrabFrameCotroller videoFrameController;

    public BasicFramePusherForStream1(String rtmpPath) {
        this.rtmpPath = rtmpPath;
    }

    public BasicFramePusherForStream1(String rtmpPath, IFrameMerger frameMerger) {
        this.rtmpPath = rtmpPath;
        this.frameMerger = frameMerger;
    }

    @Override
    public void setFrameMerger(IFrameMerger frameMerger) {
        this.frameMerger = frameMerger;
    }

    @Override
    public void start() {
        if (null == rtmpPath || rtmpPath.length() < 2)
            throw new RuntimeException("推流地址设置错误！");
        if (null == frameMerger)
            throw new RuntimeException("推帧器需要一个合帧器！");
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

    private Runnable createWorker() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("推帧器启动了...");
                frameMerger.start();
                openRecorder();
                pushFrameRepeatly();
                closeRecorder();
                frameMerger.stop();
                System.out.println("推帧器停止了...");
            }
        };
    }


    private void sleep(long millSeconds) {
        try {
            Thread.sleep(millSeconds);
        } catch (Exception e) {
        }
    }

    private void openRecorder() {
        try {
            recorder = new FFmpegFrameRecorder(rtmpPath, 600, 400);
//            recorder.setInterleaved(true);
            recorder.setFormat("flv");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//            recorder.setVideoCodecName("h264_amf");
            /**
             * 该参数用于降低延迟 参考FFMPEG官方文档：https://trac.ffmpeg.org/wiki/StreamingGuide
             * 官方原文参考：ffmpeg -f dshow -i video="Virtual-Camera" -vcodec libx264
             * -tune zerolatency -b 900k -f mpegts udp://10.1.0.102:1234
             */

//            recorder.setVideoOption("tune", "zerolatency");
//            recorder.setVideoOption("tune", "stillimage");
//            recorder.setVideoOption("qp", "0");
            /**
             * 权衡quality(视频质量)和encode speed(编码速度) values(值)：
             * ultrafast(终极快),superfast(超级快), veryfast(非常快), faster(很快), fast(快),
             * medium(中等), slow(慢), slower(很慢), veryslow(非常慢)
             * ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小；而veryslow(非常慢)提供最佳的压缩（高编码器CPU）的同时降低视频流的大小
             * 参考：https://trac.ffmpeg.org/wiki/Encode/H.264 官方原文参考：-preset ultrafast
             * as the name implies provides for the fastest possible encoding. If
             * some tradeoff between quality and encode speed, go for the speed.
             * This might be needed if you are going to be transcoding multiple
             * streams on one machine.
             */
            recorder.setVideoOption("preset", "ultrafast");
            /**
             * 参考转流命令: ffmpeg
             * -i'udp://localhost:5000?fifo_size=1000000&overrun_nonfatal=1' -crf 30
             * -preset ultrafast -acodec aac -strict experimental -ar 44100 -ac
             * 2-b:a 96k -vcodec libx264 -r 25 -b:v 500k -f flv 'rtmp://<wowza
             * serverIP>/live/cam0' -crf 30
             * -设置内容速率因子,这是一个x264的动态比特率参数，它能够在复杂场景下(使用不同比特率，即可变比特率)保持视频质量；
             * 可以设置更低的质量(quality)和比特率(bit rate),参考Encode/H.264 -preset ultrafast
             * -参考上面preset参数，与视频压缩率(视频大小)和速度有关,需要根据情况平衡两大点：压缩率(视频大小)，编/解码速度 -acodec
             * aac -设置音频编/解码器 (内部AAC编码) -strict experimental
             * -允许使用一些实验的编解码器(比如上面的内部AAC属于实验编解码器) -ar 44100 设置音频采样率(audio sample
             * rate) -ac 2 指定双通道音频(即立体声) -b:a 96k 设置音频比特率(bit rate) -vcodec libx264
             * 设置视频编解码器(codec) -r 25 -设置帧率(frame rate) -b:v 500k -设置视频比特率(bit
             * rate),比特率越高视频越清晰,视频体积也会变大,需要根据实际选择合理范围 -f flv
             * -提供输出流封装格式(rtmp协议只支持flv封装格式) 'rtmp://<FMS server
             * IP>/live/cam0'-流媒体服务器地址
             */
//            recorder.setVideoOption("crf", "25");
            // 2000 kb/s, 720P视频的合理比特率范围
            recorder.setVideoBitrate(3 * 1024 * 1024);
            // 关键帧间隔，一般与帧率相同或者是视频帧率的两倍
            recorder.setGopSize(30 * 2);
            recorder.setFrameRate(30);

            // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
//        recorder.setAudioOption("aq", "10");
            // 最高质量
//            recorder.setAudioQuality(0);
            // 音频比特率
            recorder.setAudioBitrate(2 * 128 * 1024);
            // 音频采样率
            recorder.setSampleRate(22050);
            // 双通道(立体声)
            recorder.setAudioChannels(2);
            recorder.start();


            audioFrameController = new GrabFrameCotroller(99);
            audioFrameController.start();
            videoFrameController = new GrabFrameCotroller(20);
            videoFrameController.start();
            System.out.println("------------新建一个recorder...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeRecorder() {
        try {
            recorder.stop();
            recorder.release();
            System.out.println("------------关闭一个recorder...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //连续推帧到推流地址
    private void pushFrameRepeatly() {
        Comparator<Frame> comparator = new Comparator<Frame>() {
            @Override
            public int compare(Frame frame1, Frame frame2) {
                if (null == frame1)
                    return -1;
                if (null == frame2)
                    return 1;
                if (frame2.timestamp > frame1.timestamp)
                    return -1;
                if (frame2.timestamp == frame1.timestamp)
                    return 0;
                return 1;
            }
        };
        TimeUtil totalTimer = new TimeUtil();
        totalTimer.start();
        int pushCount = 0;
        long lastTimeStamp = -1;
        List<Frame> totalFrames = new ArrayList<Frame>();
        while (!exit) {
            try {
//                List<Frame> frames=frameMerger.takeMergedFrames();
//               totalFrames.addAll(frames);
//               if(totalFrames.size()<3)
//                   continue;
//               frames=totalFrames;
//               totalFrames=new ArrayList<Frame>();
//                Collections.sort(frames,comparator);
//                for(int i=0;i<frames.size();i++)
                {
//                    Frame frame=frames.get(i);
                    Frame frame = frameMerger.takeFirstFrame();
                    if (null == frame)
                        continue;
                    if (lastTimeStamp < 0)
                        lastTimeStamp = frame.timestamp;
                    if (frame.timestamp < lastTimeStamp) {
                        System.out.println("XXXXXXXXXXX推帧器推流一帧错误时间戳...." + frame.timestamp + " ,前一帧时间戳：" + lastTimeStamp);
                        continue;
                    } else
                        lastTimeStamp = frame.timestamp;
                    if (lastTimeStamp > recorder.getTimestamp() && frame.samples != null) {
                        System.out.println("XXXXXXXXXXX-AAAAAA推帧器推流一音频帧时间戳晚于当前时间戳...." + frame.timestamp + " ,当前时间戳：" + recorder.getTimestamp());
                        lastTimeStamp = recorder.getTimestamp();
                        frame.timestamp = lastTimeStamp;
//                        continue;
                    }

                    if (lastTimeStamp < recorder.getTimestamp() && frame.samples != null) {
                        System.out.println("XXXXXXXXXXX-AAAAAA推帧器推流一音频帧时间戳早于当前时间戳...." + frame.timestamp + " ,当前时间戳：" + recorder.getTimestamp());
                        lastTimeStamp = recorder.getTimestamp();
                        frame.timestamp = lastTimeStamp;
//                        continue;
                    }

//                    if(lastTimeStamp<recorder.getTimestamp()&&frame.image!=null)
//                    {
//                        System.out.println("XXXXXXXXXXX推帧器推流一视频帧时间戳早于当前时间戳...."+frame.timestamp+" ,当前时间戳："+recorder.getTimestamp());
////                        lastTimeStamp=recorder.getTimestamp();
////                        frame.timestamp=lastTimeStamp;
//                        continue;
//                    }
//
//                    if(lastTimeStamp>recorder.getTimestamp()&&frame.image!=null)
//                    {
////                        System.out.println("XXXXXXXXXXX推帧器推流一视频帧时间戳早于当前时间戳...."+frame.timestamp+" ,当前时间戳："+recorder.getTimestamp());
//                        lastTimeStamp=recorder.getTimestamp();
//                        frame.timestamp=lastTimeStamp;
////                        continue;
//                    }
//                    if(lastTimeStamp<recorder.getTimestamp()) {
////                     lastTimeStamp=recorder.getTimestamp();
////                     frame.timestamp=lastTimeStamp;
//                        System.out.println("YYYYYYYYYYYYYY推帧器推流一帧时间戳早于当前时间戳...."+frame.timestamp+" ,视频帧："+(frame.image!=null));
//                        if(frame.image!=null)
//                            continue;
////                     continue;
//                    }

//                    if(lastTimeStamp>recorder.getTimestamp())
//                    {
////                        lastTimeStamp=recorder.getTimestamp();
////                        frame.timestamp=lastTimeStamp;
//                        System.out.println("YYYYYYYYYYYYYY推帧器推流一帧时间戳晚于当前时间戳...."+frame.timestamp+" ,视频帧："+(frame.image!=null));
//                    }
//                    if(frame.samples!=null&& audioFrameController.canGrab()==false)
//                    {
//                        continue;
//                    }
//                    if(frame.image!=null&& videoFrameController.canGrab()==false)
//                    {
//                        continue;
//                    }

                    try {
//                        lastTimeStamp=recorder.getTimestamp();
//                        frame.timestamp=lastTimeStamp;
                        frame.opaque = null;
                        recorder.setTimestamp(frame.timestamp);
//                        if(frame.samples!=null)
//                            recorder.recordSamples(frame.samples);
//                        else
                        recorder.record(frame);
                        if (frame.image != null)
                            System.out.println("-----------------------推帧器成功推流一视频帧...." + frame.timestamp);
                        if (frame.samples != null)
                            System.out.println("-----------------------推帧器成功推流一音频帧...." + frame.timestamp);
                    } catch (Exception e) {

                    }
//                    if(frame.samples!=null)
//                        audioFrameController.updateGrabCount();
//                    if(frame.image!=null)
//                        videoFrameController.updateGrabCount();

                    pushCount++;
                    totalTimer.end();
                    if (totalTimer.getTimeInSecond() >= 1.0) {
                        System.out.println("1秒内录制推流的总帧数：" + pushCount);
                        pushCount = 0;
                        totalTimer = new TimeUtil();
                        totalTimer.start();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                closeRecorder();
                openRecorder();
            }
        }

    }
}
