package com.md.ffmpeg;

import com.md.api.IFrameMerger;
import com.md.api.IFrameTaker;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
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

public class TestFFMpegFilter {


    public static void main(String[] args) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        testVideoFilter0();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testVideoFilter0()throws Exception
    {
        int audioChannels=1;
        int sampleRate=48000;
        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
        grabber.setAudioChannels(audioChannels);
        grabber.setSampleRate(sampleRate);
//        grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber.start();
        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
        grabber2.setAudioChannels(audioChannels);
        grabber2.setSampleRate(sampleRate);
//        grabber2.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber2.start();



        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 400, 300);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(8*1024*1024);
        // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
        recorder.setAudioOption("crf", "0");
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
        String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=FL:sample_rates=48000[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=FR:sample_rates=48000[audio2];[audio1][audio2]amix=inputs=2[mix1];[mix1]atempo=1.1[a]";
//        FFmpegFrameFilter filter2 =new FFmpegFrameFilter("scale=800:600,transpose=1,transpose=1",600,400);
                String vstr="nullsrc=size=200x100 [background];[0:v]scale=100x100 [left];[1:v]scale=100x100 [right];[background][left] overlay=shortest=1 [background+left];[background+left][right] overlay=shortest=1:x=100 [left+right];[left+right]scale=300:200[v]";
//        String vstr="[0:v]format=pix_fmts=rgb24,scale=300:200[base];[1:v]format=pix_fmts=rgb24,scale=100:60[top];[base][top]overlay=5:5[mix1];[mix1]null[v]";
//        String vstr="[0:v][1:v]hstack=inputs=2[mix1];[mix1]scale=300:200[v]";
//        String vstr="[0:v][1:v]hstack=inputs=2[mix1];[mix1]scale=300:200[v]";
        FFmpegFrameFilter videoFilter = new FFmpegFrameFilter(vstr, 300, 200);
//        videoFilter.setPixelFormat(grabber.getPixelFormat());
        videoFilter.setVideoInputs(2);
        videoFilter.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//        videoFilter.setPixelFormat(grabber.getPixelFormat());
        videoFilter.start();
        FFmpegFrameFilter filter =new FFmpegFrameFilter(filterStr,audioChannels);
        filter.setAudioInputs(2);
        filter.start();
        Frame frame=null,frame2=null;
        TwoVideoFrameMerger merger = new TwoVideoFrameMerger();
        while(true)
        {
            if(frame==null)
            {
                frame=grabber.grab();
                if(frame==null)
                {
//                    System.out.println("---------------no frame grabbed..." );
                    continue;
                }
            }

            if(frame.image!=null)
            {
                Frame mixedFrame=frame.clone();
                videoFilter.push(0,mixedFrame);
                mixedFrame=videoFilter.pullImage();
                if(mixedFrame!=null)
                {
                    mixedFrame=mixedFrame.clone();
                    mixedFrame.timestamp=frame.timestamp;
                    recorder.setTimestamp(recorder.getTimestamp());
                    recorder.record(mixedFrame);
                    System.out.println("VVVV111---------------mixed two video frames..." );
                }
            }



            if(frame2==null)
            {
                frame2=grabber2.grab();
                if(frame2==null)
                {
//                    System.out.println("---------------no frame2 grabbed..." );
                    continue;
                }
            }

//            if(frame.samples!=null)
//            {
//                filter.push(0,frame);
//                Frame mixedAudioFrame=filter.pullSamples();
//                if(mixedAudioFrame!=null)
//                {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.recordSamples(mixedAudioFrame.samples);
//                    System.out.println("AAAAAAA111---------------mixed two audio frames..." );
//                }
//            }
//            if(frame2.samples!=null)
//            {
//                filter.push(1,frame2);
//                Frame mixedAudioFrame=filter.pullSamples();
//                if(mixedAudioFrame!=null)
//                {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.recordSamples(mixedAudioFrame.samples);
//                    System.out.println("AAAAAAA222---------------mixed two audio frames..." );
//                }
//            }

            if(frame2.image!=null)
            {
                Frame mixedFrame=frame2.clone();
                videoFilter.push(1,mixedFrame);
                mixedFrame=videoFilter.pullImage();
                if(mixedFrame!=null)
                {
                    mixedFrame=mixedFrame.clone();
                    mixedFrame.timestamp=frame.timestamp;
                    recorder.setTimestamp(recorder.getTimestamp());
                    recorder.record(mixedFrame);
                    System.out.println("VVVV222---------------mixed two video frames..." );
                }
            }


//            if(frame.image!=null)
//            {
//                merger.updateBigImage(frame);
//            }
//            if(frame2.image!=null)
//            {
//                merger.updateSmallImage(frame2);
//            }
//            Frame mergedVideoFrame=null;
//            if(frame.image!=null||frame2.image!=null)
//            {
//                try {
//                    mergedVideoFrame = merger.mergeFrame(frame.timestamp);
//                } catch (Exception e) {
//                }
//                if(mergedVideoFrame!=null)
//                {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.record(mergedVideoFrame);
//                    System.out.println("VVVV---------------mixed two video frames..." );
//                }
//            }

            frame=null;
            frame2=null;

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



    public static void testVideoFilter1()throws Exception
    {
        int audioChannels=1;
        int sampleRate=48000;
        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
        grabber.setAudioChannels(audioChannels);
        grabber.setSampleRate(sampleRate);
//        grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber.start();
        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
        grabber2.setAudioChannels(audioChannels);
        grabber2.setSampleRate(sampleRate);
//        grabber2.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber2.start();

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
        recorder.setAudioOption("crf", "0");
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
        String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=FL:sample_rates=48000[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=FR:sample_rates=48000[audio2];[audio1][audio2]amix=inputs=2[a]";
//        FFmpegFrameFilter filter2 =new FFmpegFrameFilter("scale=800:600,transpose=1,transpose=1",600,400);
        FFmpegFrameFilter filter2 =new FFmpegFrameFilter("scale=600:400",600,400);
        filter2.start();
        FFmpegFrameFilter filter =new FFmpegFrameFilter(filterStr,audioChannels);
        filter.setAudioInputs(2);
        filter.start();

        while(true)
        {
            Frame frame=grabber.grab();
            if(frame==null)
            {
//                System.out.println("---------------no frame grabbed..." );
                continue;
            }
            Frame frame2=grabber2.grab();
            if(frame2==null)
            {
//                System.out.println("---------------no frame2 grabbed..." );
                continue;
            }


            if(frame.samples!=null)
            {
                filter.push(0,frame);
                Frame mixedAudioFrame=filter.pullSamples();
                if(mixedAudioFrame!=null)
                {
                    recorder.setTimestamp(frame.timestamp);
                    recorder.recordSamples(mixedAudioFrame.samples);
                    System.out.println("AAAAAAA111---------------mixed two audio frames..." );
                }
            }
            if(frame2.samples!=null)
            {
                filter.push(1,frame2);
                Frame mixedAudioFrame=filter.pullSamples();
                if(mixedAudioFrame!=null)
                {
                    recorder.setTimestamp(frame.timestamp);
                    recorder.recordSamples(mixedAudioFrame.samples);
                    System.out.println("AAAAAAA222---------------mixed two audio frames..." );
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


    public static void testVideoFilter2()throws Exception
    {
        int audioChannels=4;
        int sampleRate=48000;
        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
        grabber.setAudioChannels(audioChannels);
        grabber.setSampleRate(sampleRate);
//        grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber.start();
        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
        grabber2.setAudioChannels(audioChannels);
        grabber2.setSampleRate(sampleRate);
//        grabber2.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber2.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream18", 320, 240);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(8*1024*1024);
        // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(8*128*1024);
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
        String filterStr = "[0:a][1:a]amix=inputs=2[a]";
        String filterStr8 = "[0:a][1:a]amerge[a]";
        String fileterStr2="nullsrc=size=640x480 [base];[0:v] setpts=PTS-STARTPTS, scale=320x240 [upperleft];[1:v] setpts=PTS-STARTPTS, scale=320x240 [upperright];[2:v] setpts=PTS-STARTPTS, scale=320x240 [lowerleft];[3:v] setpts=PTS-STARTPTS, scale=320x240 [lowerright];[base][upperleft] overlay=shortest=1 [tmp1];[tmp1][upperright] overlay=shortest=1:x=320 [tmp2];[tmp2][lowerleft] overlay=shortest=1:y=240 [tmp3];[tmp3][lowerright] overlay=shortest=1:x=320:y=240";

        String filterStr2="nullsrc=size=640x480 [base];scale=iw*2:ih[p1];[base][p1]overlay=30:30";
        String filterStr3="[0:v] setpts=PTS-STARTPTS, scale=600:400[a];[1:v] setpts=PTS-STARTPTS, scale=250*200[b];[a][b]overlay=(main_w-overlay_w):(main_h-overlay_h-100)";
        String filterStr5="scale=400x300,transpose=1,transpose=1";

        String filterStr6="[0:v]scale=800x600[base];[1:v]scale=iw/2:ih/2[pip];[base][pip]overlay=main_w-overlay_w-10:main_h-overlay_h-10[v]";
        FFmpegFrameFilter filter2 =new FFmpegFrameFilter(filterStr6,800,600);
        filter2.setVideoInputs(2);
        filter2.start();
        FFmpegFrameFilter filter =new FFmpegFrameFilter(filterStr,audioChannels);
        filter.setAudioInputs(2);
        filter.start();
        Frame bigFrame=null,smallFrame=null;
        long lastTimestamp=-1;
        while(true)
        {
            Frame frame=grabber.grab();
            if(frame==null)
            {
                System.out.println("---------------no frame grabbed..." );
                continue;
            }
            Frame frame2=grabber2.grab();
            if(frame2==null)
            {
                System.out.println("---------------no frame2 grabbed..." );
                continue;
            }
            lastTimestamp=frame.timestamp;
            boolean haveVideoUpdate=false;

            if(frame.samples!=null)
            {
                filter.push(0,frame);
                Frame mixedAudioFrame=filter.pullSamples();
                if(mixedAudioFrame!=null)
                {
                    recorder.setTimestamp(frame.timestamp);
                    recorder.recordSamples(mixedAudioFrame.samples);
                    System.out.println("AAAAAAA---------------mixed two audio frames..." );
                }
            }
            if(frame2.samples!=null)
            {
                filter.push(1,frame2);
                Frame mixedAudioFrame=filter.pullSamples();
                if(mixedAudioFrame!=null)
                {
                    recorder.setTimestamp(frame.timestamp);
                    recorder.recordSamples(mixedAudioFrame.samples);
                    System.out.println("AAAAAAA---------------mixed two audio frames..." );
                }
            }
            if(frame.image!=null)
            {
                bigFrame=frame;
            }
            if(frame2.image!=null)
            {
                smallFrame=frame2;
            }
            if(bigFrame!=null&&smallFrame!=null)
            {
                filter2.push(0,bigFrame);
                filter2.push(1,smallFrame);
                bigFrame=null;
                smallFrame=null;
                Frame mergedFrame=filter2.pullImage();
                if(mergedFrame!=null)
                {
                    recorder.setTimestamp(lastTimestamp);
                    recorder.record(mergedFrame);
                    System.out.println("VVVV---------------mixed two video frames..." );
                }
            }

//            if(frame.image!=null)
//            {
////                filter2.push(0,frame2);
////                filter2.push(1,frame);
//                filter2.push(frame);
//                Frame mergedFrame=filter2.pullImage();
//                if(mergedFrame!=null)
//                {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.record(mergedFrame);
//                    System.out.println("VVVV---------------mixed two video frames..." );
//                }
//
//            }
//
//            if(frame2.image!=null)
//            {
////                filter2.push(0,frame2);
////                filter2.push(1,frame);
//                filter2.push(frame2);
//                Frame mergedFrame=filter2.pullImage();
//                if(mergedFrame!=null)
//                {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.record(mergedFrame);
//                    System.out.println("VVVV---------------mixed two video frames..." );
//                }
//
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


    public static void testVideoFilter3()throws Exception
    {
        int audioChannels=4;
        int sampleRate=48000;
        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
        grabber.setAudioChannels(audioChannels);
        grabber.setSampleRate(sampleRate);
//        grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber.start();
        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
        grabber2.setAudioChannels(audioChannels);
        grabber2.setSampleRate(sampleRate);
//        grabber2.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
        grabber2.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream16", 600, 400);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(8*1024*1024);
        // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(8*128*1024);
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
        String filterStr = "[0:a][1:a]amix=inputs=2[a]";
//        String filterStr = "[0:a][1:a]amerge=inputs=2[a]";
        String fileterStr2="nullsrc=size=640x480 [base];[0:v] setpts=PTS-STARTPTS, scale=320x240 [upperleft];[1:v] setpts=PTS-STARTPTS, scale=320x240 [upperright];[2:v] setpts=PTS-STARTPTS, scale=320x240 [lowerleft];[3:v] setpts=PTS-STARTPTS, scale=320x240 [lowerright];[base][upperleft] overlay=shortest=1 [tmp1];[tmp1][upperright] overlay=shortest=1:x=320 [tmp2];[tmp2][lowerleft] overlay=shortest=1:y=240 [tmp3];[tmp3][lowerright] overlay=shortest=1:x=320:y=240";

        String filterStr2="nullsrc=size=640x480 [base];scale=iw*2:ih[p1];[base][p1]overlay=30:30";
        String filterStr3="[0:v] setpts=PTS-STARTPTS, scale=600:400[a];[1:v] setpts=PTS-STARTPTS, scale=250*200[b];[a][b]overlay=(main_w-overlay_w):(main_h-overlay_h-100)[v]";
        String filterStr5="scale=400x300,transpose=1,transpose=1";

        String filterStr6="[0:v] setpts=PTS-STARTPTS,scale=800x600[base];[1:v] setpts=PTS-STARTPTS,scale=iw/2:ih/2[pip];[base][pip]overlay=main_w-overlay_w-10:main_h-overlay_h-10[v]";

        String filterStr7="[0:v] setpts=PTS-STARTPTS,scale=600x400[base];[1:v]setpts=PTS-STARTPTS,scale=200x100[pip];[base][pip]overlay=main_w-overlay_w-10:main_h-overlay_h-10[v]";

        FFmpegFrameFilter filter =new FFmpegFrameFilter(filterStr7,filterStr,600,400,audioChannels);
        filter.setVideoInputs(2);
        filter.setAudioInputs(2);
        filter.start();
        Frame bigFrame=null,smallFrame=null;
        long lastTimestamp=-1;
        while(true)
        {
            Frame frame=grabber.grab();
            if(frame==null)
            {
                System.out.println("---------------no frame grabbed..." );
                continue;
            }
            Frame frame2=grabber2.grab();
            if(frame2==null)
            {
                System.out.println("---------------no frame2 grabbed..." );
                continue;
            }
            lastTimestamp=frame.timestamp;
            {
                filter.push(0,frame);
                filter.push(1,frame2);
                bigFrame=null;
                smallFrame=null;
                Frame mergedFrame=filter.pull();
                if(mergedFrame!=null)
                {
                    recorder.setTimestamp(lastTimestamp);
                    recorder.record(mergedFrame);
                    System.out.println("VVVV---------------mixed two frames..." +lastTimestamp);
                }
            }
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

    public static void testVideoFilter5()throws Exception
    {
        IFrameTaker taker1=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11",200);
        IFrameTaker taker2=new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12",200);
        IFrameMerger merger=new TwoStreamFrameMergerForMp4(100);
        merger.setFrameTakers(taker1,taker2);
        merger.start();
        int audioChannels=1;
        int sampleRate=48000;
//        FFmpegFrameGrabber grabber=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream11");
//        grabber.setAudioChannels(audioChannels);
//        grabber.setSampleRate(sampleRate);
////        grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
//        grabber.start();
//        FFmpegFrameGrabber grabber2=new FFmpegFrameGrabber("rtmp://127.0.0.1/oflaDemo/stream12");
//        grabber2.setAudioChannels(audioChannels);
//        grabber2.setSampleRate(sampleRate);
////        grabber2.setSampleFormat(avutil.AV_SAMPLE_FMT_FLTP);
//        grabber2.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream16", 320, 240);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(8*1024*1024);
        // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
        recorder.setAudioOption("crf", "0");
        // 最高质量
//            recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(8*128*1024);
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
        String filterStr = "[0:a][1:a]amix=inputs=2[a]";
        String filterStr8 = "[0:a][1:a]amerge[a]";
        String fileterStr2="nullsrc=size=640x480 [base];[0:v] setpts=PTS-STARTPTS, scale=320x240 [upperleft];[1:v] setpts=PTS-STARTPTS, scale=320x240 [upperright];[2:v] setpts=PTS-STARTPTS, scale=320x240 [lowerleft];[3:v] setpts=PTS-STARTPTS, scale=320x240 [lowerright];[base][upperleft] overlay=shortest=1 [tmp1];[tmp1][upperright] overlay=shortest=1:x=320 [tmp2];[tmp2][lowerleft] overlay=shortest=1:y=240 [tmp3];[tmp3][lowerright] overlay=shortest=1:x=320:y=240";

        String filterStr2="nullsrc=size=640x480 [base];scale=iw*2:ih[p1];[base][p1]overlay=30:30";
        String filterStr3="[0:v] setpts=PTS-STARTPTS, scale=600:400[a];[1:v] setpts=PTS-STARTPTS, scale=250*200[b];[a][b]overlay=(main_w-overlay_w):(main_h-overlay_h-100)";
        String filterStr5="scale=400x300,transpose=1,transpose=1";

        String filterStr6="[0:v]scale=800x600[base];[1:v]scale=iw/2:ih/2[pip];[base][pip]overlay=main_w-overlay_w-10:main_h-overlay_h-10[v]";
        FFmpegFrameFilter filter2 =new FFmpegFrameFilter(filterStr6,800,600);
        filter2.setVideoInputs(2);
        filter2.start();
        FFmpegFrameFilter filter =new FFmpegFrameFilter(filterStr,audioChannels);
        filter.setAudioInputs(2);
        filter.start();
        Frame bigFrame=null,smallFrame=null;
        long lastTimestamp=-1;

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
            if(k>=Integer.MAX_VALUE)
                break;
        }

//        while(true)
//        {
//            Frame frame=taker1.takeFirstFrame();
//            if(frame==null)
//            {
//                System.out.println("---------------no frame grabbed..." );
//                continue;
//            }
//            Frame frame2=taker2.takeFirstFrame();
//            if(frame2==null)
//            {
//                System.out.println("---------------no frame2 grabbed..." );
//                continue;
//            }
//            lastTimestamp=frame.timestamp;
//            boolean haveVideoUpdate=false;
//
//            if(frame.samples!=null)
//            {
//                filter.push(0,frame);
//                Frame mixedAudioFrame=filter.pullSamples();
//                if(mixedAudioFrame!=null)
//                {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.recordSamples(mixedAudioFrame.samples);
//                    System.out.println("AAAAAAA---------------mixed two audio frames..." );
//                }
//            }
//            if(frame2.samples!=null)
//            {
//                filter.push(1,frame2);
//                Frame mixedAudioFrame=filter.pullSamples();
//                if(mixedAudioFrame!=null)
//                {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.recordSamples(mixedAudioFrame.samples);
//                    System.out.println("AAAAAAA---------------mixed two audio frames..." );
//                }
//            }
//            if(frame.image!=null)
//            {
//                bigFrame=frame;
//            }
//            if(frame2.image!=null)
//            {
//                smallFrame=frame2;
//            }
//            if(bigFrame!=null&&smallFrame!=null)
//            {
//                filter2.push(0,bigFrame);
//                filter2.push(1,smallFrame);
//                bigFrame=null;
//                smallFrame=null;
//                Frame mergedFrame=filter2.pullImage();
//                if(mergedFrame!=null)
//                {
//                    recorder.setTimestamp(lastTimestamp);
//                    recorder.record(mergedFrame);
//                    System.out.println("VVVV---------------mixed two video frames..." );
//                }
//            }
//
//            k++;
//            if(k>Integer.MAX_VALUE)
//                break;
//
//        }
        merger.stop();
        recorder.stop();
        recorder.release();
    }



    private static class TwoVideoFrameMerger {
        private Java2DFrameConverter converter = new Java2DFrameConverter();
        private Java2DFrameConverter converter2 = new Java2DFrameConverter();
        private BufferedImage previousBigImage = null;
        private BufferedImage previousSmallImage = null;
        private double scaleRation = 0.76;


        public Frame mergeFrame(long timestamp) {
            Frame resultFrame = null;
            if(null!=previousBigImage&&null!=previousSmallImage)
            {
                converter = new Java2DFrameConverter();
                BufferedImage image1=scaleImage(previousSmallImage,scaleRation,previousBigImage.getWidth(),previousBigImage.getHeight());
                BufferedImage imageb=copyImage(previousBigImage);
                BufferedImage image2=combineTwoImagesToRightTop(image1,imageb);
                resultFrame = converter.convert(image2);
                resultFrame.timestamp = timestamp;
                System.out.println("-----------------           ------push one combined video frame（实时大小图像合成一帧）...." + timestamp);
                return  resultFrame;
            }
            return resultFrame;
        }

        public void updateSmallImage(Frame frame2)
        {
            converter2 = new Java2DFrameConverter();
            if (null != frame2)
                previousSmallImage = converter2.getBufferedImage(frame2);
        }

        public void updateBigImage(Frame frame1)
        {
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
            graphics.draw3DRect(0,0,smallImage.getWidth(),smallImage.getHeight(),true);
            graphics.dispose();
            return bigImage;
        }

        //将小图像叠加到大图像上合成一个新图像返回
        private  BufferedImage combineTwoImagesToRightTop(BufferedImage smallImage, BufferedImage bigImage) {
            Graphics2D graphics = bigImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            //叠加图像
            int marginx=(int)(bigImage.getWidth()*0.03);
            int marginy=(int)(bigImage.getHeight()*0.03);
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
        private BufferedImage copyImage(BufferedImage bufImg) {

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
