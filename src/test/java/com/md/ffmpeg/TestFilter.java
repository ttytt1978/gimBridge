package com.md.ffmpeg;

import com.md.api.IFrameTaker;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;
import sun.font.FontDesignMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TestFilter {

    public static void main(String[] args) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        testTwoFrameFilter();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class AudioFrameFormat {
        private FFmpegFrameFilter filter;

        public AudioFrameFormat() {
            String filterStr = "aformat=sample_fmts=s16:channel_layouts=stereo:sample_rates=48000[a1];[a1]anull";
            filter = new FFmpegFrameFilter(filterStr, 1);
//            filter.setAudioInputs(1);
            try {
                filter.start();
            } catch (FrameFilter.Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        public Frame format(Frame audioFrame) {
            try {
                filter.push(audioFrame);
                Frame mixedAudioFrame = filter.pullSamples();
                return mixedAudioFrame;
            } catch (FrameFilter.Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }



    public static void testTwoFrameFilter() throws Exception {
        int audioChannels = 1;
        int sampleRate = 48000;
        IFrameTaker taker1 = new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11", 200);
        IFrameTaker taker2 = new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12", 200);
        taker1.start();
        taker2.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 320, 240);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(8 * 1024 * 1024);
        // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
        recorder.setAudioOption("crf", "0");
        // 最高质量
        recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(8 * 128 * 1024);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(1);
        recorder.start();
        int k = 0;
        //直接推送，但是修改帧时间戳为新时间戳
        long orginStartTimeStamp = -1, newStartTimeStamp = -1, orginLastTimeStamp = 0, realLastTimeStamp = 0;
        Random random = new Random();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        Java2DFrameConverter converter2 = new Java2DFrameConverter();
        BufferedImage bigImage = null, smallImage = null;
//        String filterStr = "[0:a][1:a]amix=inputs=2[a]";
//        String filterStr = "[0:a][1:a]amerge=inputs=2[a]";
//        String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=FL:sample_rates=48000[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=FR:sample_rates=48000[audio2];[audio1][audio2]amix=inputs=2[mix1];[mix1]atempo=1.0[a]";
        String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000,asetpts=STARTPTS[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000,asetpts=STARTPTS[audio2];[audio1][audio2]amix=inputs=2[mix1];[mix1]anull[a]";

//        FFmpegFrameFilter videoFilter = new FFmpegFrameFilter("scale=800:600,transpose=1,transpose=1", 600, 400);

//        FFmpegFrameFilter videoFilter = new FFmpegFrameFilter("scale=320:240,transpose=1,hflip,format=pix_fmts=yuv420p,noise=alls=20:allf=t+u", 600, 400);
//        String vstr="nullsrc=size=200x100 [background];[0:v]scale=100x100 [left];[1:v]scale=100x100 [right];[background][left] overlay=shortest=1 [background+left];[background+left][right] overlay=shortest=1:x=100 [left+right];[left+right]scale=300:200[v]";
        String vstr="[0:v]format=pix_fmts=rgb24,scale=300:200[base];[1:v]format=pix_fmts=rgb24,scale=100:60[top];[base][top]overlay=5:5[mix1];[mix1]null[v]";
//        String vstr="[0:v][1:v]hstack=inputs=2[mix1];[mix1]scale=300:200[v]";
//        String vstr="[0:v][1:v]hstack=inputs=2[mix1];[mix1]scale=300:200[v]";
        FFmpegFrameFilter videoFilter = new FFmpegFrameFilter(vstr, 300, 200);
        videoFilter.setVideoInputs(2);
//        videoFilter.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
//        videoFilter.setPixelFormat(grabber.getPixelFormat());
        videoFilter.start();
        FFmpegFrameFilter filter = new FFmpegFrameFilter(filterStr, 1);
        filter.setAudioInputs(2);
        filter.start();
        String filterStra = "aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000,anull";
        FFmpegFrameFilter filter3 = new FFmpegFrameFilter(filterStra, 1);
        filter3.start();
        AudioFrameFormat frameFormat = new AudioFrameFormat();
        Frame frame = null, frame2 = null;
        TwoVideoFrameMerger merger = new TwoVideoFrameMerger();
        while (true) {
            if (frame == null) {
                frame = taker1.takeFirstFrame();
                if (frame == null) {
//                    System.out.println("---------------no frame grabbed..." );
                    continue;
                }
            }

            if (frame2 == null) {
                frame2 = taker2.takeFirstFrame();
                if (frame2 == null) {
//                    System.out.println("---------------no frame2 grabbed..." );
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
                    recorder.setTimestamp(frame.timestamp);
                    recorder.record(mixedFrame);
                    System.out.println("VVVV111---------------mixed two video frames..." );
                }
            }

            if(frame2.image!=null)
            {
                Frame mixedFrame=frame2.clone();
                videoFilter.push(1,mixedFrame);
//                mixedFrame=videoFilter.pullImage();
//                if(mixedFrame!=null)
//                {
//                    mixedFrame=mixedFrame.clone();
//                    mixedFrame.timestamp=frame.timestamp;
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.record(mixedFrame);
//                    System.out.println("VVVV222---------------mixed two video frames..." );
//                }
            }


            if (frame.samples != null) {
//                recorder.setTimestamp(frame.timestamp);
//                recorder.recordSamples(frame.samples);
//                System.out.println("AAAAAAA1111---------------mixed two audio frames..." );
                frame=frame.clone();
                filter.push(0, frame);
                Frame mixedAudioFrame = filter.pullSamples();

                if (mixedAudioFrame != null) {
                    mixedAudioFrame=mixedAudioFrame.clone();
                    recorder.setTimestamp(frame.timestamp);
                    recorder.recordSamples(mixedAudioFrame.samples);
                    System.out.println("AAAAAAA1111---------------mixed two audio frames...");
                }
            }
            if (frame2.samples != null) {
                frame2=frame2.clone();
                frame2.timestamp=frame.timestamp;
                filter.push(1, frame2);
                Frame mixedAudioFrame = filter.pullSamples();
                if (mixedAudioFrame != null) {
                    mixedAudioFrame=mixedAudioFrame.clone();
                    recorder.setTimestamp(frame.timestamp);
                    recorder.recordSamples(mixedAudioFrame.samples);
                    System.out.println("AAAAAAA2222---------------mixed two audio frames...");
                }


            }

//            if (frame.image != null) {
//                merger.updateBigImage(frame);
//            }
//            if (frame2.image != null) {
//                merger.updateSmallImage(frame2);
//            }
//            Frame mergedVideoFrame = null;
//            if (frame.image != null || frame2.image != null) {
//                try {
//                    mergedVideoFrame = merger.mergeFrame(frame.timestamp);
//                } catch (Exception e) {
//                }
//                if (mergedVideoFrame != null) {
//                    recorder.setTimestamp(frame.timestamp);
//                    recorder.record(mergedVideoFrame);
//                    System.out.println("VVVV---------------mixed two video frames...");
//                }
//            }



            frame = null;
            frame2 = null;

            k++;
            if (k > Integer.MAX_VALUE)
                break;

        }
        taker1.stop();
        taker2.stop();
        recorder.stop();
        recorder.release();
    }



    public static void testTwoAudioFilter() throws Exception {
        int audioChannels = 1;
        int sampleRate = 48000;
        IFrameTaker taker1 = new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream11", 200);
        IFrameTaker taker2 = new BasicFrameTaker("rtmp://127.0.0.1/oflaDemo/stream12", 200);
        taker1.start();
        taker2.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://127.0.0.1/oflaDemo/stream19", 320, 240);
        recorder.setInterleaved(true);
        recorder.setFormat("flv");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(20);
        recorder.setVideoBitrate(8 * 1024 * 1024);
        // 不可变(固定)音频比特率
//        int sampleRate=44100;
//        int audioChannels=4;
        recorder.setAudioOption("crf", "0");
        // 最高质量
        recorder.setAudioQuality(0);
        // 音频比特率
        recorder.setAudioBitrate(8 * 128 * 1024);
        // 音频采样率
        recorder.setSampleRate(sampleRate);
        // 双通道(立体声)
        recorder.setAudioChannels(1);
        recorder.start();
        int k = 0;
        //直接推送，但是修改帧时间戳为新时间戳
        long orginStartTimeStamp = -1, newStartTimeStamp = -1, orginLastTimeStamp = 0, realLastTimeStamp = 0;
        Random random = new Random();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        Java2DFrameConverter converter2 = new Java2DFrameConverter();
        BufferedImage bigImage = null, smallImage = null;
//        String filterStr = "[0:a][1:a]amix=inputs=2[a]";
//        String filterStr = "[0:a][1:a]amerge=inputs=2[a]";
//        String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=FL:sample_rates=48000[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=FR:sample_rates=48000[audio2];[audio1][audio2]amix=inputs=2[mix1];[mix1]atempo=1.0[a]";
        String filterStr = "[0:a]aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000,asetpts=STARTPTS[audio1];[1:a]aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000,asetpts=STARTPTS[audio2];[audio1][audio2]amix=inputs=2[mix1];[mix1]anull[a]";

        FFmpegFrameFilter filter2 = new FFmpegFrameFilter("scale=800:600,transpose=1,transpose=1", 600, 400);
        filter2.start();
        FFmpegFrameFilter filter = new FFmpegFrameFilter(filterStr, 1);
        filter.setAudioInputs(2);
        filter.start();
        String filterStra = "aformat=sample_fmts=s16:channel_layouts=mono:sample_rates=48000,anull";
        FFmpegFrameFilter filter3 = new FFmpegFrameFilter(filterStra, 1);
        filter3.start();
        AudioFrameFormat frameFormat = new AudioFrameFormat();
        Frame frame = null, frame2 = null;
        TwoVideoFrameMerger merger = new TwoVideoFrameMerger();
        while (true) {
            if (frame == null) {
                frame = taker1.takeFirstFrame();
                if (frame == null) {
//                    System.out.println("---------------no frame grabbed..." );
                    continue;
                }
            }

            if (frame2 == null) {
                frame2 = taker2.takeFirstFrame();
                if (frame2 == null) {
//                    System.out.println("---------------no frame2 grabbed..." );
                    continue;
                }
            }


            if (frame.samples != null) {
//                recorder.setTimestamp(frame.timestamp);
//                recorder.recordSamples(frame.samples);
//                System.out.println("AAAAAAA1111---------------mixed two audio frames..." );
                frame=frame.clone();
                filter.push(0, frame);
                Frame mixedAudioFrame = filter.pullSamples();

                    if (mixedAudioFrame != null) {
                        mixedAudioFrame=mixedAudioFrame.clone();
                        recorder.setTimestamp(frame.timestamp);
                        recorder.recordSamples(mixedAudioFrame.samples);
                        System.out.println("AAAAAAA1111---------------mixed two audio frames...");
                    }
            }
            if (frame2.samples != null) {
                frame2=frame2.clone();
                frame2.timestamp=frame.timestamp;
                filter.push(1, frame2);
                Frame mixedAudioFrame = filter.pullSamples();
                    if (mixedAudioFrame != null) {
                        mixedAudioFrame=mixedAudioFrame.clone();
                        recorder.setTimestamp(frame.timestamp);
                        recorder.recordSamples(mixedAudioFrame.samples);
                        System.out.println("AAAAAAA2222---------------mixed two audio frames...");
                    }


            }

            if (frame.image != null) {
                merger.updateBigImage(frame);
            }
            if (frame2.image != null) {
                merger.updateSmallImage(frame2);
            }
            Frame mergedVideoFrame = null;
            if (frame.image != null || frame2.image != null) {
                try {
                    mergedVideoFrame = merger.mergeFrame(frame.timestamp);
                } catch (Exception e) {
                }
                if (mergedVideoFrame != null) {
                    recorder.setTimestamp(frame.timestamp);
                    recorder.record(mergedVideoFrame);
                    System.out.println("VVVV---------------mixed two video frames...");
                }
            }

//            if(frame.image!=null)
//            {
//                recorder.setTimestamp(frame.timestamp);
//                recorder.record(frame);
//                System.out.println("VVVV---------------mixed two video frames..." );
//            }

            frame = null;
            frame2 = null;

            k++;
            if (k > Integer.MAX_VALUE)
                break;

        }
        taker1.stop();
        taker2.stop();
        recorder.stop();
        recorder.release();
    }


    private static BufferedImage addSubtitle(BufferedImage bufImg, String subTitleContent) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 添加字幕时的时间
        Font font = new Font("微软雅黑", Font.BOLD, 12);
        String timeContent = sdf.format(new Date());
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        BufferedImage resultImg = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = resultImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        //设置图片背景
        graphics.drawImage(bufImg, 0, 0, null);
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
        int marginx = (int) (bigImage.getWidth() * 0.05);
        int marginy = (int) (bigImage.getHeight() * 0.05);
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
    private static BufferedImage copyImage(BufferedImage bufImg) {

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


    private static class TwoVideoFrameMerger {
        private Java2DFrameConverter converter = new Java2DFrameConverter();
        private Java2DFrameConverter converter2 = new Java2DFrameConverter();
        private BufferedImage previousBigImage = null;
        private BufferedImage previousSmallImage = null;
        private double scaleRation = 0.76;


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
