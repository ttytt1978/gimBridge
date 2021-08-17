package com.md.red5;

import com.md.api.IFrameTaker;
import com.md.ffmpeg.BasicFrameTaker;
import com.md.util.TimeUtil;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyPusher implements IPusher {

    private String originPath;
    private String rtmpPath;
    private boolean exit=true;

    public MyPusher(String originPath, String rtmpPath) {
        this.originPath = originPath;
        this.rtmpPath = rtmpPath;
    }

    @Override
    public void start()
    {
        if(exit==false)
            return;
        exit=false;
        Thread worker=new Thread(() -> {
//            pushVideoAndAudio(mp4FileName,rtmpPath);
            pushStream(originPath,rtmpPath);
//            pushDoubleStream(mp4FileName,rtmpPath);
        });
        worker.start();
    }

    @Override
    public void stop()
    {
        exit=true;
    }



    //将原始rtmp流转码后推送到另一个rtmpPath
    private  void pushStream(String orginPath, String rtmpPath){
        // 使用rtsp的时候需要使用 FFmpegFrameGrabber，不能再用 FrameGrabber
        int width = 640, height = 480;
        FFmpegFrameRecorder recorder = null;
        IFrameTaker taker=new BasicFrameTaker(orginPath);
        taker.start();
        try {
            recorder = new FFmpegFrameRecorder(rtmpPath, width, height);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setFormat("flv");
            recorder.setSampleRate(44100);
            recorder.setAudioChannels(2);
            recorder.setFrameRate(30);
            recorder.start();

            while (!exit) {
                try {
                    TimeUtil timer=new TimeUtil();
                    timer.start();
                    List<Frame> frames=taker.takeFrames();
                    for(Frame frame:frames)
                    {
                        recorder.setTimestamp(frame.timestamp);
                        recorder.record(frame);
                        System.out.println("record one frame..."+frame.timestamp);
                    }
                    timer.end();

                } catch (Exception e) {
                }
            }
            recorder.stop();
            recorder.release();
           taker.stop();
            System.out.println("-----------------------push stop....");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //将原始rtmp流转码并合成图像后推送到另一个rtmpPath
    private  void pushDoubleStream(String orginPath, String rtmpPath){
        // 使用rtsp的时候需要使用 FFmpegFrameGrabber，不能再用 FrameGrabber

        // 构造测试字幕
        String[] test = {
                "世上无难事",
                "只怕有心人",
                "只要思想不滑坡",
                "办法总比困难多",
                "长江后浪推前浪",
                "前浪死在沙滩上"
        };

        // 为连续的50帧设置同一个测试字幕文本
        List<String> testStr = new ArrayList<String>();
        for (int i = 0; i < 300; i++) {
            testStr.add(test[i / 50]);
        }

        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(orginPath);
            grabber.start();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtmpPath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());

            // 视频相关配置，取原视频配置
//            recorder.setFrameRate(grabber.getFrameRate());
//            recorder.setVideoCodec(grabber.getVideoCodec());
//            recorder.setVideoBitrate(grabber.getVideoBitrate());
//            // 音频相关配置，取原音频配置
//            recorder.setSampleRate(grabber.getSampleRate());
////            recorder.setAudioCodec(avcodec.AV_CODEC_ID_MP3);
//            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
//            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//            recorder.setFormat("flv");


            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setFormat("flv");
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.start();
            System.out.println("准备开始推流...");
            Java2DFrameConverter converter = new Java2DFrameConverter();
            Frame frame=null;
            int i = 0;
            while ((frame = grabber.grab()) != null) {
                // 从视频帧中获取图片
                long timestamp=grabber.getTimestamp();

                if (frame.image != null) {

                    BufferedImage bufferedImage = converter.getBufferedImage(frame);

                    // 对图片进行文本合入
                    BufferedImage smallImage=scaleImage(bufferedImage,0.34);
                    smallImage = addSubtitle(smallImage, testStr.get(i++ % 300));
                    bufferedImage=combineTwoImages(smallImage,bufferedImage);

                    // 视频帧赋值，写入输出流
                    frame.image = converter.getFrame(bufferedImage).image;
                    recorder.setTimestamp(timestamp);
                    recorder.record(frame);
                    System.out.println("-----------------------push one video frame...."+frame.timestamp);
                }

                // 音频帧写入输出流
                if(frame.samples != null) {
                    recorder.setTimestamp(timestamp);
                    recorder.record(frame);
                    System.out.println("-----------------------push one audio frame...."+frame.timestamp);
                }
            }
            System.out.println("推流结束...");
            System.out.println("-----------------------push stop....");
            grabber.stop();
            recorder.stop();
            recorder.release();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 图片添加文本
     *
     * @param bufImg
     * @param subTitleContent
     * @return
     */
    private BufferedImage addSubtitle(BufferedImage bufImg, String subTitleContent) {

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 添加字幕时的时间
        Font font = new Font("微软雅黑", Font.BOLD, 12);
        String timeContent = sdf.format(new Date());
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        Graphics2D graphics = bufImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        //设置图片背景
        graphics.drawImage(bufImg, 0, 0, bufImg.getWidth(), bufImg.getHeight(), null);

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
        return bufImg;
    }


    //复制一个同样内容但缩放过的图像
    private BufferedImage scaleImage(BufferedImage bufImg, double ratio) {
        int width=(int)(bufImg.getWidth()*ratio);
        int height=(int)(bufImg.getHeight()*ratio);
        final BufferedImage resultImg = new BufferedImage( width,height, BufferedImage.TYPE_INT_ARGB );

        Graphics2D graphics = resultImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        //设置图片背景
        graphics.drawImage(bufImg, 0, 0, width,height, null);
        graphics.dispose();
        return resultImg;
    }

    //将小图像叠加到大图像上合成一个新图像返回
    private BufferedImage combineTwoImages(BufferedImage smallImage, BufferedImage bigImage) {
        Graphics2D graphics = bigImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        //叠加图像
        graphics.drawImage(smallImage, bigImage.getWidth()-smallImage.getWidth(), 0, smallImage.getWidth(),smallImage.getHeight(), null);
        graphics.dispose();
        return bigImage;
    }

    }
