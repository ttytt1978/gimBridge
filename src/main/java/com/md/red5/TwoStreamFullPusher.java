package com.md.red5;

import com.md.util.TimeUtil;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Math.pow;

public class TwoStreamFullPusher implements IPusher {

    private String orginPath1;
    private String orginPath2;
    private String rtmpPath;
    private Thread worker;
    private boolean exit = true;

    public TwoStreamFullPusher(String orginPath1, String orginPath2, String rtmpPath) {
        this.orginPath1 = orginPath1;
        this.orginPath2 = orginPath2;
        this.rtmpPath = rtmpPath;
    }

    @Override
    public void start() {
        if (exit == false)
            return;
        exit = false;
        worker = new Thread(() -> {
            pushDoubleStream();
        });
        worker.start();
    }

    @Override
    public void stop() {
        exit = true;
    }


    //将原始rtmp流转码并合成图像后推送到另一个rtmpPath
    private void pushDoubleStream() {
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
            FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(orginPath2);
//            grabber1.setSampleRate(grabber2.getSampleRate());
//            grabber1.setAudioChannels(grabber2.getAudioChannels());
//            grabber1.setSampleFormat(grabber2.getSampleFormat());
//            grabber1.setSampleMode(grabber2.getSampleMode());
            grabber1.start();
            grabber2.start();
//            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtmpPath, grabber1.getImageWidth(), grabber1.getImageHeight(), grabber1.getAudioChannels());
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtmpPath, 640, 480);

            // 视频相关配置，取原视频配置
//            recorder.setFrameRate(grabber1.getFrameRate());
            recorder.setFrameRate(20);
//            recorder.setVideoCodec(grabber.getVideoCodec());
//            recorder.setVideoBitrate(grabber1.getVideoBitrate());
            recorder.setVideoBitrate(8*1024*1024);
//            // 音频相关配置，取原音频配置
//            recorder.setSampleRate(grabber1.getSampleRate());
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
            int sampleRate = 44100;
            int audioChannels = 2;

//            int sampleRate = grabber1.getSampleRate();
//            int audioChannels = grabber1.getAudioChannels();
//            recorder.setAudioOption("crf", "0");
//            // 最高质量
//            recorder.setAudioQuality(0);
//            // 音频比特率
            recorder.setAudioBitrate(2*128*1024);
//            // 音频采样率
            recorder.setSampleRate(sampleRate);
//            // 双通道(立体声)
            recorder.setAudioChannels(audioChannels);
//            // 音频编/解码器
//            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//            recorder.setInterleaved(true);
            recorder.start();
            System.out.println("准备开始双录推流...");
            Java2DFrameConverter converter = new Java2DFrameConverter();
            Java2DFrameConverter converter2= new Java2DFrameConverter();
            Frame frame1 = null;
            Frame frame2 = null;
            BufferedImage previousBigImage = null;
            BufferedImage previousSmallImage = null;
//            OpenCVFrameConverter.ToIplImage frameConveter = new OpenCVFrameConverter.ToIplImage();
            long curTimeStamp = -1;
            double scaleRation = 0.5;

            int i = 0;
            while (!exit) {
                    TimeUtil timer=new TimeUtil();
                    frame1 = grabber1.grab();
               while(frame1==null)
               {
                   System.out.println("--------------------------frame1为null，等待...！");
                   Thread.sleep(3000);
                   try {
                       try
                       {
                           grabber1.stop();
                       }catch (Exception e1)
                       {

                       }
                       grabber1 = new FFmpegFrameGrabber(orginPath1);
                       grabber1.start();
                       frame1 = grabber1.grab();
//                       curTimeStamp = -1;
                       previousBigImage = null;
                       previousSmallImage = null;
                   }catch (Exception e)
                   {
                       try
                       {
                          grabber1.stop();
                       }catch (Exception e1)
                       {

                       }finally {
                           frame1=null;
                       }

                   }
               }

                frame2 = grabber2.grab();
                while(frame2==null)
                {
                    System.out.println("--------------------------frame2为null，等待...！");
                    Thread.sleep(3000);
                    try {
                        try
                        {
                            grabber2.stop();
                        }catch (Exception e1)
                        {

                        }
                        grabber2 = new FFmpegFrameGrabber(orginPath2);
                        grabber2.start();
                        frame2 = grabber2.grab();
//                        curTimeStamp = -1;
                        previousBigImage = null;
                        previousSmallImage = null;
                    }catch (Exception e)
                    {
                        try
                        {
                            grabber2.stop();
                        }catch (Exception e1)
                        {

                        }finally {
                            frame2=null;
                        }
                    }
                }
                timer.end();
                System.out.println("------grab frame1 and frame2-----cost:"+timer.getTimeInMillSecond());
                // 从视频帧中获取图片
                long timestamp1 = frame1.timestamp;
                long timestamp2 = grabber2.getTimestamp();

//                if (timestamp1 <= curTimeStamp) {
//                    System.out.println("-------------------------当前frame1的timestamp错误，跳过！timestamp=" + timestamp1);
//                    continue;
//                }
                if(curTimeStamp<=0)
                {
                    curTimeStamp=System.currentTimeMillis();
                }
                timestamp1=1000*(System.currentTimeMillis()-curTimeStamp);

                timer=new TimeUtil();
                if (frame1.image != null) {
                    previousBigImage= converter.getBufferedImage(frame1);
                    if(frame2.image!=null)
                    {
                        // 对图片进行文本合入
//                        BufferedImage smallImage=scaleImage(converter.getBufferedImage(frame2),scaleRation,bufferedImage.getWidth(),bufferedImage.getHeight());
                        previousSmallImage=scaleImage(converter2.getBufferedImage(frame2),scaleRation,previousBigImage.getWidth(),previousBigImage.getHeight());
//                        smallImage = addSubtitle(smallImage, testStr.get(i++ % 300));
                        previousBigImage=combineTwoImages(previousSmallImage,previousBigImage);
                        // 视频帧赋值，写入输出流
//                        frame1.image = converter.getFrame(bufferedImage).image;
                        recorder.setTimestamp(timestamp1);
                        System.out.println("-----------------------push one combined video frame（实时大小图像合成一帧）...."+timestamp1);
//                        recorder.record(frame1);
                        recorder.record(converter.convert(previousBigImage));

                    }else {
                        if(previousSmallImage!=null)
                        {
//                            BufferedImage smallImage = addSubtitle(previousSmallImage, testStr.get(i++ % 300));
                            previousBigImage=combineTwoImages(previousSmallImage,previousBigImage);
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one combined video frame(小图像用前一帧合成)...."+timestamp1);
//                            recorder.record(frame1);
                            recorder.record(converter.convert(previousBigImage));

                        }else {
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one video frame（无小图像只用大图像生成帧）...."+timestamp1);
//                            recorder.record(frame1);
                            recorder.record(converter.convert(previousBigImage));

                        }
                    }

                }
                else {

                    if(frame2.image!=null)
                    {
//                        previousSmallImage= scaleImage(converter2.getBufferedImage(frame2),scaleRation);
                        if(previousBigImage!=null)
                        {
//                            smallImage=scaleImage(converter.getBufferedImage(frame2),scaleRation,previousBigImage.getWidth(),previousBigImage.getHeight());

                            previousSmallImage=scaleImage(converter2.getBufferedImage(frame2),scaleRation,previousBigImage.getWidth(),previousBigImage.getHeight());
//                            smallImage=scaleImage(converter.getBufferedImage(frame2),scaleRation);
//                            previousSmallImage=smallImage;
                            previousBigImage=combineTwoImages(previousSmallImage,previousBigImage);
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one combined video frame(大图像用前一帧合成)...."+timestamp1);
                            recorder.record(converter.convert(previousBigImage));

                        }else
                        {
                            System.out.println("----------------无大图像无法生成帧...");
                        }
                    }else
                    {
                        if(previousSmallImage!=null&&previousBigImage!=null)
                        {
//                            BufferedImage smallImage = addSubtitle(previousSmallImage, testStr.get(i++ % 300));
                            previousBigImage=combineTwoImages(previousSmallImage,previousBigImage);
                            // 视频帧赋值，写入输出流
//                            frame1.image = converter.getFrame(bufferedImage).image;
                            recorder.setTimestamp(timestamp1);
                            System.out.println("-----------------------push one combined video frame(大小图像都用前一帧合成)...."+timestamp1);
                            recorder.record(converter.convert(previousBigImage));
                        }else
                        {
                            System.out.println("----------------无实时大小图像无法生成帧...");
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
//                    Buffer[] samples=mixTwoSounds(frame1.samples,frame2.samples);
//                    recorder.recordSamples(sampleRate,audioChannels,samples);
////                    recorder.recordSamples(sampleRate,audioChannels,frame1.samples );
////                    recorder.recordSamples(sampleRate,audioChannels,frame2.samples );
//                    continue;
//                }
                if(frame1.samples != null) {
                    recorder.setTimestamp(timestamp1);
                    System.out.println("-----------------------push one audio frame from stream1...."+timestamp1);
//                    recorder.record(frame1);
                    recorder.recordSamples(sampleRate,audioChannels,frame1.samples );
//                    continue;
                }
                // 音频帧写入输出流
//                if(frame2.samples != null) {
//                    recorder.setTimestamp(timestamp1);
////                    recorder.record(frame2);
//                    recorder.recordSamples(sampleRate,audioChannels,frame2.samples );
//                    System.out.println("-----------------------push one audio frame from stream2...."+timestamp1);
//                }
                timer.end();
                System.out.println("------combine frame1 and frame2-----cost:"+timer.getTimeInMillSecond());
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

    //对两个Buffer[]数组进行混音合成，生成一个新的Buffer[]数组返回
    private Buffer[] mixTwoSounds(Buffer[] soundA, Buffer[] soundB) {
        int lengthA = soundA.length;
        int lengthB = soundB.length;
        int maxLength = lengthA > lengthB ? lengthA : lengthB;
        Buffer[] samples = new Buffer[maxLength];
        for (int k = 0; k < maxLength; k++) {
            if (k < lengthA && k < lengthB) {
                samples[k] = mixTwoBuffer(soundA[k], soundB[k]);
                continue;
            }
            if (k < lengthA) {
                samples[k] = soundA[k];
            }
            if (k < lengthB) {
                samples[k] = soundB[k];
            }
        }
        return samples;
//        return soundB;
    }

    private Buffer mixTwoBuffer(Buffer a, Buffer b) {
        int mainLength = 0, slaveLength = 0;
        ShortBuffer mainBuf = null, slaveBuf = null;
        if (a.capacity() > b.capacity()) {
            mainBuf = (ShortBuffer) a;
            slaveBuf = (ShortBuffer) b;
        } else {
            mainBuf = (ShortBuffer) b;
            slaveBuf = (ShortBuffer) a;
        }
        mainLength = mainBuf.capacity();
        slaveLength = slaveBuf.capacity();
        short[] mainArray = new short[mainLength];
        short[] slaveArray = new short[slaveLength];
        mainBuf.rewind();
        slaveBuf.rewind();
        mainBuf.get(mainArray);
        slaveBuf.get(slaveArray);
//        for(int k=0;k<slaveLength;k++)
//        {
////            mainArray[k]=(short)((mainArray[k]+slaveArray[k])>>1);//混音
//            mainArray[k]=mix(mainArray[k],slaveArray[k]);
//        }
        mainArray = mix1(mainArray, slaveArray, slaveLength);
        mainBuf.rewind();
        slaveBuf.rewind();
        mainBuf.put(mainArray);
        System.out.println("a.class=" + a.getClass().getName() + "---a.hasArray=" + a.hasArray() + ",a.readonly=" + a.isReadOnly() + ",a.capacity=" + a.capacity());
        System.out.println("b.class=" + b.getClass().getName() + "---b.hasArray=" + b.hasArray() + ",b.readonly=" + b.isReadOnly() + ",b.capacity=" + b.capacity());
        return mainBuf;
    }

    private  double f = 1;
    private short[] mix1(short[] main, short[] slave, int slaveLength) {
        //归一化混音
        final int MAX = 32767;
        final int MIN = -32768;
        int output = 0;
        for (int i = 0; i < slaveLength; i++) {
            output = (int) ((main[i] + slave[i]) * f);

            if (output > MAX) {
                f = (double) MAX / (double) (output);
                output = MAX;
            }
            if (output < MIN) {
                f = (double) MIN / (double) (output);
                output = MIN;
            }
            if (f < 1) {
                //此处取SETPSIZE 为 32
                f += ((double) 1 - f) / (double)16;
            }
            main[i] = (short) output;

        }
        return main;
    }

    private short mix(short data1, short data2) {
        short date_mix = 0;
        if (data1 < 0 && data2 < 0)
            date_mix = (short) (data1 + data2 - (data1 * data2 / -(pow(2, 16 - 1) - 1)));
        else
            date_mix = (short) (data1 + data2 - (data1 * data2 / (pow(2, 16 - 1) - 1)));
        return date_mix;
    }


    /**
     * b1与b2数组长度可以不相等
     *
     * @param b1 byte1[]
     * @param b2 byte2[]
     * @return
     */
    public byte[] remix(byte[] b1, byte[] b2) {
        int l1 = b1.length;
        int l2 = b2.length;
        byte[] bMax = null;
        byte[] bMin = null;
        if (l1 > l2) {
            bMax = b1;
            bMin = b2;
        } else {
            bMax = b2;
            bMin = b1;
        }
        byte[] b = new byte[bMax.length];
        for (int i = 0; i < bMax.length; i++) {
            if (i < bMin.length) {
                b[i] = (byte) ((bMax[i] + bMin[i]) >> 1);
            } else {
                b[i] = bMax[i];
            }
        }
        return b;
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
        BufferedImage resultImg = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
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


    //复制一个同样内容但缩放过的图像
//    private BufferedImage scaleImage(BufferedImage bufImg, double ratio) {
//        int width = (int) (bufImg.getWidth() * ratio);
//        int height = (int) (bufImg.getHeight() * ratio);
//        BufferedImage resultImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//
//        Graphics2D graphics = resultImg.createGraphics();
//        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
//
//        //设置图片背景
//        graphics.drawImage(bufImg, 0, 0, width, height, null);
//        graphics.dispose();
//        return resultImg;
//    }

    //复制一个同样内容但缩放过的图像（按给定宽高）
    private BufferedImage scaleImage(BufferedImage bufImg, double ratio, int maxWidth, int maxHeight) {
        int width = (int) (maxWidth * ratio);
        int height = (int) (maxHeight * ratio);
        final BufferedImage resultImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

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


}
