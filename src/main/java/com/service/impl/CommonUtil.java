package com.service.impl;

public class CommonUtil {
     ///��ָ���ַ�������Ϊ�̶����ȵ��ַ��������ָ���ַ����ĳ���С��ָ�����ȣ������ַ����Ŀ�ʼλ�ò�һ����"0"�����ָ���ַ����ĳ��ȴ���ָ�����ȣ�
    ///��ȡ�ַ������ָ�����ȵ��Ӵ�
  	public static String addZeroToString(String source, int length)
    {
        String ret;
        int len = source.length();
        if (len > length)
            ret = source.substring(0, length);
        else if (len < length)
        {
            ret = "";
            for (int i = 0; i < length - len; i++)
                ret = ret + "0";
            ret = ret + source;
        }
        else
            ret = source;
        return ret;
    }
}

