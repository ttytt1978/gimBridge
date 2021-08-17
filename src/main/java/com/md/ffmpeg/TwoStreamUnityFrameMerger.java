package com.md.ffmpeg;

import com.md.api.IFrameTaker;
import com.md.util.TimeUtil;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import sun.font.FontDesignMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//简单合帧器
public class TwoStreamUnityFrameMerger extends AbstractFrameMerger {

    public TwoStreamUnityFrameMerger() {
        this(200);
    }

    public TwoStreamUnityFrameMerger(int capacity) {
        super(capacity);
    }

    //合帧处理
    @Override
    protected void mergeFrameRepeatly(List<IFrameTaker> frameTakers) {
        TwoFrameMerger merger = new TwoFrameMerger();
        TimeUtil totalTimer = new TimeUtil();
        totalTimer.start();
        int mergeCount = 0;
        deleteCount = 0;
        while (!isStopped()) {
            TimeUtil timer = new TimeUtil();
            timer.start();
            IFrameTaker unityTaker = frameTakers.get(0);
            UnityFrame unityFrame=(UnityFrame)unityTaker.takeFirstFrame();
            if(null==unityFrame)
                continue;
            System.out.println("------------合并处理一帧视频..timestamp=" + unityFrame.frame.timestamp);
            if(unityFrame.frame!=null)//处理音频帧
            {
                addMergedFrameToQue(unityFrame.frame);
                continue;
            }
           if(unityFrame.frame!=null)
               continue;
            long timestamp=0;
            TimeUtil timer2 = new TimeUtil();
            timer2.start();
            Frame mergedVideoFrame = null;
            if(unityFrame.channel==2)
                merger.updateSmallImage(unityFrame.frame);
            if(unityFrame.channel==1)
                merger.updateBigImage(unityFrame.frame);
            try {
                mergedVideoFrame = merger.mergeFrame(unityFrame.frame.timestamp);
            } catch (Exception e) {
            }
            timer2.end();
            if(mergedVideoFrame!=null)
            {
                timestamp=mergedVideoFrame.timestamp;
                System.out.println("------------合并处理一帧视频..timestamp=" + timestamp + " ,cost=" + (long) (timer2.getTimeInMillSecond()) + "毫秒");
                mergeCount++;
            }
            totalTimer.end();
            if (totalTimer.getTimeInSecond() >= 1.0) {
                System.out.println("1秒内合成视频总帧数：" + mergeCount + " ,删除视频总帧数：" + deleteCount);
                mergeCount = 0;
                deleteCount = 0;
                totalTimer = new TimeUtil();
                totalTimer.start();
            }
            timer.end();
            addMergedFrameToQue(mergedVideoFrame);
        }
    }


    private class TwoFrameMerger {
        private Java2DFrameConverter converter = new Java2DFrameConverter();
        private Java2DFrameConverter converter2 = new Java2DFrameConverter();
        private BufferedImage previousBigImage = null;
        private BufferedImage previousSmallImage = null;
        private double scaleRation = 0.3;


        public Frame mergeFrame(long timestamp) {
            Frame resultFrame = null;
            if(null!=previousBigImage&&null!=previousSmallImage)
            {
                converter = new Java2DFrameConverter();
                BufferedImage image1=scaleImage(previousSmallImage,scaleRation,previousBigImage.getWidth(),previousBigImage.getHeight());
                BufferedImage imageb=copyImage(previousBigImage);
                BufferedImage image2=combineTwoImages(image1,imageb);
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
