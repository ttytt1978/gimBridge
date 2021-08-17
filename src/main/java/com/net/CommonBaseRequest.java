package com.net;

import java.util.HashMap;
import java.util.Map;

//��ɻ�����http�����еĲ������룬��post()��������������ʵ�֣������ͨ���е����ݷ��ͺͽ����Ӧ��
abstract class CommonBaseRequest
{
	protected Map<String, String> params = new HashMap<String, String>();// ��Ų���
	protected String url = "";// ���������url

	// ʹ�ù��������뷽������
	public CommonBaseRequest(String requestName)
	{
		if (null == requestName)
			throw new RuntimeException("�����requestName����Ϊ�գ�");
		requestName = requestName.trim();
		if ("".equals(requestName))
			throw new RuntimeException("�����requestName����Ϊ�գ�");
		url=buildWholeUrlBy(requestName);
		System.out.println("url----"+url);
	}

	//�������������url
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

	// ���һ��������map�У��������෴����ε��������������Ĳ����б�
	protected void addParam(String name, String value)
	{
		params.put(name, value);
	}

	public abstract void post();

	// // ���������ӳ��ַ���
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
