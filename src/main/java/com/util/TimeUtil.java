package com.util;

//职责：一个计时工具类
public class TimeUtil {

	private long startTime;
	private long endTime;

	public TimeUtil() {
		startTime = System.nanoTime();
		endTime = startTime;
	}

	// 开始计时
	public void start() {
		startTime = System.nanoTime();
	}

	// 结束计时
	public void end() {
		endTime = System.nanoTime();
	}

	// 获得所费时间（豪秒为单位）
	public double getTimeInMillSecond() {
		return (endTime - startTime) / 1000000.0;
	}

	// 获得所费时间（秒为单位）
	public double getTimeInSecond() {
		return getTimeInMillSecond() / 1000.0;
	}

}
