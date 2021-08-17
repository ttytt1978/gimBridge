package com.md.api;

import org.bytedeco.javacv.Frame;

import java.util.List;

//合帧器接口
public interface IFrameMerger {
    void setFrameTakers(IFrameTaker... takers);//注入多个取帧器以便于合帧处理
    void start();//启动合帧器
    void stop();//停止合帧器
    List<Frame> takeMergedFrames();//获取目前合并的全部帧列表，列表中第1个元素为第1帧，最后一个元素为最后1帧，无则返回空列表
    Frame takeFirstFrame();//获取目前抓取到的第1帧，无则返回null
    Frame takeLastFrame();//获取目前抓取到的最后1帧，无则返回null
    void clearFrames();//清空抓取到的全部帧队列
}
