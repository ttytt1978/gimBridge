package com.net;

import java.util.HashMap;
import java.util.Map;

//完成基本的http请求中的参数传入，其post()方法必须由子类实现，以完成通信中的数据发送和结果响应。
abstract class CommonBaseRequest
{
	protected Map<String, String> params = new HashMap<String, String>();// 存放参数
	protected String url = "";// 存放完整的url

	// 使用构造器传入方法名字
	public CommonBaseRequest(String requestName)
	{
		if (null == requestName)
			throw new RuntimeException("传入的requestName不能为空！");
		requestName = requestName.trim();
		if ("".equals(requestName))
			throw new RuntimeException("传入的requestName不能为空！");
		url=buildWholeUrlBy(requestName);
		System.out.println("url----"+url);
	}

	//生成请求的完整url
	private String buildWholeUrlBy(String requestName)
	{
		String urlRoot= RequestConfig.urlRoot;
		if(null==urlRoot)
			return requestName;
		urlRoot=urlRoot.trim();
		if("".equals(urlRoot))
			return requestName;
		char first=urlRoot.charAt(urlRoot.length()-1);
		char second=requestName.charAt(0);
		final char conn='/';
		if(conn!=first&&conn!=second)
			return urlRoot+conn+requestName;
		if(conn==first&&conn==second)
			return urlRoot+requestName.substring(1);
		return urlRoot+requestName;
	}

	// 添加一个参数到map中，可由子类反复多次调用来构建完整的参数列表
	protected void addParam(String name, String value)
	{
		params.put(name, value);
	}

	public abstract void post();

	// // 将参数连接成字符串
	// private String convertParamsToString()
	// {
	// String paraString = "";
	// Set<String> keys = params.keySet();
	// for (String key : keys)
	// {
	// String value = params.get(key);
	// paraString += "&" + key + "=" + value;
	// }
	// if (paraString.length() > 0)
	// paraString = paraString.substring(1);
	// return paraString;
	// }

}
