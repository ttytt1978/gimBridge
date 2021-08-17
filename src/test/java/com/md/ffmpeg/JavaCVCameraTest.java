package com.md.ffmpeg;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JavaCVCameraTest {


    public static void main(String[] args) throws Exception, InterruptedException{

        //新建opencv抓取器，一般的电脑和移动端设备中摄像头默认序号是0，不排除其他情况
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();   //开始获取摄像头数据

        CanvasFrame canvas = new CanvasFrame("摄像头预览");//新建一个预览窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);
        canvas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    grabber.close();//停止抓取
                    canvas.setVisible(false);
                    System.exit(0);
                } catch (FrameGrabber.Exception exception) {
                    exception.printStackTrace();
                }
                super.windowClosing(e);
            }
        });

        while(true){
            if(!canvas.isVisible())
            {//窗口是否关闭
                grabber.close();//停止抓取
                return;
            }
            canvas.showImage(grabber.grab());//获取摄像头图像并放到窗口上显示， 这里的Frame
//            frame=grabber.grab(); frame是一帧视频图像
        }
    }


}
