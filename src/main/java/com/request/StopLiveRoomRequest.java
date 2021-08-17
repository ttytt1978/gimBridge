package com.request;

import com.md.liveroom.LiveRoom;
import com.net.CommonJsonRequest;
import com.net.IBusinessResponseListener;

//停止一个直播间（结束双录）
public class StopLiveRoomRequest extends CommonJsonRequest<LiveRoom>
{
	private static final String METHOD_NAME = "StopLiveRoom";

	public StopLiveRoomRequest(int liveroomId, IBusinessResponseListener<LiveRoom> listener)
	{
		super(METHOD_NAME, listener);
		addParam("id", liveroomId + "");
	}

}
