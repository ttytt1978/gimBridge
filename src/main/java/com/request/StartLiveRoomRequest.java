package com.request;

import com.md.liveroom.LiveRoom;
import com.net.CommonJsonRequest;
import com.net.IBusinessResponseListener;

//启动一个直播间（开始双录）
public class StartLiveRoomRequest extends CommonJsonRequest<LiveRoom>
{
	private static final String METHOD_NAME = "StartLiveRoom";

	public StartLiveRoomRequest(int liveroomId, IBusinessResponseListener<LiveRoom> listener)
	{
		super(METHOD_NAME, listener);
		addParam("id", liveroomId + "");
	}

}
