package com.net;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//Json请求器：发送http请求并解析返回的字符串为业务对象
public abstract class CommonJsonRequest<T> extends CommonBaseRequest implements IStringResponseListener
{
	private Class<T> TClass;
	private IBusinessResponseListener<T> listener;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CommonJsonRequest(String requestName, IBusinessResponseListener<T> listener)
	{
		super(requestName);
		if (null == listener)
			throw new RuntimeException("业务响应器不能为null！");
		this.listener = listener;
		//通过反射来找到泛型的实际类型Class。
		Type genType = getClass().getGenericSuperclass();
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		TClass = (Class) params[0];
		System.out.println("T Class=" + TClass);
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
	public void responseOk(String json)
	{
		try
		{
			// 解析传回的字符串为业务对象列表
			ObjectMapper mapper = new ObjectMapper();
			//设置输入时忽略JSON字符串中存在而POJO对象实际没有的属性,或者在POJO对象中存在而在Json字符串中没有的属性。
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			JsonNode root = mapper.readTree(json);
			JsonNode message = root.findValue("message");
			JsonNode object = root.findValue("object");
			JsonNode success = root.findValue("success");
//			System.out.println("message-----------" + message.textValue());
//			System.out.println("object-----------" + object.toString());
//			System.out.println("success-----------" + success.booleanValue());
			String objectJson = object.toString();
			if (!success.booleanValue())// 请求结果失败
			{
				System.out.println("数据内容失败：" + message.textValue());
				listener.updateError(message.textValue());
				return;
			}
			if (object.isNull())//请求结果中没有业务对象的数据
			{
				System.out.println("object--------------null");
				listener.updateSuccess(new ArrayList<T>());
				return;
			}
			if (object.isArray())//请求结果中有多个业务对象数据
			{
				List<T> list = new ArrayList<T>();
				Iterator<JsonNode> iterator = object.elements();
				while (iterator.hasNext())
				{
					JsonNode node = iterator.next();
					T tObject = mapper.readValue(node.toString(), TClass);
					list.add(tObject);
				}
				System.out.println("list-------------" + list);
				listener.updateSuccess(list);
				return;
			}
			//请求结果中只有1个业务对象数据
			T tObject = mapper.readValue(objectJson, TClass);
			System.out.println("single----------------"+tObject);
			List<T> list = new ArrayList<T>();
			list.add(tObject);
			listener.updateSuccess(list);
		} catch (Exception e)
		{
			listener.updateError("解析Json字符串失败：" + e.getMessage());
		}
	}

	@Override
	public void post()
	{
		HttpUtil.postForStringInSubthread(url, params, this);
	}

}
