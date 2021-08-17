package com.md.liveroom;

import java.util.Scanner;

public class TestLiveRoom {

    public static void main(String[] args) {
        try {
            LiveRoom room1=new LiveRoom("rtmp://127.0.0.1/oflaDemo/","rtmp://127.0.0.1/oflaDemo/","c:\\","http://127.0.0.1/video/");
            System.out.println(room1.getClientPushPath());
            System.out.println(room1.getManagerPushPath());
            System.out.println("按回车键开始测试，再按一次停止测试...");
            Scanner input=new Scanner(System.in);
            String line=input.nextLine();
            room1.start();
            line=input.nextLine();
            room1.stop();
            Thread.sleep(3*1000);
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
