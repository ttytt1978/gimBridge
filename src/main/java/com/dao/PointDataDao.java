package com.dao;

import com.bean.PointData;
import com.bean.PointDataList;
import com.bean.RawPointDataList;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface PointDataDao {

    public int add(PointData pointData, String table_name);

    public PointData getLastLegalData(Timestamp start_time, String table_name);

    public PointData getLastStdData(Timestamp start_time, String table_name);

    public int update(PointData pointData, String table_name);

    public List<PointDataList> getPointDataList(Timestamp start_time, Timestamp end_time, String table_name) throws Exception;

    public List<RawPointDataList> getRawPointDataList(Timestamp start_time, Timestamp end_time, String table_name) throws Exception;

    public int getNoCalculateDataCount(Timestamp start_time, String table_name);

    public List<PointData> getNoCalculateDataList(Timestamp start_time, String table_name);
}
