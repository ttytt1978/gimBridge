package com.md.ffmpeg;

import org.bytedeco.javacv.Frame;

public class UnityFrame extends Frame {
    public int channel;
    public Frame frame;

    public UnityFrame(Frame frame,int channel) {
        super();
        this.channel = channel;
        this.frame=frame;
    }


}
