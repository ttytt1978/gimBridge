package com.md.ffmpeg;

import com.md.red5.Mp4Pusher;

public class TestVideoAudioMerge {

    public static void main(String[] args) {
//        testVideoPlayer();
        testMp4Pusher();
    }


    public static void testVideoPlayer() {
        String mp4FileName = "d:\\ffmpeg\\b1.mp4";
        String rtmpPath = "rtmp://127.0.0.1/live/stream5";
//        String rtmpPath = "c:\\test.mp4";
//        File file=new File(mp4FileName);
        videoPlayer player = new videoPlayer(mp4FileName,rtmpPath);
        int imgW = player.getVideoWidth();
        int imgH = player.getVideoHeight();
        System.out.println("video width=" + imgW + " ,height=" + imgH);
        player.setStartPlay();

    }

    public static void testMp4Pusher() {
        String mp4FileName = "d:\\ffmpeg\\b1.mp4";
        String rtmpPath = "rtmp://127.0.0.1/live/stream5";
//        String rtmpPath = "c:\\test.mp4";
//        File file=new File(mp4FileName);
        Mp4Pusher pusher=new Mp4Pusher(mp4FileName,rtmpPath);
        pusher.start();

    }
}
