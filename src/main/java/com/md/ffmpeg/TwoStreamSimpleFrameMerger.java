package com.md.ffmpeg;

import com.md.api.IFrameTaker;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

//简单合帧器
public class TwoStreamSimpleFrameMerger extends AbstractFrameMerger {

    public TwoStreamSimpleFrameMerger()
    {
        super(15);
    }

    public TwoStreamSimpleFrameMerger(int capacity)
    {
        super(capacity);
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

    //合帧处理
    @Override
    protected void mergeFrameRepeatly(List<IFrameTaker> frameTakers) {
        TwoFrameMerger merger=new TwoFrameMerger();
        List<Frame> totalFrames1=new ArrayList<>();
        List<Frame> totalFrames2=new ArrayList<>();
        while (!isStopped()) {
            if(!canGrab())
                continue;
            IFrameTaker taker1 = frameTakers.get(0);
            Frame frame=taker1.takeFirstFrame();
            if(frame!=null)
            {
                addMergedFrameToQue(frame);
//                System.out.println("合帧器完成工作一次...bufferSize="+frameQue.size());
            }


//           totalFrames1.addAll(taker1.takeFrames());
//            IFrameTaker taker2 = frameTakers.get(1);
//            totalFrames2.addAll(taker2.takeFrames());
//            if(totalFrames1.size()+totalFrames2.size()<bufferMaxCapacity*0.1)
//                 continue;
//            for (int i = 0; i < totalFrames1.size(); i++) {
//                Frame frame1 = totalFrames1.get(i);
//                addMergedFrameToQue(frame1);
//            }
//            totalFrames1.clear();
//            totalFrames2.clear();
//            System.out.println("合帧器完成工作一次...bufferSize="+frameQue.size());
        }
    }


    private class TwoFrameMerger {
        private Java2DFrameConverter converter = new Java2DFrameConverter();
        private Java2DFrameConverter converter2 = new Java2DFrameConverter();
        private BufferedImage previousBigImage = null;
        private BufferedImage previousSmallImage = null;
        private double scaleRation = 0.3;

        private Frame mergeOnlyBigFrame(Frame frame1)
        {
            Frame resultFrame = null;
            long timestamp1 = frame1.timestamp;
            if (frame1.image != null) {
                previousBigImage = converter.getBufferedImage(frame1);
                if (previousSmallImage != null) {
                    previousBigImage = combineTwoImages(previousSmallImage, previousBigImage);
                    // 视频帧赋值，写入输出流
                    resultFrame = converter.convert(previousBigImage);
                    resultFrame.timestamp = timestamp1;
                    System.out.println("-----------------------push one combined video frame(小图像用前一帧合成)...." + timestamp1);
                    return resultFrame;

                } else {
                    // 视频帧赋值，写入输出流
                    resultFrame = converter.convert(previousBigImage);
                    resultFrame.timestamp = timestamp1;
                    System.out.println("-----------------------push one video frame（无小图像只用大图像生成帧）...." + timestamp1);
                    return resultFrame;
                }
            }else {
                return frame1;
            }
        }

        public Frame merge(Frame frame1, Frame frame2) {
            Frame resultFrame = null;
            if (null == frame1 )
                return resultFrame;
            if(null==frame2)
                return mergeOnlyBigFrame(frame1);
            long timestamp1 = frame1.timestamp;
            if (frame1.image != null) {
                previousBigImage = converter.getBufferedImage(frame1);
                if (frame2.image != null) {
                    // 对图片进行合入
                    previousSmallImage = scaleImage(converter2.getBufferedImage(frame2), scaleRation, previousBigImage.getWidth(), previousBigImage.getHeight());
                    previousBigImage = combineTwoImages(previousSmallImage, previousBigImage);
                    resultFrame = converter.convert(previousBigImage);
                    resultFrame.timestamp = timestamp1;
                    System.out.println("-----------------------push one combined video frame（实时大小图像合成一帧）...." + timestamp1);
                    return resultFrame;
                } else {
                    if (previousSmallImage != null) {
                        previousBigImage = combineTwoImages(previousSmallImage, previousBigImage);
                        // 视频帧赋值，写入输出流
                        resultFrame = converter.convert(previousBigImage);
                        resultFrame.timestamp = timestamp1;
                        System.out.println("-----------------------push one combined video frame(小图像用前一帧合成)...." + timestamp1);
                        return resultFrame;

                    } else {
                        // 视频帧赋值，写入输出流
                        resultFrame = converter.convert(previousBigImage);
                        resultFrame.timestamp = timestamp1;
                        System.out.println("-----------------------push one video frame（无小图像只用大图像生成帧）...." + timestamp1);
                        return resultFrame;
                    }
                }
            } else {
                if (frame2.image != null) {
                    if (previousBigImage != null) {
                        previousSmallImage = scaleImage(converter2.getBufferedImage(frame2), scaleRation, previousBigImage.getWidth(), previousBigImage.getHeight());
                        previousBigImage = combineTwoImages(previousSmallImage, previousBigImage);
                        // 视频帧赋值，写入输出流
                        resultFrame = converter.convert(previousBigImage);
                        resultFrame.timestamp = timestamp1;
                        System.out.println("-----------------------push one combined video frame(大图像用前一帧合成)...." + timestamp1);
                        return resultFrame;
                    } else {
                        System.out.println("----------------无大图像无法生成帧...");
                        return null;
                    }
                } else {
                    if (previousSmallImage != null && previousBigImage != null) {
                        previousBigImage = combineTwoImages(previousSmallImage, previousBigImage);
                        // 视频帧赋值，写入输出流
                        resultFrame = converter.convert(previousBigImage);
                        resultFrame.timestamp = timestamp1;
                        return resultFrame;
                    } else {
                        System.out.println("----------------无实时大小图像无法生成帧...");
                        return null;
                    }
                }
            }
//                 音频帧写入输出流
//            if (frame1.samples == null && frame2.samples == null)
//                continue;
        }

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
            graphics.draw3DRect(0, 0, smallImage.getWidth(), smallImage.getHeight(), true);
            graphics.dispose();
            return bigImage;
        }


    }

}
