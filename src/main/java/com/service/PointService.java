package com.service;

import com.bean.Point;

import java.util.List;

public interface PointService {
    public int add(Point pointManage);
    public Point getPointByPointName(String pointName);
    public List<Integer> getWorkPortList();
    public String getTableNameByPort(int port);
    public int update(Point point);
    public void startCalculate(Point point);
    public List<Point> searchNoCalculatePoint();
    public void calculateStd();
}
