package com.request;

import com.md.liveroom.LiveRoom;
import com.net.CommonJsonRequest;
import com.net.IBusinessResponseListener;


//创建一个新直播间
public class CreateLiveRoomRequest extends CommonJsonRequest<LiveRoom>
{
	private static final String METHOD_NAME = "CreateLiveRoom";

	public CreateLiveRoomRequest(IBusinessResponseListener<LiveRoom> listener)
	{
		super(METHOD_NAME, listener);		
	}

}
