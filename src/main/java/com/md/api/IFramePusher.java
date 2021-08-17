package com.md.api;

//推帧器接口
public interface IFramePusher {
    void setFrameMerger(IFrameMerger merger);//注入1个合帧器以便于将合帧结果推流
    void start();//启动推帧器
    void stop();//停止推帧器
}
