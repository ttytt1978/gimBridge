package com.md.red5;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TwoStreamPusher implements IPusher {

    private String orginPath1;
    private String orginPath2;
    private String rtmpPath;
    private Thread worker;
    private boolean exit=true;

    public TwoStreamPusher(String orginPath1, String orginPath2, String rtmpPath) {
        this.orginPath1 = orginPath1;
        this.orginPath2 = orginPath2;
        this.rtmpPath = rtmpPath;
    }

    @Override
    public void start()
    {
        if(exit==false)
            return;
        exit=false;
        worker=new Thread(() -> {
            pushDoubleStream();
        });
        worker.start();
    }

    @Override
    public void stop()
    {
        exit=true;
    }


    //将原始rtmp流转码并合成图像后推送到另一个rtmpPath
    private  void pushDoubleStream(){
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

//        AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);

        try {
            FFmpegFrameGrabber grabber1 = new FFmpegFrameGrabber(orginPath1);
            grabber1.start();
            FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(orginPath2);
//            grabber1.setSampleRate(grabber2.getSampleRate());
//            grabber1.setAudioChannels(grabber2.getAudioChannels());
//            grabber1.setSampleFormat(grabber2.getSampleFormat());
//            grabber1.setSampleMode(grabber2.getSampleMode());
            grabber2.start();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtmpPath, grabber1.getImageWidth(), grabber1.getImageHeight(), grabber1.getAudioChannels());

            // 视频相关配置，取原视频配置
            recorder.setFrameRate(grabber1.getFrameRate());
//            recorder.setVideoCodec(grabber.getVideoCodec());
            recorder.setVideoBitrate(grabber1.getVideoBitrate());
//            // 音频相关配置，取原音频配置
//            recorder.setSampleRate(grabber.getSampleRate());
////            recorder.setAudioCodec(avcodec.AV_CODEC_ID_MP3);
//            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
//            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setFormat("flv");


            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //avcodec.AV_CODEC_ID_H264  //AV_CODEC_ID_MPEG4
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//            recorder.setFormat("flv");
//            recorder.setSampleRate(grabber1.getSampleRate());
//            recorder.setAudioChannels(grabber1.getAudioChannels());
            // 不可变(固定)音频比特率
            int sampleRate=44100;
            int audioChannels=2;
            recorder.setAudioOption("crf", "0");
            // 最高质量
            recorder.setAudioQuality(0);
            // 音频比特率
            recorder.setAudioBitrate(192000);
            // 音频采样率
            recorder.setSampleRate(sampleRate);
            // 双通道(立体声)
            recorder.setAudioChannels(audioChannels);
            // 音频编/解码器
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//            recorder.setInterleaved(true);
            recorder.start();
            System.out.println("准备开始双录推流...");
            Java2DFrameConverter converter = new Java2DFrameConverter();
            Frame frame1=null;
            Frame frame2=null;
            BufferedImage previousBigImage=null;
            BufferedImage previousSmallImage=null;
            OpenCVFrameConverter.ToIplImage frameConveter = new OpenCVFrameConverter.ToIplImage();
            long curTimeStamp=-1;

            int i = 0;
            while (!exit) {
                frame1 = grabber1.grab();
                frame2 = grabber2.grab();
                if(frame1==null||frame2==null)
                {
                    System.out.println("--------------------------frame1或frame2为null，跳过！");
                    continue;
                }

                // 从视频帧中获取图片
                long timestamp1=frame1.timestamp;
                long timestamp2=grabber2.getTimestamp();

                if(timestamp1<=curTimeStamp)
                {
                    System.out.println("-------------------------当前frame1的timestamp错误，跳过！timestamp="+timestamp1);
                    continue;
                }
                curTimeStamp=timestamp1;

                if (frame1.image != null) {
                    BufferedImage bufferedImage = converter.getBufferedImage(frame1);
                    previousBigImage=bufferedImage;
                    if(frame2.image!=null)
                    {
                        // 对图片进行文本合入
                        BufferedImage smallImage=scaleImage(converter.getBufferedImage(frame2),0.3);
                        previousSmallImage=smallImage;
                        smallImage = addSubtitle(smallImage, testStr.get(i++ % 300));
                        bufferedImage=combineTwoImages(smallImage,bufferedImage);
                        // 视频帧赋值，写入输出流
//                        frame1.image = converter.getFrame(bufferedImage).image;
                        recorder.setTimestamp(timestamp1);
                        System.out.println("-----------------------push one combined video frame（实时大小图像）...."+timestamp1);
//                        recorder.record(frame1);
                        recorder.record(converter.convert(bufferedImage));

                    }else {
                        if(previousSmallImage!=null)
                        {
                            BufferedImage smallImage = addSubtitle(previousSmallImage, testStr.get(i++ % 300));
                            bufferedImage=combineTwoImages(smallImage,bufferedImage);
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one combined video frame(小图像用前一帧)...."+timestamp1);
//                            recorder.record(frame1);
                            recorder.record(converter.convert(bufferedImage));

                        }else {
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one combined video frame（无小图像）...."+timestamp1);
//                            recorder.record(frame1);
                            recorder.record(converter.convert(bufferedImage));

                        }
                    }

                }else {

                    if(frame2.image!=null)
                    {
                        BufferedImage smallImage=scaleImage(converter.getBufferedImage(frame2),0.32);
                        previousSmallImage=smallImage;
                        if(previousBigImage!=null)
                        {
                            BufferedImage bufferedImage=combineTwoImages(smallImage,previousBigImage);
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one combined video frame(大图像用前一帧)...."+timestamp1);
                            recorder.record(converter.convert(bufferedImage));

                        }else
                        {
                            System.out.println("----------------无实时大小图像...");
                        }
                    }else
                    {
                        if(previousSmallImage!=null&&previousBigImage!=null)
                        {
                            BufferedImage smallImage = addSubtitle(previousSmallImage, testStr.get(i++ % 300));
                            BufferedImage bufferedImage=combineTwoImages(smallImage,previousBigImage);
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one combined video frame(大小图像都用前一帧)...."+timestamp1);
                            recorder.record(converter.convert(bufferedImage));
                        }else
                        {
                            System.out.println("----------------无实时大小图像...");
                        }
                    }

                }

//                 音频帧写入输出流
                if(frame1.samples==null&&frame2.samples==null)
                    continue;
//                if(frame1.samples!=null&&frame2.samples!=null)
//                {
//                    recorder.setTimestamp(timestamp1);
//                    System.out.println("-----------------------push one combined audio frame from stream1 and stream2...."+timestamp1);
//                    Buffer[] samples=new Buffer[frame1.samples.length+frame2.samples.length];
//                    int k=0;
//                    for(Buffer buffer:frame1.samples)
//                    {
//                        samples[k]=buffer;
//                        k++;
//                    }
//                    for(Buffer buffer:frame2.samples)
//                    {
//                        samples[k]=buffer;
//                        k++;
//                    }
//                    recorder.recordSamples(sampleRate,audioChannels,samples);
//                    continue;
//                }
                if(frame1.samples != null) {
                    recorder.setTimestamp(timestamp1);
                    System.out.println("-----------------------push one audio frame from stream1...."+timestamp1);
//                    recorder.record(frame1);
                    recorder.recordSamples(sampleRate,audioChannels,frame1.samples );
                    continue;
                }
                // 音频帧写入输出流
//                if(frame2.samples != null) {
//                    recorder.setTimestamp(timestamp1);
////                    recorder.record(frame2);
//                    recorder.recordSamples(sampleRate,audioChannels,frame2.samples );
//                    System.out.println("-----------------------push one audio frame from stream2...."+timestamp1);
//                    continue;
//                }

//                if(timestamp1>curTimeStamp)
//                {
//                    curTimeStamp=timestamp1;
//                }
            }
            System.out.println("双录推流结束...");
            System.out.println("-----------------------push stop....");
            grabber1.stop();
            grabber2.stop();
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
        BufferedImage resultImg = new BufferedImage( bufImg.getWidth(),bufImg.getHeight(), BufferedImage.TYPE_INT_ARGB );
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
//        BufferedImage resultImg = new BufferedImage( bigImage.getWidth(),bigImage.getHeight(), BufferedImage.TYPE_INT_ARGB );
//        Graphics2D graphics = resultImg.createGraphics();

        Graphics2D graphics = bigImage.createGraphics();


        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        //叠加图像
//        graphics.drawImage(bigImage, 0,0, null);
        graphics.drawImage(smallImage, bigImage.getWidth()-smallImage.getWidth(), 0, smallImage.getWidth(),smallImage.getHeight(), null);
        graphics.dispose();
        return bigImage;
    }

    }
