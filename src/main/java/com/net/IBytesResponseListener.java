package com.net;

//获取字节数据的回调响应器
public interface IBytesResponseListener
{
	//成功获取到字节数据后进行处理
	void responseOk(byte[] data);

	//获取失败后进行处理
	void responseError(Exception exception);
}
