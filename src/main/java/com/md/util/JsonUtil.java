package com.md.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class JsonUtil {
	private static Random random = new Random();
	// 从一个list中选取第start——end编号中的全部元素（编号从1开始）
	public static <T> List<T> rangeList(List<T> list, int start, int end) {
		if (null == list)
			return new ArrayList<T>();
		List<T> result = new ArrayList<T>();
		start--;
		end--;
		for (int i = start; i <= end; i++)
			if (i < list.size() && i >= 0)
				result.add(list.get(i));
			else
				break;
		return result;
	}

}
