package com.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil
{
	//判断给定的起始日期和结束日期是否正确（结束日期>=起始日期）
	public static boolean isLegalDateRange(Timestamp startTime,Timestamp endTime)
	{
		try {
			return startTime.getTime()<=endTime.getTime();
		} catch (Exception e) {
			return false;
		}
	}

	// 判断两个日期是否年月日都相同
	public static boolean isSameYearMonthDay(Calendar date1, Calendar date2)
	{
		if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) && date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH))
			return true;
		else
			return false;
	}

	// 判断两个日期是否年月日都相同
	public static boolean isSameYearMonthDay(Date date1, Date date2)
	{
		return isSameYearMonthDay(toCalendar(date1), toCalendar(date2));
	}

	public static Date toDate(Calendar calendar)
	{
		if (calendar == null)
			return null;
		return calendar.getTime();
	}

	public static Calendar toCalendar(Date date)
	{
		if (date == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static Calendar toCalendar(String date)
	{
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar formDate = Calendar.getInstance();
			formDate.setTime(dateFormat.parse(date));
			return formDate;
		} catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	public static Timestamp calendarToTimestamp(Calendar cal)
	{
		return new Timestamp(cal.getTimeInMillis());
	}

	// 将2016-02-25这样的字符串转换成日期
	public static Timestamp toTimestamp(String date)
	{
		if (null == date || "".equals(date))
			return null;
		try
		{
			// String[] str = date.split("/");
			// int year = Integer.parseInt(str[0]);
			// System.out.println("year=" + year);
			// int month = Integer.parseInt(str[1]) - 1;
			// System.out.println("month-1=" + month);
			// int day = Integer.parseInt(str[2]);
			// System.out.println("day=" + day);
			// Calendar calendar = Calendar.getInstance();
			// calendar.set(year, month, day);
			// Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
			// return timestamp;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar formDate = Calendar.getInstance();
			formDate.setTime(dateFormat.parse(date));
			// System.out.println("date:" + formDate);
			Timestamp timestamp = new Timestamp(formDate.getTimeInMillis());
			return timestamp;
		} catch (Exception e)
		{
			throw new RuntimeException("转换字符串到日期出错：" + e.getMessage());
		}

	}

	// 判断给定日期当前是否已经过期，已过期返回true
	public static boolean hasExpiredToday(Date date)
	{
		Calendar expireDate = toCalendar(date);
		Calendar today = toCalendar(new Date());
		if (isSameYearMonthDay(expireDate, today))// 今天与给定日期是同一天
			return false;
		if (today.compareTo(expireDate) < 0)// 今天还未到给定日期
			return false;
		else
			return true;
	}

	public static String toYYYYMMDD(Calendar date)
	{
		String result = "";
		result += date.get(Calendar.YEAR) + "-";
		int month = date.get(Calendar.MONTH) + 1;
		result += String.format("%02d", month) + "-";
		int day = date.get(Calendar.DAY_OF_MONTH);
		result += String.format("%02d", day);
		return result;
	}

	public static String toYYYYMMDDhhmm(Timestamp timestamp)
	{
		if(null==timestamp)
			return null;
		String result = "";
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(timestamp.getTime());
		result += date.get(Calendar.YEAR) + "-";
		int month = date.get(Calendar.MONTH) + 1;
		result += String.format("%02d", month) + "-";
		int day = date.get(Calendar.DAY_OF_MONTH);
		result += String.format("%02d", day);
		int hour = date.get(Calendar.HOUR_OF_DAY);
		result += " "+String.format("%02d", hour)+":";
		int minute = date.get(Calendar.MINUTE);
		result += String.format("%02d", minute);
		return result;
	}

	public static String toYYYYMMDD(Date date)
	{
		Calendar calendar = toCalendar(date);
		return toYYYYMMDD(calendar);
	}

	public static String genLogTime()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int millSecond = calendar.get(Calendar.MILLISECOND);
		String result = "";
		result += "(" + hour + "点" + minute + "分" + second + "秒" + millSecond + "毫秒)";
		return result;
	}

	// 生成给定线程编号及当前时间串
	public static String genLogTime(int threadNumber)
	{
		return "线程" + threadNumber + genLogTime();
	}

	public static String genLogDate()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String result = "";
		result += "(" + year + "年" + month + "月" + day + "日)";
		return result;
	}


	// 处理证件有效期字符串
	public static  String formatZjyxq(String zjyxq) {
		if(null==zjyxq)
			return "";
		// 首先分割成两部分，
		String[] parts=zjyxq.split("-");
		// 对每部分分别格式化
		for(int i=0;i<parts.length;i++)
			parts[i]=formatDateString(parts[i]);
		// 最后合成在一起
		if(parts.length==1)
			return parts[0];
		else
			return parts[0]+"—"+parts[1];
	}

	private static  String formatDateString(String dateString) {
		if (null == dateString)
			return "";
		dateString = dateString.trim();
		if (dateString.length() < 8)
			return dateString;
		try {
			String a = dateString.substring(0, 4);
			String b = dateString.substring(4, 6);
			String c = dateString.substring(6, 8);
//			int year = Integer.parseInt(a);
//			int month = Integer.parseInt(b);
//			int day = Integer.parseInt(c);
			return a + "." + b + "." + c;
		} catch (Exception e) {
			return dateString;
		}

	}

}
