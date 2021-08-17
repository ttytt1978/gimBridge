package com.net;

//获取字符串的回调响应器
public interface IStringResponseListener
{
	//成功获取到字符串后进行处理
	void responseOk(String message);

	//获取失败后进行处理
	void responseError(Exception exception);
}
