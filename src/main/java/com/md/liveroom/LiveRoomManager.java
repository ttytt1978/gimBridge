package com.md.liveroom;

import java.util.ArrayList;
import java.util.List;

//全部直播间管理器
public class LiveRoomManager {
    private static List<LiveRoom> liverooms = new ArrayList<>();
    public static String pushPathHead = "rtmp://127.0.0.1/oflaDemo/";
    public static String pullPathHead = "rtmp://127.0.0.1/oflaDemo/";
    public static String filePathHead = "c:\\";
    public static String videoPathHead = "http://127.0.0.1/";

    public static List<LiveRoom> getAllLiveRooms() {
        List<LiveRoom> results = new ArrayList<>();
        results.addAll(liverooms);
        return results;
    }

    public static LiveRoom findValidLiveRoomById(int liveroomId) throws RuntimeException {
        LiveRoom liveRoom = null;
        for (LiveRoom each : liverooms)
            if (each.getId() == liveroomId) {
                liveRoom = each;
                break;
            }
        if (liveRoom == null)
            throw new RuntimeException("未找到给定id的直播间！id=" + liveroomId);
        return liveRoom;
    }

    public static LiveRoom createLiveRoom() {
        LiveRoom liveRoom = new LiveRoom(pushPathHead, pullPathHead, filePathHead, videoPathHead);
        liverooms.add(liveRoom);
        return liveRoom;
    }

}
