package com.md.ffmpeg;

import com.md.api.IFrameMerger;
import com.md.api.IFramePusher;
import com.md.api.IFrameTaker;
import com.md.red5.DirectPusher;
import com.md.red5.IPusher;
import com.md.util.TimeUtil;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil.AVFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;
import sun.font.FontDesignMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TestPusher {

    public static void main(String[] args) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        testTwoAudioFilter();
//                        testFramePusherToStream12Syn();
//                        testFramePusherToStream12();
                        testFramePusherToMp412();
//                        testFramePusherToStream2();
//                        testRealFramePusherToStream();
//                        testUnityFramePusherToStream();
//                        testRealTimeGrabAndRecord2();
//                        testUnityFramePusherToStream();
//                        testDirectGrabAndRecord();

//                        testDirectGrab2AndRecordMp4File();
//                        testDirectGrabAndRecordMp4File();
//                        testDirectGrab2AndFilterAndRecordMp4File();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testDirectGrab2AndRecordMp4File()throws Exception
    {
        int audioChannels=1;
        int sampleRate=48000;
        FFmpegFrameGrabber grabber1=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
        grabber1.setAudioChannels(audioChannels);
        grabber1.setSampleRate(sampleRate);
        grabber1.setFrameRate(30);
        grabber1.start();
        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
        grabber2.setAudioChannels(audioChannels);
        grabber2.setSampleRate(sampleRate);
        grabber2.setFrameRate(30);
        grabber2.start();

//        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 800, 600);
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("c:\\test1.mp4", 800, 600);
        recorder.setInterleaved(true);
        recorder.setFormat("mp4");
//        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(30);
        recorder.setVideoBitrate(8*1024*1024);
//        recorder.setVideoCodecName("h264_amf");
        // 不可变(固定)音频比特率
//        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(4*128*1024);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(audioChannels);
        recorder.start();
        int k=0;
        //直接推送，但是修改帧时间戳为新时间戳
        long curTimestamp=0;
        TwoAudioFrameMerger audioFrameMerger=new TwoAudioFrameMerger();
        TwoVideoFrameMerger videoFrameMerger=new TwoVideoFrameMerger();
        boolean haveImage1=false;
        while(true)
        {
            Frame frame1=grabber1.grab();
            if(frame1==null)
            {
                System.out.println("---------------no frame1 grabbed..." );
                continue;
            }
            curTimestamp=frame1.timestamp;
            haveImage1=false;
           if(frame1.samples!=null)
           {
               Frame mergedFrame=audioFrameMerger.mergeFrame(frame1,1);
               if(mergedFrame!=null)
               {
                   if(curTimestamp>recorder.getTimestamp())
                       curTimestamp=recorder.getTimestamp();
                   mergedFrame.opaque=null;
                   mergedFrame.timestamp=curTimestamp;
//                   recorder.setTimestamp(curTimestamp);
                   try
                   {
                       recorder.record(mergedFrame);
                       System.out.println("record one  mixed audio frame1..."+curTimestamp);
                   }catch (Exception e)
                   {

                   }

               }
           }
           if(frame1.image!=null)
           {
               videoFrameMerger.updateBigImage(frame1);
               Frame mergedFrame=videoFrameMerger.mergeFrame(curTimestamp);
//               Frame mergedFrame=frame1.clone();
               if(mergedFrame!=null)
               {
                   if(curTimestamp>recorder.getTimestamp())
                       curTimestamp=recorder.getTimestamp();
                   mergedFrame.opaque=null;
                   mergedFrame.timestamp=curTimestamp;
//                   recorder.setTimestamp(curTimestamp);
                   try
                   {
                       recorder.record(mergedFrame);
                       System.out.println("vvvvvvvvvv1-----record one  mixed video frame1..."+curTimestamp);
                       haveImage1=true;
                   }catch (Exception e)
                   {

                   }

               }
           }

            Frame frame2=grabber2.grab();
            if(frame2==null)
            {
                System.out.println("---------------no frame2 grabbed..." );
                continue;
            }

            if(frame2.image!=null)
            {
                videoFrameMerger.updateSmallImage(frame2);
                Frame mergedFrame=videoFrameMerger.mergeFrame(curTimestamp);
//                Frame mergedFrame=frame2.clone();
                if(mergedFrame!=null)
                {
                    if(curTimestamp>recorder.getTimestamp())
                        curTimestamp=recorder.getTimestamp();
                    mergedFrame.opaque=null;
                    mergedFrame.timestamp=curTimestamp;
//                    recorder.setTimestamp(curTimestamp);
                    try
                    {
                        recorder.record(mergedFrame);
                        System.out.println("record one  mixed video frame2..."+curTimestamp);
                    }catch (Exception e)
                    {

                    }

                }
            }

//            if(frame1.image!=null||frame2.image!=null)
//            {
////                videoFrameMerger.updateBigImage(frame1);
//                Frame mergedFrame=videoFrameMerger.mergeFrame(curTimestamp);
////               Frame mergedFrame=frame1.clone();
//                if(mergedFrame!=null)
//                {
//                    mergedFrame.opaque=null;
//                    mergedFrame.timestamp=curTimestamp;
//                    recorder.setTimestamp(curTimestamp);
//                    try
//                    {
//                        recorder.record(mergedFrame);
//                        System.out.println("vvvvvvvvvv1-----record one  mixed video frame1..."+curTimestamp);
//                        haveImage1=true;
//                    }catch (Exception e)
//                    {
//
//                    }
//
//                }
//            }

            if(frame2.samples!=null)
            {
                Frame mergedFrame=audioFrameMerger.mergeFrame(frame2,2);
                if(mergedFrame!=null)
                {
                    if(curTimestamp>recorder.getTimestamp())
                        curTimestamp=recorder.getTimestamp();
                    mergedFrame.opaque=null;
                    mergedFrame.timestamp=curTimestamp;
//                    recorder.setTimestamp(curTimestamp);
                    try
                    {
                        recorder.record(mergedFrame);
                        System.out.println("record one  mixed audio frame2..."+curTimestamp);
                    }catch (Exception e)
                    {

                    }

                }
            }


            k++;
            if(k>4000)
                break;

        }
        grabber1.stop();
        grabber1.release();
        grabber2.stop();
        grabber2.release();
        recorder.stop();
        recorder.release();
    }

    private static class MyFrameFilter
    {
        private FFmpegFrameFilter avFilter;

        public MyFrameFilter()
        {
            String vStr="fps=fps=30";
            String aStr="aresample=48000,anull";
            avFilter=new FFmpegFrameFilter(vStr,aStr,800,600,1);
            avFilter.setSampleRate(48000);
            avFilter.setFrameRate(30);
            try {
                avFilter.start();
            } catch (FrameFilter.Exception e) {
                e.printStackTrace();
            }
        }

        public Frame filterFrame(Frame frame)
        {
            if(null==frame)
                return null;
            if(frame.image==null&&frame.samples==null)
                return null;
            try {
                avFilter.push(frame);
                Frame mergedFrame=avFilter.pull();
                if(mergedFrame==null)
                    return null;
                if(mergedFrame.samples==null&&mergedFrame.image==null)
                    return null;
                System.out.println("MMMMM----过滤后的帧："+mergedFrame.timestamp);
                return mergedFrame;
            } catch (FrameFilter.Exception e) {
                return null;
            }
        }
    }

    public static void testDirectGrab2AndFilterAndRecordMp4File()throws Exception
    {
        int audioChannels=1;
        int sampleRate=48000;
        FFmpegFrameGrabber grabber1=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
        grabber1.setAudioChannels(audioChannels);
        grabber1.setSampleRate(sampleRate);
        grabber1.setFrameRate(30);
        grabber1.start();
        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
        grabber2.setAudioChannels(audioChannels);
        grabber2.setSampleRate(sampleRate);
        grabber2.setFrameRate(30);
        grabber2.start();

//        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 800, 600);
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("c:\\test1.mp4", 800, 600);
        recorder.setInterleaved(true);
        recorder.setFormat("mp4");
//        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(30);
        recorder.setVideoBitrate(8*1024*1024);
//        recorder.setVideoCodecName("h264_amf");
        // 不可变(固定)音频比特率
//        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(4*128*1024);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(audioChannels);
        recorder.start();
        int k=0;
        //直接推送，但是修改帧时间戳为新时间戳
        long curTimestamp=0;
        TwoAudioFrameMerger audioFrameMerger=new TwoAudioFrameMerger();
        TwoVideoFrameMerger videoFrameMerger=new TwoVideoFrameMerger();
        boolean haveImage1=false;
        MyFrameFilter myFrameFilter=new MyFrameFilter();
        while(true)
        {
            Frame frame1=grabber1.grab();
            if(frame1==null)
            {
                System.out.println("---------------no frame1 grabbed..." );
                continue;
            }
            curTimestamp=frame1.timestamp;
            haveImage1=false;
            if(frame1.samples!=null)
            {
                Frame mergedFrame=audioFrameMerger.mergeFrame(frame1,1);
                mergedFrame=myFrameFilter.filterFrame(mergedFrame);
                if(mergedFrame!=null)
                {
//                    if(curTimestamp>recorder.getTimestamp())
//                        curTimestamp=recorder.getTimestamp();
//                    mergedFrame.opaque=null;
//                    mergedFrame.timestamp=curTimestamp;
//                   recorder.setTimestamp(curTimestamp);
                    try
                    {
                        recorder.record(mergedFrame);
                        System.out.println("record one  mixed audio frame1..."+curTimestamp);
                    }catch (Exception e)
                    {

                    }

                }
            }
            if(frame1.image!=null)
            {
                videoFrameMerger.updateBigImage(frame1);
                Frame mergedFrame=videoFrameMerger.mergeFrame(curTimestamp);
                mergedFrame=myFrameFilter.filterFrame(mergedFrame);
                if(mergedFrame!=null)
                {
//                    if(curTimestamp>recorder.getTimestamp())
//                        curTimestamp=recorder.getTimestamp();
//                    mergedFrame.opaque=null;
//                    mergedFrame.timestamp=curTimestamp;
//                   recorder.setTimestamp(curTimestamp);
                    try
                    {
                        recorder.record(mergedFrame);
                        System.out.println("vvvvvvvvvv1-----record one  mixed video frame1..."+curTimestamp);
                        haveImage1=true;
                    }catch (Exception e)
                    {

                    }

                }
            }

            Frame frame2=grabber2.grab();
            if(frame2==null)
            {
                System.out.println("---------------no frame2 grabbed..." );
                continue;
            }

            if(frame2.image!=null)
            {
                videoFrameMerger.updateSmallImage(frame2);
                Frame mergedFrame=videoFrameMerger.mergeFrame(curTimestamp);
                mergedFrame=myFrameFilter.filterFrame(mergedFrame);
                if(mergedFrame!=null)
                {
//                    if(curTimestamp>recorder.getTimestamp())
//                        curTimestamp=recorder.getTimestamp();
//                    mergedFrame.opaque=null;
//                    mergedFrame.timestamp=curTimestamp;
//                    recorder.setTimestamp(curTimestamp);
                    try
                    {
                        recorder.record(mergedFrame);
                        System.out.println("record one  mixed video frame2..."+curTimestamp);
                    }catch (Exception e)
                    {

                    }

                }
            }

//            if(frame1.image!=null||frame2.image!=null)
//            {
////                videoFrameMerger.updateBigImage(frame1);
//                Frame mergedFrame=videoFrameMerger.mergeFrame(curTimestamp);
////               Frame mergedFrame=frame1.clone();
//                if(mergedFrame!=null)
//                {
//                    mergedFrame.opaque=null;
//                    mergedFrame.timestamp=curTimestamp;
//                    recorder.setTimestamp(curTimestamp);
//                    try
//                    {
//                        recorder.record(mergedFrame);
//                        System.out.println("vvvvvvvvvv1-----record one  mixed video frame1..."+curTimestamp);
//                        haveImage1=true;
//                    }catch (Exception e)
//                    {
//
//                    }
//
//                }
//            }

            if(frame2.samples!=null)
            {
                Frame mergedFrame=audioFrameMerger.mergeFrame(frame2,2);
                mergedFrame=myFrameFilter.filterFrame(mergedFrame);
                if(mergedFrame!=null)
                {
//                    if(curTimestamp>recorder.getTimestamp())
//                        curTimestamp=recorder.getTimestamp();
//                    mergedFrame.opaque=null;
//                    mergedFrame.timestamp=curTimestamp;
//                    recorder.setTimestamp(curTimestamp);
                    try
                    {
                        recorder.record(mergedFrame);
                        System.out.println("record one  mixed audio frame2..."+curTimestamp);
                    }catch (Exception e)
                    {

                    }

                }
            }


            k++;
            if(k>4000)
                break;

        }
        grabber1.stop();
        grabber1.release();
        grabber2.stop();
        grabber2.release();
        recorder.stop();
        recorder.release();
    }


    public static void testDirectGrabAndRecord()throws Exception
    {
        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
//        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("d:\\ffmpeg\\b5.mp4");
        int audioChannels=1;
        int sampleRate=48000;
        grabber.setAudioChannels(audioChannels);
        grabber.setSampleRate(sampleRate);
        grabber.setFrameRate(30);
//        grabber.setVideoCodecName("h264_amf");
        grabber.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 1024, 768);
//        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("c:\\test1.mp4", 800, 600);
//        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(30);
        recorder.setVideoBitrate(8*1024*1024);
//        recorder.setVideoCodecName("h264_amf");
        // 不可变(固定)音频比特率
//        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(4*128*1024);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(audioChannels);
        recorder.start();
        int k=0;
        //直接推送，但是修改帧时间戳为新时间戳
        long orginStartTimeStamp=-1,newStartTimeStamp=-1,orginLastTimeStamp=0;
        Random random=new Random();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        final int interval=1;
        int intervalCount=0;
        Frame mergedFrame=null;
        long intervalDuration=0;
        while(true)
        {
            Frame frame=grabber.grab();
            if(frame==null)
            {
                System.out.println("---------------no frame grabbed..." );
                continue;
            }
//            if(intervalCount==0)
//            {
//                mergedFrame=frame;
//                intervalCount++;
//                intervalDuration=0;
//                continue;
//            }
//            if(intervalCount<interval)
//            {
//                AVFrame avframe=(AVFrame)frame.opaque;//把Frame直接强制转换为AVFrame
//                intervalDuration+=avframe.pkt_duration();
//                intervalCount++;
//                continue;
//            }


            AVFrame avframe=(AVFrame)frame.opaque;//把Frame直接强制转换为AVFrame
            long lastPts=avframe.pts();
            long lastDts=avframe.pkt_dts();
            long duration=avframe.pkt_duration();//+intervalDuration;
            long pos=avframe.pkt_pos();
//            avframe.pkt_duration(duration);
            recorder.setTimestamp(frame.timestamp);
            recorder.record(frame);
            System.out.println("record one frame..."+frame.timestamp+"   pts："+lastPts+"  dts:"+lastDts+"  pos="+pos+"  duration="+duration);
            intervalCount=0;
            k++;
            if(k>Integer.MAX_VALUE)
                break;

        }
        grabber.stop();
        grabber.release();
        recorder.stop();
        recorder.release();
    }

    public static void testDirectGrabAndRecordMp4File()throws Exception
    {
        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
//        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("d:\\ffmpeg\\b1.mp4");
        int audioChannels=1;
        int sampleRate=48000;
        grabber.setAudioChannels(audioChannels);
        grabber.setSampleRate(sampleRate);
        grabber.setFrameRate(30);
//        grabber.setVideoCodecName("h264_amf");
        grabber.start();

//        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 800, 600);
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("c:\\test1.mp4", 800, 600);
//        recorder.setInterleaved(true);
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(30);
        recorder.setVideoBitrate(8*1024*1024);
//        recorder.setVideoCodecName("h264_amf");
        // 不可变(固定)音频比特率
//        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(4*128*1024);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(audioChannels);
        recorder.start();
        int k=0;
        //直接推送，但是修改帧时间戳为新时间戳
        long orginStartTimeStamp=-1,newStartTimeStamp=-1,orginLastTimeStamp=0;
        Random random=new Random();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        final int interval=1;
        int intervalCount=0;
        Frame mergedFrame=null;
        long intervalDuration=0;
        while(true)
        {
            Frame frame=grabber.grab();
            if(frame==null)
            {
                System.out.println("---------------no frame grabbed..." );
                continue;
            }
//            if(intervalCount==0)
//            {
//                mergedFrame=frame;
//                intervalCount++;
//                intervalDuration=0;
//                continue;
//            }
//            if(intervalCount<interval)
//            {
//                AVFrame avframe=(AVFrame)frame.opaque;//把Frame直接强制转换为AVFrame
//                intervalDuration+=avframe.pkt_duration();
//                intervalCount++;
//                continue;
//            }


            AVFrame avframe=(AVFrame)frame.opaque;//把Frame直接强制转换为AVFrame
            long lastPts=avframe.pts();
            long lastDts=avframe.pkt_dts();
            long duration=avframe.pkt_duration();//+intervalDuration;
            long pos=avframe.pkt_pos();
//            avframe.pkt_duration(duration);
            recorder.setTimestamp(frame.timestamp);
            recorder.record(frame);
            System.out.println("record one frame..."+frame.timestamp+"   pts："+lastPts+"  dts:"+lastDts+"  pos="+pos+"  duration="+duration);
            intervalCount=0;
            k++;
            if(k>2000)
                break;

        }
        grabber.stop();
        grabber.release();
        recorder.stop();
        recorder.release();
    }


    public static void testFrameTaker()throws Exception
    {
        IFrameTaker taker=new BasicFrameTaker("rtmp://127.0.0.1:1935/oflaDemo/stream11",10);
        taker.start();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream15", 300, 200);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFormat("flv");
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(1);
        recorder.setFrameRate(20);
        recorder.start();
        int k=0;
        while(true)
        {
//            Frame frame=taker.takeLastFrame();
//            if(frame==null)
//                continue;
            List<Frame> frames=taker.takeFrames();
            for(Frame frame:frames)
            {
                recorder.setTimestamp(frame.timestamp);
                recorder.record(frame);
                System.out.println("record one frame..."+frame.timestamp);
            }

            k++;
//            if(k>100)
//                break;
        }
//        taker.stop();
//        recorder.stop();
//        recorder.release();
    }

    private static class AudioFrameFormat
    {
        private FFmpegFrameFilter filter ;
        public AudioFrameFormat()
        {
            String filterStr = "aformat=sample_fmts=s16:channel_layouts=stereo:sample_rates=48000[a1];[a1]anull";
            filter =new FFmpegFrameFilter(filterStr,1);
//            filter.setAudioInputs(1);
            try {
                filter.start();
            } catch (FrameFilter.Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        public Frame format(Frame audioFrame)
        {
            try {
                filter.push(audioFrame);
                Frame mixedAudioFrame=filter.pullSamples();
                return  mixedAudioFrame;
            } catch (FrameFilter.Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }


    public static void testFramePusherToStream12Syn()throws Exception
    {
        final IFrameTaker[] taker1 = new IFrameTaker[1];
        final IFrameTaker[] taker2 = new IFrameTaker[1];
        final IFrameMerger[] merger = new IFrameMerger[1];
        final IFramePusher[] pusher = new IFramePusher[1];

        taker1[0] =new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",10);
        taker2[0] =new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream13",10);
        merger[0] =new TwoStreamFrameMergerForStream(100);
        merger[0].setFrameTakers(taker1[0], taker2[0]);
        pusher[0] =new BasicFramePusherForStream("rtmp://127.0.0.1/oflaDemo/stream19");
        pusher[0].setFrameMerger(merger[0]);
        pusher[0].start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while(true)
                {
                    try
                    {
                        Thread.sleep(10);
                        if(taker2[0].isDisconnected())
                        {
                            pusher[0].stop();
                            taker1[0] =new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",10);
                            taker2[0] =new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream13",10);
                            merger[0] =new TwoStreamFrameMergerForStream(100);
                            merger[0].setFrameTakers(taker1[0], taker2[0]);
                            pusher[0] =new BasicFramePusherForStream("rtmp://127.0.0.1/oflaDemo/stream19");
                            pusher[0].setFrameMerger(merger[0]);
                            pusher[0].start();
                            Thread.sleep(25000);
                        }
                    }catch (Exception e)
                    {

                    }
                }
            }
        }).start();

    }

    public static void testFramePusherToStream12()throws Exception
    {
        IFrameTaker taker1=new CameraFrameTaker("rtmp://iot.dushuren123.com/oflaDemo/clientpush1684442105",400);
        IFrameTaker taker2=new CameraFrameTaker("rtmp://iot.dushuren123.com/oflaDemo/managerpush1684442105",400);

//        IFrameTaker taker1=new CameraFrameTaker("d:\\ffmpeg\\b1.mp4",20);
//        IFrameTaker taker2=new CameraFrameTaker("d:\\ffmpeg\\b2.mp4",20);

        IFrameMerger merger=new TwoStreamFrameMergerForStream4(800);
//        IFrameMerger merger=new TwoCameraStreamFrameMerger(10);
        merger.setFrameTakers(taker1,taker2);

        IFramePusher pusher=new BasicFramePusherForStream("rtmp://127.0.0.1/oflaDemo/stream20");
        pusher.setFrameMerger(merger);
        pusher.start();
//        DirectPusher pusher=new DirectPusher("rtmp://iot.dushuren123.com/oflaDemo/clientpush1505914745","rtmp://127.0.0.1/oflaDemo/stream20");
//        pusher.start();
    }

    public static void testFramePusherToMp412()throws Exception
    {
//        IFrameTaker taker1=new VideoAudioFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",600);
//        IFrameTaker taker2=new VideoAudioFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",600);

        IFrameTaker taker1=new CameraFrameTaker("rtmp://iot.dushuren123.com/oflaDemo/clientpush1684442105",400);
        IFrameTaker taker2=new CameraFrameTaker("rtmp://iot.dushuren123.com/oflaDemo/managerpush1684442105",400);
        IFrameMerger merger=new TwoStreamFrameMergerForStream4(800);
        merger.setFrameTakers(taker1,taker2);

        IFramePusher pusher=new BasicFramePusherForMp4("c:\\test91.mp4");
        pusher.setFrameMerger(merger);
        pusher.start();
        try {
            Thread.sleep(100*1000);
        }catch (Exception e)
        {

        }
        pusher.stop();
        try {
            Thread.sleep(1*1000);
        }catch (Exception e)
        {

        }
    }


    public static void testTwoAudioFilter()throws Exception
    {
        int audioChannels=1;
        int sampleRate=48000;
        IFrameTaker taker1=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",100);
        IFrameTaker taker2=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",200);
taker1.start();
taker2.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 320, 240);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(8*1024*1024);
        // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
//        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(8*128*1024);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(1);
        recorder.start();
        int k=0;
        //直接推送，但是修改帧时间戳为新时间戳
        long orginStartTimeStamp=-1,newStartTimeStamp=-1,orginLastTimeStamp=0,realLastTimeStamp=0;
        Random random=new Random();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        Java2DFrameConverter converter2 = new Java2DFrameConverter();
        BufferedImage bigImage =null,smallImage=null;
//        String filterStr = "[0:a][1:a]amix=inputs=2[a]";
//        String filterStr = "[0:a][1:a]amerge=inputs=2[a]";
        String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000[audio2];[audio1][audio2]amix=inputs=2[a]";
        FFmpegFrameFilter filter2 =new FFmpegFrameFilter("scale=800:600,transpose=1,transpose=1",600,400);
        filter2.start();
        FFmpegFrameFilter filter =new FFmpegFrameFilter(filterStr,1);
        filter.setAudioInputs(2);
        filter.start();
        String filterStra = "aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000[a1];[a1]anull";
        FFmpegFrameFilter filter3 =new FFmpegFrameFilter(filterStra,1);
        filter3.start();
        AudioFrameFormat frameFormat=new AudioFrameFormat();
        Frame frame=null,frame2=null;
        while(true)
        {
            if(frame==null)
            {
                frame=taker1.takeFirstFrame();
                if(frame==null)
                {
//                    System.out.println("---------------no frame grabbed..." );
                    continue;
                }
            }

            if(frame2==null)
            {
                frame2=taker2.takeFirstFrame();
                if(frame2==null)
                {
//                    System.out.println("---------------no frame2 grabbed..." );
                    continue;
                }
            }



            if(frame.samples!=null)
            {
//                Frame mixedAudioFrame=frameFormat.format(frame);
//                if(mixedAudioFrame!=null)
//                {
//                    mixedAudioFrame=frameFormat.format(frame);
//                    recorder.setTimestamp(frame.timestamp);
//                        recorder.recordSamples(mixedAudioFrame.samples);
//                        System.out.println("AAAAAAA---------------mixed two audio frames..." );
//                }
                filter.push(0,frame);
                Frame mixedAudioFrame=filter.pullSamples();
                if(mixedAudioFrame!=null)
                {
//                    filter3.push(mixedAudioFrame);
//                    mixedAudioFrame=filter3.pullSamples();
                    if(mixedAudioFrame!=null)
                    {
                        recorder.setTimestamp(frame.timestamp);
                        recorder.recordSamples(mixedAudioFrame.samples);
                        System.out.println("AAAAAAA1111---------------mixed two audio frames..." );
                    }

                }
            }
            if(frame2.samples!=null)
            {
                filter.push(1,frame2);
                Frame mixedAudioFrame=filter.pullSamples();
                if(mixedAudioFrame!=null)
                {
//                    filter3.push(mixedAudioFrame);
//                    mixedAudioFrame=filter3.pullSamples();
                    if(mixedAudioFrame!=null)
                    {
                        recorder.setTimestamp(frame.timestamp);
                        recorder.recordSamples(mixedAudioFrame.samples);
                        System.out.println("AAAAAAA2222---------------mixed two audio frames..." );
                    }

                }
            }
            if(frame.image!=null)
            {
                filter2.push(frame);
                Frame mergedFrame=filter2.pullImage();
                recorder.setTimestamp(frame.timestamp);
                recorder.record(mergedFrame);
                System.out.println("VVVV---------------mixed two video frames..." );
            }

            frame=null;
            frame2=null;

            k++;
            if(k>Integer.MAX_VALUE)
                break;

        }
        taker1.stop();
        taker2.stop();
        recorder.stop();
        recorder.release();
    }



    public static void testFrameMerger()throws Exception
    {
        IFrameTaker taker1=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",100);
        IFrameTaker taker2=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",200);

        IFrameMerger merger=new TwoStreamFrameMergerForMp4(100);
        merger.setFrameTakers(taker1,taker2);
        merger.start();


        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream15", 300, 200);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFormat("flv");
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(1);
        recorder.setFrameRate(20);
        recorder.start();
        int k=0;
        while(true)
        {
//            Frame frame=taker.takeLastFrame();
//            if(frame==null)
//                continue;
            List<Frame> frames=merger.takeMergedFrames();
            for(Frame frame:frames)
            {
                recorder.setTimestamp(frame.timestamp);
                recorder.record(frame);
                System.out.println("record one frame..."+frame.timestamp);
            }

            k++;
//            if(k>100)
//                break;
        }
//        taker.stop();
//        recorder.stop();
//        recorder.release();
    }

    public static IFramePusher testFramePusherToFile()throws Exception
    {
        IFrameTaker taker1=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",10);
        IFrameTaker taker2=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",10);

        IFrameMerger merger=new TwoStreamFrameMergerForMp4(20);
//        IFrameMerger merger=new TwoStreamSimpleFrameMerger(30);
        merger.setFrameTakers(taker1,taker2);

//        IFramePusher pusher=new BasicFramePusher("rtmp://127.0.0.1/oflaDemo/stream15");
        IFramePusher pusher=new BasicFramePusherForStream("c:\\test1.flv");
        pusher.setFrameMerger(merger);
        pusher.start();
        return pusher;
    }

    public static void testFramePusherToStream11()throws Exception
    {
        BasicFrameTaker taker1=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",200);
        BasicFrameTaker taker2=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",200);


        IFrameMerger merger=new TwoStreamFrameMergerForMp4(200);
//        IFrameMerger merger=new TwoStreamSimpleFrameMerger(30);
        merger.setFrameTakers(taker1,taker2);
//        merger.start();

        IFramePusher pusher=new BasicFramePusherForStream("rtmp://127.0.0.1/oflaDemo/stream15");
        pusher.setFrameMerger(merger);
        pusher.start();
    }

    public static void testFramePusherToStream2()throws Exception
    {
        BasicFrameTaker taker1=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",200);
        BasicFrameTaker taker2=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",200);

        IFrameMerger merger=new TwoStreamFrameMergerForStream(300);
//        IFrameMerger merger=new TwoStreamSimpleFrameMerger(30);
        merger.setFrameTakers(taker1,taker2);
//        merger.start();

        IFramePusher pusher=new BasicFramePusherForStream("rtmp://127.0.0.1/oflaDemo/stream16");
        pusher.setFrameMerger(merger);
        pusher.start();
    }

    public static void testRealFramePusherToStream()throws Exception
    {

        BasicRealFrameTaker taker1=new BasicRealFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",300);
        BasicRealFrameTaker taker2=new BasicRealFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",300);


        IFrameMerger merger=new TwoStreamSimpleFrameMerger(600);
        merger.setFrameTakers(taker1,taker2);


        IFramePusher pusher=new BasicFramePusherForStream("rtmp://127.0.0.1/oflaDemo/stream16");
        pusher.setFrameMerger(merger);
        pusher.start();
    }

    public static void testUnityFramePusherToStream()throws Exception
    {
        UnitedTimestamp unitedTimestamp=new UnitedTimestamp();
        BasicUnityFrameTaker taker1=new BasicUnityFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",100);
        BasicUnityFrameTaker taker2=new BasicUnityFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",100);
        taker1.setUnitedTimestamp(unitedTimestamp,1);
        taker2.setUnitedTimestamp(unitedTimestamp,2);

        IFrameMerger merger=new TwoStreamUnityFrameMerger(200);
        merger.setFrameTakers(unitedTimestamp);


        IFramePusher pusher=new BasicFramePusherForStream("rtmp://127.0.0.1/oflaDemo/stream16");
        pusher.setFrameMerger(merger);
        pusher.start();
        taker1.start();
        taker2.start();
    }



    public static void testRealTimeGrabAndRecord2()throws Exception
    {
        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
        grabber.start();
        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
        grabber2.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream18", 320, 240);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(5*1024*1024);
        // 不可变(固定)音频比特率
        int sampleRate=44100;
        int audioChannels=4;
        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(192000);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(audioChannels);
        recorder.start();
        int k=0;
        //直接推送，但是修改帧时间戳为新时间戳
        long orginStartTimeStamp=-1,newStartTimeStamp=-1,orginLastTimeStamp=0,realLastTimeStamp=0;
        Random random=new Random();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        Java2DFrameConverter converter2 = new Java2DFrameConverter();
        BufferedImage bigImage =null,smallImage=null;
        while(true)
        {
            Frame frame=grabber.grabFrame();
            if(frame==null)
            {
                System.out.println("---------------no frame grabbed..." );
                continue;
            }
            Frame frame2=grabber2.grabFrame();
            if(frame2==null)
            {
                System.out.println("---------------no frame2 grabbed..." );
                continue;
            }
            if(newStartTimeStamp<0)
            {
                newStartTimeStamp=1;
                orginStartTimeStamp=frame.timestamp;
                orginLastTimeStamp=0;
                realLastTimeStamp=System.currentTimeMillis()*1000L;
            }
            long timestamp=frame.timestamp-orginStartTimeStamp;
            long gap=timestamp-orginLastTimeStamp;
            long realgap=System.currentTimeMillis()*1000L-realLastTimeStamp;
            realLastTimeStamp=System.currentTimeMillis()*1000L;
            if(timestamp<orginLastTimeStamp)
                continue;
            if(frame2.image!=null)
            {
                smallImage=converter2.getBufferedImage(frame2);
                if(bigImage!=null&&smallImage!=null)
                {
                    BufferedImage smallImage1=scaleImage(smallImage,0.36,bigImage.getWidth(),bigImage.getHeight());
                    BufferedImage bigImage1=combineTwoImages(smallImage1,bigImage);
                    bigImage1 = addSubtitle(bigImage1,"ok");
                    Frame tempFrame=converter.convert(bigImage1);
                    if(timestamp>=orginLastTimeStamp)
                    {
                        tempFrame.timestamp=timestamp;
                        recorder.setTimestamp(timestamp);
                        recorder.record(tempFrame);
                    }
                }
            }

            if(frame.image!=null)
            {
                bigImage = converter.getBufferedImage(frame);
                if(bigImage!=null&&smallImage!=null)
                {
                    BufferedImage smallImage1=scaleImage(smallImage,0.36,bigImage.getWidth(),bigImage.getHeight());
                    BufferedImage bigImage1=combineTwoImages(smallImage1,bigImage);
                    bigImage1 = addSubtitle(bigImage1,"ok");
                    frame=converter.convert(bigImage1);
                }else
                {
                    continue;
                }
            }
            frame.timestamp=timestamp;
            if(timestamp<orginLastTimeStamp)
                continue;
            orginLastTimeStamp=timestamp;
            recorder.setTimestamp(timestamp);
            recorder.record(frame);


            System.out.println("record one frame...framegap="+gap+"  realgap="+realgap);
//            if(frame2.image==null)
//            {
//                frame2.timestamp=timestamp;
//                recorder.setTimestamp(timestamp);
//                recorder.record(frame2);
//            }


            k++;
            if(k>Integer.MAX_VALUE)
                break;

        }
        grabber.stop();
        grabber.release();
        grabber2.stop();
        grabber2.release();
        recorder.stop();
        recorder.release();
    }

    public static void testTwoStreamPusher()throws Exception
    {
        IPusher pusher=new DirectPusher("rtmp://127.0.0.1/oflaDemo/stream11","rtmp://127.0.0.1/oflaDemo/stream18");
        pusher.start();
    }


    private static BufferedImage addSubtitle(BufferedImage bufImg, String subTitleContent) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 添加字幕时的时间
        Font font = new Font("微软雅黑", Font.BOLD, 12);
        String timeContent = sdf.format(new Date());
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        BufferedImage resultImg = new BufferedImage( bufImg.getWidth(),bufImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = resultImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        //设置图片背景
        graphics.drawImage(bufImg, 0, 0,  null);
        //设置左上方时间显示
        graphics.setColor(Color.orange);
        graphics.setFont(font);
        graphics.drawString(timeContent, 0, metrics.getAscent());

        // 计算文字长度，计算居中的x点坐标
//        int textWidth = metrics.stringWidth(subTitleContent);
//        int widthX = (bufImg.getWidth() - textWidth) / 2;
//        graphics.setColor(Color.red);
//        graphics.setFont(font);
//        graphics.drawString(subTitleContent, widthX, bufImg.getHeight() - 100);
        graphics.dispose();
        return resultImg;
    }

    //复制一个同样内容但缩放过的图像（按给定宽高）
    private static BufferedImage scaleImage(BufferedImage bufImg, double ratio, int maxWidth, int maxHeight) {
        int width = (int) (maxWidth * ratio);
        int height = (int) (maxHeight * ratio);
//           return (BufferedImage) bufImg.getScaledInstance(width,height,Image.SCALE_FAST);
        BufferedImage resultImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D graphics = resultImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        //设置图片背景
        graphics.drawImage(bufImg, 0, 0, width, height, null);
        graphics.dispose();
        return resultImg;
    }

    //将小图像叠加到大图像上合成一个新图像返回
    private static BufferedImage combineTwoImages(BufferedImage smallImage, BufferedImage bigImage) {
        Graphics2D graphics = bigImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        //叠加图像
        int marginx=(int)(bigImage.getWidth()*0.05);
        int marginy=(int)(bigImage.getHeight()*0.05);
        int x=bigImage.getWidth()-smallImage.getWidth()-marginx;
        int y=marginy;
        graphics.drawImage(smallImage,x,y, smallImage.getWidth(), smallImage.getHeight(), null);
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke((int)(bigImage.getWidth()*0.03)));
        graphics.draw3DRect(x,y,smallImage.getWidth(),smallImage.getHeight(),true);
        graphics.dispose();
        return bigImage;
    }

    //复制一个同样内容的图像
    private static BufferedImage copyImage(BufferedImage bufImg) {

        int width = bufImg.getWidth();
        int height = bufImg.getHeight();
        int type=bufImg.getType();
        System.out.println("----------------------------------------------------- copy bufferedimage type:"+type);
        BufferedImage resultImg = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
//            BufferedImage resultImg = createBufferedImage(bufImg);
        Graphics2D graphics = resultImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        //设置图片背景
        graphics.drawImage(bufImg, 0, 0, null);
        graphics.dispose();
        return resultImg;
    }



    private static class TwoAudioFrameMerger {
//        private String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=FL:sample_rates=48000[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=FR:sample_rates=48000[audio2];[audio1][audio2]amix=inputs=2[mix1];[mix1]atempo=1.08[a]";

        private String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=FL:sample_rates=48000[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=FR:sample_rates=48000[audio2];[audio1][audio2]amix=inputs=2:duration=shortest[a]";
        //        private String filterStr = "[0:a][1:a]amix=inputs=2[a]";
        private FFmpegFrameFilter filter;


        public TwoAudioFrameMerger() {
            try {
                filter = new FFmpegFrameFilter(filterStr, 1);
                filter.setSampleRate(48000);
                filter.setAudioInputs(2);
                filter.start();
            } catch (FrameFilter.Exception e) {
                throw new RuntimeException("FFmpegFrameFilter created error:" + e.getMessage());
            }
        }

        public Frame mergeFrame(Frame audioFrame, int channelNumber) {
            Frame resultFrame = null;
            try {
                TimeUtil timer = new TimeUtil();
                timer.start();
                filter.push(channelNumber - 1, audioFrame);
                resultFrame = filter.pullSamples();
                if (resultFrame != null) {
                    resultFrame = resultFrame.clone();
                    resultFrame.timestamp = audioFrame.timestamp;
                    timer.end();
                    System.out.println("AAAAAAA------mixed two audio frames..." + resultFrame.timestamp + " ,cost=" + timer.getTimeInMillSecond() + "毫秒");
                }
                return resultFrame;
            } catch (Exception e) {
                return null;
            }
        }
    }


    private static class TwoVideoFrameMerger {
        private Java2DFrameConverter converter = new Java2DFrameConverter();
        private Java2DFrameConverter converter2 = new Java2DFrameConverter();
        private BufferedImage previousBigImage = null;
        private BufferedImage previousSmallImage = null;
        private double scaleRation = 0.78;


        public Frame mergeFrame(long timestamp) {
            Frame resultFrame = null;
            if (null != previousBigImage && null != previousSmallImage) {
                converter = new Java2DFrameConverter();
                BufferedImage image1 = scaleImage(previousSmallImage, scaleRation, previousBigImage.getWidth(), previousBigImage.getHeight());
                BufferedImage imageb = copyImage(previousBigImage);
                BufferedImage image2 = combineTwoImagesToRightTop(image1, imageb);
                resultFrame = converter.convert(image2);
                resultFrame.timestamp = timestamp;
                System.out.println("-----------------           ------push one combined video frame（实时大小图像合成一帧）...." + timestamp);
                return resultFrame;
            }
            return resultFrame;
        }

        public void updateSmallImage(Frame frame2) {
            converter2 = new Java2DFrameConverter();
            if (null != frame2)
                previousSmallImage = converter2.getBufferedImage(frame2);
        }

        public void updateBigImage(Frame frame1) {
            converter = new Java2DFrameConverter();
            if (null != frame1)
                previousBigImage = converter.getBufferedImage(frame1);
        }


        //复制一个同样内容但缩放过的图像（按给定宽高）
        private BufferedImage scaleImage(BufferedImage bufImg, double ratio, int maxWidth, int maxHeight) {
            int width = (int) (maxWidth * ratio);
            int height = (int) (maxHeight * ratio);
//           return (BufferedImage) bufImg.getScaledInstance(width,height,Image.SCALE_FAST);
            BufferedImage resultImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D graphics = resultImg.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            //设置图片背景
            graphics.drawImage(bufImg, 0, 0, width, height, null);
            graphics.dispose();
            return resultImg;
        }

        //将小图像叠加到大图像上合成一个新图像返回
        private BufferedImage combineTwoImages(BufferedImage smallImage, BufferedImage bigImage) {
            Graphics2D graphics = bigImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            //叠加图像
            graphics.drawImage(smallImage, 0, 0, smallImage.getWidth(), smallImage.getHeight(), null);
            graphics.setColor(Color.RED);
            graphics.setStroke(new BasicStroke(15));
            graphics.draw3DRect(0, 0, smallImage.getWidth(), smallImage.getHeight(), true);
            graphics.dispose();
            return bigImage;
        }

        //将小图像叠加到大图像上合成一个新图像返回
        private BufferedImage combineTwoImagesToRightTop(BufferedImage smallImage, BufferedImage bigImage) {
            Graphics2D graphics = bigImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            //叠加图像
            int marginx = (int) (bigImage.getWidth() * 0.03);
            int marginy = (int) (bigImage.getHeight() * 0.03);
            int x = bigImage.getWidth() - smallImage.getWidth() - marginx;
            int y = marginy;
            graphics.drawImage(smallImage, x, y, smallImage.getWidth(), smallImage.getHeight(), null);
            graphics.setColor(Color.WHITE);
            graphics.setStroke(new BasicStroke((int) (bigImage.getWidth() * 0.03)));
            graphics.draw3DRect(x, y, smallImage.getWidth(), smallImage.getHeight(), true);
            graphics.dispose();
            return bigImage;
        }

        //复制一个同样内容的图像
        private BufferedImage copyImage(BufferedImage bufImg) {

            int width = bufImg.getWidth();
            int height = bufImg.getHeight();
            int type = bufImg.getType();
            System.out.println("----------------------------------------------------- copy bufferedimage type:" + type);
            BufferedImage resultImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//            BufferedImage resultImg = createBufferedImage(bufImg);
            Graphics2D graphics = resultImg.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            //设置图片背景
            graphics.drawImage(bufImg, 0, 0, null);
            graphics.dispose();
            return resultImg;
        }

        private BufferedImage createBufferedImage(BufferedImage src) {
            ColorModel cm = src.getColorModel();
            BufferedImage image = new BufferedImage(
                    cm,
                    cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
                    cm.isAlphaPremultiplied(),
                    null);
            return image;
        }


        private BufferedImage addSubtitle(BufferedImage bufImg, String subTitleContent) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 添加字幕时的时间
            Font font = new Font("微软雅黑", Font.BOLD, 18);
            String timeContent = sdf.format(new Date());
            FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
            BufferedImage resultImg = bufImg;
            Graphics2D graphics = resultImg.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

            //设置图片背景
//            graphics.drawImage(previousSmallImage, 0, 0, 100,100, null);
            //设置左上方时间显示
            graphics.setColor(Color.BLUE);
            graphics.setFont(font);
            graphics.drawString(timeContent, 0, metrics.getAscent());

            // 计算文字长度，计算居中的x点坐标
//        int textWidth = metrics.stringWidth(subTitleContent);
//        int widthX = (bufImg.getWidth() - textWidth) / 2;
//        graphics.setColor(Color.red);
//        graphics.setFont(font);
//        graphics.drawString(subTitleContent, widthX, bufImg.getHeight() - 100);
            graphics.dispose();
            return resultImg;
        }


        public BufferedImage toBufferedImage(Image image) {
            if (image instanceof BufferedImage) {
                return (BufferedImage) image;
            }

            // 此代码确保在图像的所有像素被载入
            image = new ImageIcon(image).getImage();
            // 如果图像有透明用这个方法
//		boolean hasAlpha = hasAlpha(image);

            // 创建一个可以在屏幕上共存的格式的bufferedimage
            BufferedImage bimage = null;
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            try {
                //确定新的缓冲图像类型的透明度
                int transparency = Transparency.OPAQUE;
                //if (hasAlpha) {
                transparency = Transparency.BITMASK;
                //}

                // 创造一个bufferedimage
                GraphicsDevice gs = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gs.getDefaultConfiguration();
                bimage = gc.createCompatibleImage(
                        image.getWidth(null), image.getHeight(null), transparency);
            } catch (HeadlessException e) {
                // 系统不会有一个屏幕
            }

            if (bimage == null) {
                // 创建一个默认色彩的bufferedimage
                int type = BufferedImage.TYPE_INT_RGB;
                //int type = BufferedImage.TYPE_3BYTE_BGR;//by wang
                //if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
                //}
                bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
            }

            // 把图像复制到bufferedimage上
            Graphics g = bimage.createGraphics();

            // 把图像画到bufferedimage上
            g.drawImage(image, 0, 0, null);
            g.dispose();

            return bimage;
        }


    }


}
