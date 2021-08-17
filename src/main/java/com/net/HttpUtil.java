package com.net;

import java.util.Map;
import java.util.Set;

//Http通信工具类
public class HttpUtil
{
	private static final long CONNECTION_TIME_OUT = 25000;
	private static final long SO_TIME_OUT = 30000;
	private static final String CONNECTION_FAILED_MESSAGE = "您的网络有问题，连接失败！";

	// 使用子线程发送http的post请求得到字符串结果并回调字符串响应器
	public static void postForStringInSubthread(final String url, final Map<String, String> params, final IStringResponseListener stringListener)
	{
		Thread newThread = new Thread()
		{
			public void run()
			{
				try
				{
					SimpleWebSubmitor submitor = prepareSubmit(url, params);
//					System.out.println("开始向服务器提交请求...");
					String result = submitor.submitForText();
//					System.out.println("开始响应结果...");
					stringListener.responseOk(result);
				} catch (Exception e)
				{
//					stringListener.responseError(new RuntimeException(CONNECTION_FAILED_MESSAGE));
					stringListener.responseError(new RuntimeException(e.getMessage()+"  "+url));
				}
			}
		};
		newThread.start();// 启动子线程完成http请求和响应
	}

	// 使用子线程发送http的post请求得到字节数组结果并回调字符串响应器
	public static void postForBytesInSubthread(final String url, final Map<String, String> params, final IBytesResponseListener bytesListener)
	{
		Thread newThread = new Thread()
		{
			public void run()
			{
				try
				{
					SimpleWebSubmitor submitor = prepareSubmit(url, params);
					byte[] result = submitor.submitForData();
					bytesListener.responseOk(result);
				} catch (Exception e)
				{
					bytesListener.responseError(new RuntimeException(CONNECTION_FAILED_MESSAGE));
				}
			}
		};
		newThread.start();// 启动子线程完成http请求和响应
	}

	private static SimpleWebSubmitor prepareSubmit(final String url, final Map<String, String> params)
	{
		SimpleWebSubmitor submitor = new SimpleWebSubmitor(url);
		submitor.setConnectionTimeout(CONNECTION_TIME_OUT);
		submitor.setSoTimeout(SO_TIME_OUT);
		submitor.setCharset("UTF-8");
		submitor.setSubmitMethod(SimpleWebSubmitor.POST);
		// 添加每个参数
		Set<String> keys = params.keySet();
		for (String key : keys)
			submitor.addFormData(key, params.get(key));
		return submitor;
	}

}
