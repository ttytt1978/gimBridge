package com.request;

import com.md.liveroom.LiveRoom;
import com.net.CommonJsonRequest;
import com.net.IBusinessResponseListener;

//获得系统中的全部直播间列表
public class AllLiveRoomsRequest extends CommonJsonRequest<LiveRoom>
{
	private static final String METHOD_NAME = "GetAllLiveRooms";

	public AllLiveRoomsRequest(IBusinessResponseListener<LiveRoom> listener)
	{
		super(METHOD_NAME, listener);
		addParam("start", "1");
		addParam("end", Integer.MAX_VALUE + "");
	}

}
