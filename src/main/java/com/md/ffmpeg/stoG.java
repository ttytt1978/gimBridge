package com.md.ffmpeg;


import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * 视频FIFO
 */

public class stoG {
    private final int MAX = 500;
    private ArrayList<myVideoFrame> list = new ArrayList<>();
    public void produce(myVideoFrame f){
        synchronized(list){
            while(list.size()+1>MAX){
                try{
                    if(Util.DEBUG2)
                        System.out.println("视频缓冲区已满，需要等待消费");
                    list.wait();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            list.add(f);
            list.notifyAll();
        }
    }
    public myVideoFrame consume(){
        myVideoFrame f;
        synchronized (list){
            while(list.size()<1){
                try{
                    if(Util.DEBUG2)
                        System.out.println("视频缓冲区为空，需要等待生产");
                    list.wait();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            if(Util.DEBUG2)
                    System.out.println("******************视频缓冲区当前数量"+list.size());
            f = list.remove(0);
            list.notifyAll();
            return f;
        }
    }
    public int getMax(){
        return MAX;
    }
    public int getCur(){
        synchronized (list){
            return list.size();
        }
    }
    public void clearFIFO(){
        synchronized (list){
            list.clear();
            list.notifyAll();
        }
    }
}

class myVideoFrame {
    public Frame f;
    public long id;
    public long timeStamp;
//    public ImageIcon imgIcon;
    private videoPlayer v;
    public myVideoFrame(videoPlayer v, Frame f, long id, long timeStamp){
        this.f = f;
        this.id = id;
        this.timeStamp = timeStamp;
        this.v = v;
        processImage();
    }
    private void processImage() {
        /**
         * 可以在这里图像处理，不过要考虑处理时间
         * 尽量短，比如图像缩放不能用java自带的getScaledInstance,
         * 耗费20ms太长了，应该用javaCV中opencv包里面的函数
         * 比如http://blog.csdn.net/m1109048058/article/details/77069607
         * https://stackoverflow.com/questions/16497853/scale-a-bufferedimage-the-fastest-and-easiest-way
         */
        BufferedImage bi = (new Java2DFrameConverter()).getBufferedImage(f);
//        ImageIcon ii = new ImageIcon(bi);
//        long c1 = System.currentTimeMillis();
//        Util.mark(bi,ii.getImage(),Util.getTimeString(timeStamp)+" / "+v.getTotalString(),new Font("宋体", Font.PLAIN,20),Color.WHITE,20,20);
//        long c2 = System.currentTimeMillis();
        //System.out.println("图像添加水印花费"+(c2-c1)+"ms");
//        this.imgIcon = ii;
    }
}



