package com.net;

import java.util.ArrayList;
import java.util.List;

//Data请求器：发送http请求并解析返回的字节数据为业务对象
public abstract class CommonDataRequest<T> extends CommonBaseRequest implements IBytesResponseListener
{
	private IBusinessResponseListener<T> listener;

	public CommonDataRequest(String requestName, IBusinessResponseListener<T> listener)
	{
		super(requestName);
		if (null == listener)
			throw new RuntimeException("业务响应器不能为null！");
		this.listener = listener;
	}

	@Override
	public void responseError(Exception exception)
	{
		try
		{ // 将失败消息通知给业务响应器
			listener.updateError(exception.getMessage());
		} catch (Exception e)
		{
			// 业务响应器的异常不做处理，留给它自己处理
		}
	}

	@Override
	public void responseOk(byte[] data)
	{
		try
		{
			// 解析传回的字节数据为业务对象列表
			T tObject=parseData(data);
			List<T> list=new ArrayList<T>();
			list.add(tObject);
			listener.updateSuccess(list);
		} catch (Exception e)
		{
			listener.updateError("解析字节数据失败：" + e.getMessage());
		}
	}

	@Override
	public void post()
	{
		HttpUtil.postForBytesInSubthread(url, params, this);
	}

	//要求子类实现将字节数据解析成业务对象的职责
	protected abstract T parseData(byte[] data);

}
