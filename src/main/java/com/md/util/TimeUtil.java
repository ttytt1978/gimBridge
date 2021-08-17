package com.md.util;

//ְ��һ����ʱ������
public class TimeUtil {

	private long startTime;
	private long endTime;

	public TimeUtil() {
		startTime = System.nanoTime();
		endTime = startTime;
	}

	// ��ʼ��ʱ
	public void start() {
		startTime = System.nanoTime();
	}

	// ������ʱ
	public void end() {
		endTime = System.nanoTime();
	}

	// �������ʱ�䣨����Ϊ��λ��
	public double getTimeInMillSecond() {
		return (endTime - startTime) / 1000000.0;
	}

	// �������ʱ�䣨��Ϊ��λ��
	public double getTimeInSecond() {
		return getTimeInMillSecond() / 1000.0;
	}

}
