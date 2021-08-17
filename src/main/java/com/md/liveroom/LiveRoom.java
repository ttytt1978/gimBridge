package com.md.liveroom;

import java.io.Serializable;
import java.util.Objects;
import java.util.Random;

//双录直播间
public class LiveRoom implements Serializable {
    private static Random random=new Random();
    private static String Internal_Rtmp_Head="rtmp://127.0.0.1/oflaDemo/";

    private int id;
    private String clientPushPath;
    private String clientPullPath;
    private String clientFilePath;
    private String managerPushPath;
    private String managerPullPath;
    private String managerFilePath;
    private String clientVideoPath;
    private String managerVideoPath;

    private String clientPushPath1;
    private String clientPullPath1;
    private String managerPushPath1;
    private String managerPullPath1;
    private int status;//0-未开始，1-正在直播，2-已结束
    private StreamPusher clientPusher;
    private StreamPusher managerPusher;
    private Mp4Creator clientMp4Creator;
    private Mp4Creator managerMp4Creator;
    private DualStreamMp4Creator dualStreamMp4Creator;

    public  LiveRoom()
    {

    }

    public LiveRoom(String pushPathHead,String pullPathHead,String filePathHead,String videoPathHead)
    {
        this.id=Math.abs(random.nextInt());
        this.clientFilePath=filePathHead+"liveroom"+id+"client.mp4";
        this.clientVideoPath=videoPathHead+"liveroom"+id+"client.mp4";
        this.managerFilePath=filePathHead+"liveroom"+id+"manager.mp4";
        this.managerVideoPath=videoPathHead+"liveroom"+id+"manager.mp4";
        this.clientPushPath=pushPathHead+"clientpush"+id;
        this.managerPushPath=pushPathHead+"managerpush"+id;
        this.clientPullPath=pullPathHead+"clientpull"+id;
        this.managerPullPath=pullPathHead+"managerpull"+id;
        this.clientPushPath1=Internal_Rtmp_Head+"clientpush"+id;
        this.managerPushPath1=Internal_Rtmp_Head+"managerpush"+id;
        this.clientPullPath1=Internal_Rtmp_Head+"clientpull"+id;
        this.managerPullPath1=Internal_Rtmp_Head+"managerpull"+id;
        status=0;
    }


    public void start()
    {
        if(status==1)
            return;
        clientPusher=new StreamPusher(managerPushPath1,clientPushPath1,clientPullPath1);
        managerPusher=new StreamPusher(clientPushPath1,managerPushPath1,managerPullPath1);
        clientMp4Creator=new Mp4Creator(clientPushPath1,clientFilePath);
        managerMp4Creator=new Mp4Creator(managerPushPath1,managerFilePath);
        dualStreamMp4Creator=new DualStreamMp4Creator(clientPushPath1,managerPushPath1,clientFilePath);
//        clientPusher.start();
//        managerPusher.start();
//        clientMp4Creator.start();
//        managerMp4Creator.start();
        dualStreamMp4Creator.start();
        status=1;
    }

    public void stop()
    {
       if(status!=1)
           return;
//        clientPusher.stop();
//        managerPusher.stop();
//        clientMp4Creator.stop();
//        managerMp4Creator.stop();
        dualStreamMp4Creator.stop();
        status=2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientPushPath() {
        return clientPushPath;
    }


    public String getClientVideoPath() {
        return clientVideoPath;
    }

    public void setClientVideoPath(String clientVideoPath) {
        this.clientVideoPath = clientVideoPath;
    }

    public String getManagerVideoPath() {
        return managerVideoPath;
    }

    public void setManagerVideoPath(String managerVideoPath) {
        this.managerVideoPath = managerVideoPath;
    }

    public void setClientPushPath(String clientPushPath) {
        this.clientPushPath = clientPushPath;
    }

    public String getClientPullPath() {
        return clientPullPath;
    }

    public void setClientPullPath(String clientPullPath) {
        this.clientPullPath = clientPullPath;
    }

    public String getClientFilePath() {
        return clientFilePath;
    }

    public void setClientFilePath(String clientFilePath) {
        this.clientFilePath = clientFilePath;
    }

    public String getManagerPushPath() {
        return managerPushPath;
    }

    public void setManagerPushPath(String managerPushPath) {
        this.managerPushPath = managerPushPath;
    }

    public String getManagerPullPath() {
        return managerPullPath;
    }

    public void setManagerPullPath(String managerPullPath) {
        this.managerPullPath = managerPullPath;
    }

    public String getManagerFilePath() {
        return managerFilePath;
    }

    public void setManagerFilePath(String managerFilePath) {
        this.managerFilePath = managerFilePath;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiveRoom liveRoom = (LiveRoom) o;
        return id == liveRoom.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
