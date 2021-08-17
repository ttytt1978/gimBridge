package com.service;

import com.bean.PointData;
import com.bean.PointDataList;
import com.bean.RawPointDataList;
import com.github.pagehelper.PageInfo;

import java.sql.Timestamp;
import java.util.List;

public interface PointDataService {
    public int add(PointData pointData, String table_name);
    public int update(PointData pointData, String table_name);
    public List<PointDataList> getPointDataList(Timestamp start_time, Timestamp end_time, String table_name) throws Exception;
    public PageInfo<RawPointDataList> getRawPointDataList(Timestamp start_time, Timestamp end_time, String table_name, int index, int pageSize) throws Exception;
}
