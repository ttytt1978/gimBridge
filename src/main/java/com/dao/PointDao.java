package com.dao;

import com.bean.Point;
import java.util.List;

public interface PointDao {
    public int add(Point point);
    public Point getPointByPointName(String pointName);
    public Point getPointByTableName(String tableName);
    public String getTableNameByPort(int port);
    public List<Integer> getWorkPortList();
    public int update(Point pointManage);
    public List<Point> searchNoCalculatePoint();
}
