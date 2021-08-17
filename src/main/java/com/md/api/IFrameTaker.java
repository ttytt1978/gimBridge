package com.md.api;

import org.bytedeco.javacv.Frame;

import java.util.List;

//取帧器接口
public interface IFrameTaker {

    void start();//启动帧抓取
    void stop();//停止帧抓取
    Frame takeFirstFrame();//获取目前抓取到的第1帧，无则返回null
    Frame takeLastFrame();//获取目前抓取到的最后1帧，无则返回null
    List<Frame> takeFrames();//获取目前抓取到的全部帧列表，列表中第1个元素为第1帧，最后一个元素为最后1帧，无则返回空列表
    boolean isDisconnected();//是否与音视频源连接上，true代表未连接上
    void clearFrames();//清空抓取到的全部帧队列
}
