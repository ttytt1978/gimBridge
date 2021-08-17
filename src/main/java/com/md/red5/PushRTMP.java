package com.md.red5;


import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class PushRTMP {
	static boolean exit = false;

	public static void run()  {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("start push...");
					String rtmpPath = "rtmp://127.0.0.1/oflaDemo/stream1";
					String rtsp = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
					//String rtspPath = "rtmps://localhost:8443/oflaDemo/sssssss"; //oflaDemo
					String rtspPath = "rtmp://127.0.0.1:1935/oflaDemo/double1";

					int audioRecord = 1; // 0 = 不录制，1=录制
					boolean saveVideo = false;
					push(rtmpPath, rtspPath, audioRecord, saveVideo);
					System.out.println("end push...");
				}
			}).start();

		}catch (Exception e)
		{

		}

	}

	@SuppressWarnings("resource")
	private static void push(String rtmpPath, String rtspPath, int audioRecord, boolean saveVideo){
		// 使用rtsp的时候需要使用 FFmpegFrameGrabber，不能再用 FrameGrabber
		int width = 800, height = 600;
		try {
			FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(rtmpPath);
			grabber.setOption("rtsp_transport", "tcp"); // 使用tcp的方式，不然会丢包很严重

			grabber.setImageWidth(width);
			grabber.setImageHeight(height);
			System.out.println("grabber start");
			grabber.start();
			// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
			FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtspPath, width, height, audioRecord);
			recorder.setInterleaved(true);
			 recorder.setVideoOption("crf","28");
			recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 28
			recorder.setAudioOption("crf","0");
		 	 // Highest quality
		 	 recorder.setAudioQuality(0);
		 	 // 192 Kbps
		 	 recorder.setAudioBitrate(192000);
		 	 recorder.setSampleRate(44100);
		 	 recorder.setAudioChannels(1);
		 	 recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
			recorder.setFormat("flv"); // rtmp的类型
			recorder.setFrameRate(20);
			recorder.setImageWidth(width);
			recorder.setImageHeight(height);
			recorder.setPixelFormat(0); // yuv420p
			System.out.println("recorder start");
			recorder.start();
			 
			System.out.println("all start!!"); 
			while (!exit) { 
				Frame frame = grabber.grab();
				if (frame == null) {
//					Thread.sleep(10);
					continue;
				} 
				recorder.record(frame);
				System.out.println("record one frame "+frame.timestamp);
			} 
			grabber.stop();
			grabber.release();
			recorder.stop();
			recorder.release();
		} catch (Exception e) {
			//不抛异常
		} 
	}
}