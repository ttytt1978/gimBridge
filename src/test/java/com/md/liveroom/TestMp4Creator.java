package com.md.liveroom;

public class TestMp4Creator {

    public static void main(String[] args) {
        try {
            Mp4Creator mp4Creator=new Mp4Creator("rtmp://127.0.0.1/oflaDemo/stream11","c:\\test2.mp4");
            mp4Creator.start();
            Thread.sleep(30*1000);
            mp4Creator.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
