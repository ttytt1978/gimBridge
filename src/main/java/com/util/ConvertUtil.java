package com.util;

import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ConvertUtil {

	public static Timestamp convertDateToTimestamp(Date date) throws RuntimeException {
		try {
			if (null == date)
				return null;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return new Timestamp(calendar.getTimeInMillis());
		} catch (Exception e) {
			throw new RuntimeException("转换时间出错：" + e.getMessage());
		}
	}

	public static Date convertTimestampToDate(Timestamp time) {
		if (null == time)
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		return calendar.getTime();
	}

	// (输入串格式：年-月-日-时-分-秒或者年-月-日，如2017-4-21-19-3-2或2017-4-21)
	public static Timestamp convertStringToTimestamp(String timeStr) throws RuntimeException {
		try {
			Date date = convertStringToTime(timeStr);
			if (null == date)
				return null;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return new Timestamp(calendar.getTimeInMillis());
		} catch (Exception e) {
			throw new RuntimeException("转换时间字符串出错：" + e.getMessage());
		}
	}

	// (格式：年-月-日-时-分-秒，如2017-4-21-19-3-2)
	public static String convertTimestampToString(Timestamp time) {
		if (null == time)
			return "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		int year = (calendar.get(Calendar.YEAR));
		int month = (calendar.get(Calendar.MONTH) + 1);
		int day = (calendar.get(Calendar.DATE));
		int hour = (calendar.get(Calendar.HOUR_OF_DAY));
		int minute = (calendar.get(Calendar.MINUTE));
		int second = (calendar.get(Calendar.SECOND));
		return year + "-" + month + "-" + day + "-" + hour + "-" + minute + "-" + second;
	}

	// (格式：年月，如201704)
	public static String convertTimeToYearMonthString(Date time) {
		if (null == time)
			return "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		int year = (calendar.get(Calendar.YEAR));
		int month = (calendar.get(Calendar.MONTH) + 1);
		return String.format("%04d%02d", year, month);
	}

	// (格式：年-月-日-时-分-秒，如2017-4-21-19-3-2)
	public static String convertTimeToString(Date time) {
		if (null == time)
			return "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		int year = (calendar.get(Calendar.YEAR));
		int month = (calendar.get(Calendar.MONTH) + 1);
		int day = (calendar.get(Calendar.DATE));
		int hour = (calendar.get(Calendar.HOUR_OF_DAY));
		int minute = (calendar.get(Calendar.MINUTE));
		int second = (calendar.get(Calendar.SECOND));
		return year + "-" + month + "-" + day + "-" + hour + "-" + minute + "-" + second;
	}

	// (输入串格式：年-月-日-时-分-秒或者年-月-日，如2017-4-21-19-3-2或2017-4-21)
	public static Date convertStringToTime(String timeStr) throws RuntimeException {
		if (null == timeStr)
			return null;
		timeStr = timeStr.trim();
		if ("".equals(timeStr))
			return null;
		try {
			String[] fileds = timeStr.split("-");
			int year = Integer.parseInt(fileds[0]);
			int month = Integer.parseInt(fileds[1]);
			int day = Integer.parseInt(fileds[2]);
			int hour = 11;
			int minute = 59;
			int second = 59;
			if (fileds.length >= 6) {
				hour = Integer.parseInt(fileds[3]);
				minute = Integer.parseInt(fileds[4]);
				second = Integer.parseInt(fileds[5]);
			}
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month - 1, day, hour, minute, second);
			return calendar.getTime();
		} catch (Exception e) {
			throw new RuntimeException("转换时间字符串出错：" + e.getMessage());
		}
	}

	public static String convertListToString(List<String> list) {
		String result = "";
		if (null == list || list.isEmpty())
			return result;
		result += list.get(0);
		for (int i = 1; i < list.size(); i++)
			result += "," + list.get(i);
		return result;
	}

	public static String convertDateToYYYYMMDD(Date date) {
		if (null == date)
			return "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		return String.format("%04d", year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
	}

	public static double convertMoney(double money) {
		long l = (long) (money * 100);
		money = l / 100.0;
		return money;
	}

	public static Object convertDate(Date date) {
		if (date == null)
			return null;
		Long time = date.getTime();
		return time;
	}

	public static Object convertCalendar(Calendar calendar) {
		if (calendar == null)
			return 0;
		// return 19700101L;
		return convertDate(calendar.getTime());
	}

	public static Object convertImage(BufferedImage image) {
		if (image == null)
			return null;
		return ImageUtil.imageToBytes(image);
	}

	public static String convertNullToBlankString(String str) {
		if (null == str)
			return "";
		return str.trim();
	}

	public static String convertBlankToNullString(String str) {
		if (null == str)
			return null;
		str = str.trim();
		if (str.equals(""))
			return null;
		if (str.equals("null") || str.equals("NULL"))
			return null;
		return str;
	}

	public static List<Integer> convertStringToIdList(String str) {
		List<Integer> list = new ArrayList<Integer>();
		try {
			String[] ids = str.split(",");
			for (String id : ids) {
				int a = Integer.parseInt(id);
				list.add(a);
			}
			return list;

		} catch (Exception e) {
			return list;
		}
	}

	public static List<String> convertStringToList(String str, String splitor) throws RuntimeException {
		List<String> list = new ArrayList<String>();
		try {
			String[] ids = str.split(splitor);
			for (String id : ids)
				list.add(id);
			return list;

		} catch (Exception e) {
			throw new RuntimeException("分解字符串失败：" + splitor);
		}
	}

	// public static Date convertStringToBirthday(String str) throws
	// RuntimeException
	// {
	// try
	// {
	// str = str.replace("年", ",");
	// str = str.replace("月", ",");
	// str = str.replace("日", ",");
	// System.out.println(str);
	// List<String> list = ConvertUtil.convertStringToList(str, ",");
	// int year = Integer.parseInt(list.get(0));
	// int month = Integer.parseInt(list.get(1));
	// int day = Integer.parseInt(list.get(2));
	// Calendar calendar = Calendar.getInstance();
	// calendar.set(year, month - 1, day, 0, 0, 0);
	// return calendar.getTime();
	// } catch (Exception e)
	// {
	// throw new RuntimeException("转换生日字符串失败：" + e.getMessage());
	// }
	//
	// }

	// 字符串为“yyyymmdd”形式
	public static Date convertStringToBirthday(String str) throws RuntimeException {
		try {
			String a = str.substring(0, 4);
			String b = str.substring(4, 6);
			String c = str.substring(6, 8);
			System.out.println(str);
			int year = Integer.parseInt(a);
			int month = Integer.parseInt(b);
			int day = Integer.parseInt(c);
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month - 1, day, 0, 0, 0);
			return calendar.getTime();
		} catch (Exception e) {
			return null;// 以空对象返回
		}

	}

}
