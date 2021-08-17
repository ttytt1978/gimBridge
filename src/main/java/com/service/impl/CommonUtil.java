package com.service.impl;

public class CommonUtil {
     ///将指定字符串处理为固定长度的字符串，如果指定字符串的长度小于指定长度，则在字符串的开始位置补一定的"0"，如果指定字符串的长度大于指定长度，
    ///则取字符串左端指定长度的子串
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

