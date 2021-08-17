package com.md.liveroom;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.*;

public class DirectPusher {
    private String incomingPath;
    private String outcomingPath;
    private boolean exit;
    private Runnable worker;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;

    public DirectPusher(String incomingPath, String outcomingPath) {
        this.incomingPath = incomingPath;
        this.outcomingPath = outcomingPath;
    }

    public void start()
    {
        exit=false;
        if(worker!=null)
            return;
        worker=createWorker();
        new Thread(worker).start();
    }

    public void stop()
    {
        exit=true;
    }


    private Runnable createWorker()
    {
        return new Runnable(){
            @Override
            public void run()  {
                grabber=new FFmpegFrameGrabber(incomingPath);
                int audioChannels=1;
                int sampleRate=48000;
                grabber.setAudioChannels(audioChannels);
                grabber.setSampleRate(sampleRate);
                grabber.setFrameRate(30);
                try {
                    grabber.start();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }

                recorder = new FFmpegFrameRecorder(outcomingPath, 400, 300);
//        recorder.setInterleaved(true);
                recorder.setFormat("flv");
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setFrameRate(30);
                recorder.setVideoBitrate(3*1024*1024);
//                recorder.setVideoOption("tune", "zerolatency");
//                recorder.setVideoOption("qp", "0");
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
//        recorder.setVideoCodecName("h264_amf");
                // 不可变(固定)音频比特率
//        recorder.setAudioOption("crf", "0");
                // 最高质量
//            recorder.setAudioQuality(0);
                // 音频比特率
                recorder.setAudioBitrate(2*128*1024);
                // 音频采样率
                recorder.setSampleRate(sampleRate);
                // 双通道(立体声)
                recorder.setAudioChannels(audioChannels);
                try {
                    recorder.start();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
                while(!exit)
                {
                    Frame frame= null;
                    try {
                        frame = grabber.grab();
                    } catch (FrameGrabber.Exception e) {
                        e.printStackTrace();
                    }
                    if(frame==null)
                    {
                        System.out.println("---------------no frame grabbed..." );
                        continue;
                    }

                    avutil.AVFrame avframe=(avutil.AVFrame)frame.opaque;//把Frame直接强制转换为AVFrame
                    long lastPts=avframe.pts();
                    long lastDts=avframe.pkt_dts();
                    long duration=avframe.pkt_duration();//+intervalDuration;
                    long pos=avframe.pkt_pos();
//            avframe.pkt_duration(duration);
                    recorder.setTimestamp(frame.timestamp);
                    try
                    {
                        recorder.record(frame);
                        System.out.println("record one frame..."+frame.timestamp+"   pts："+lastPts+"  dts:"+lastDts+"  pos="+pos+"  duration="+duration);
                    }catch (Exception e)
                    {

                    }
                }
                try {
                    grabber.stop();
                    grabber.release();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
                try {
                    recorder.stop();
                    recorder.release();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
