package com.net;

//配置请求的全局参数
public class RequestConfig
{
	static String urlRoot="";

	//设置请求的url根，不设置默认为“”
	public static void setRquestUrlRoot(String root)
	{
		urlRoot=root;
	}

}
