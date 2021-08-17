package com.maincontroller;

import com.md.liveroom.LiveRoom;
import com.md.liveroom.LiveRoomManager;
import com.md.util.JsonUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class LiveRoomController {

    @RequestMapping("/QueryLiveRoomList")
    @ResponseBody
    public List<LiveRoom> queryLiveRoomList(int start, int end)
    {
        return JsonUtil.rangeList(LiveRoomManager.getAllLiveRooms(),start,end);
    }

    @RequestMapping("/QueryLiveRoomListSum")
    @ResponseBody
    public int queryLiveRoomListSum()
    {
        return LiveRoomManager.getAllLiveRooms().size();
    }

    @RequestMapping("/GetAllLiveRooms")
    @ResponseBody
    public List<LiveRoom> getAll()
    {
       return LiveRoomManager.getAllLiveRooms();
    }

    @RequestMapping("/LiveRoom")
    @ResponseBody
    public LiveRoom find(int id)
    {
        return LiveRoomManager.findValidLiveRoomById(id);
    }

    @RequestMapping("/StartLiveRoom")
    @ResponseBody
    public void startLiveRoom(int id)
    {
        LiveRoom room= LiveRoomManager.findValidLiveRoomById(id);
        room.start();
    }

    @RequestMapping("/StopLiveRoom")
    @ResponseBody
    public void stopLiveRoom(int id)
    {
        LiveRoom room= LiveRoomManager.findValidLiveRoomById(id);
        room.stop();
    }

    @RequestMapping("/CreateLiveRoom")
    @ResponseBody
    public LiveRoom createLiveRoom()
    {
        return LiveRoomManager.createLiveRoom();
    }
}
