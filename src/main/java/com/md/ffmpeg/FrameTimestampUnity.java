package com.md.ffmpeg;

import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.List;

//统一设定一组帧的时间戳
public class FrameTimestampUnity {
    private long newStartTimeStamp;//新的起始时间戳
    private long oldStartTimeStamp;//原来帧的起始时间戳
    private boolean haveOldStartTimeStamp=false;

    public FrameTimestampUnity(long newStartTimeStamp) {
        this.newStartTimeStamp = newStartTimeStamp;
    }


    public List<Frame> updateTimeStamp(List<Frame> frames)
    {
        List<Frame> list=new ArrayList<Frame>();
        if(null==frames||frames.isEmpty())
            return list;
        if(!haveOldStartTimeStamp)
        {
            Frame frame=frames.get(0);//默认第1帧为最早时间戳
            oldStartTimeStamp=frame.timestamp;
            haveOldStartTimeStamp=true;
        }
        for(int i=0;i<frames.size();i++)
        {
            Frame frame=frames.get(i);
            long gap=frame.timestamp-oldStartTimeStamp;
            frame.timestamp=newStartTimeStamp+gap;
            list.add(frame);
        }
        return list;
    }


}
