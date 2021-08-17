package com.md.liveroom;

public class TestStreamPusher {

    public static void main(String[] args) {
        try {
            StreamPusher pusher=new StreamPusher("rtmp://127.0.0.1/oflaDemo/stream11","rtmp://127.0.0.1/oflaDemo/stream12","rtmp://127.0.0.1/oflaDemo/stream19");
            pusher.start();
            Thread.sleep(30*1000);
            pusher.stop();
            Thread.sleep(1*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
