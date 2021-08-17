package com.util;

//监视内存使用状况
public class MemoryMonitor
{
	// 获得当前可用内存数量（单位MB）
	public static long getUsableMemory()
	{
		Runtime run = Runtime.getRuntime();
		long max = run.maxMemory();
		long total = run.totalMemory();
		long free = run.freeMemory();
		long usable = max - total + free;
		return usable / (1024 * 1024);
	}

	// 获得内存占用率
	public static double getUsedPercent()
	{
		Runtime run = Runtime.getRuntime();
		long max = run.maxMemory() / (1024 * 1024);
		long usable = getUsableMemory();
		double percent = ((max - usable) * 1.0 / max);
		long x = (long) (percent * 100);
		return x / 100.0;
	}

}
