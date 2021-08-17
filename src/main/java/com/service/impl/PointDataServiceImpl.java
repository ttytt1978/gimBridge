package com.service.impl;

import com.bean.*;
import com.dao.PointDao;
import com.dao.PointDataDao;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.service.PointDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointDataServiceImpl implements PointDataService {
    @Autowired
    PointDataDao pointDataDao;

    @Autowired
    PointDao pointDao;

    @Transactional
    public int add(PointData pointdata, String table_name){
        if(pointdata.getStatus() == 4) {
            Point point = pointDao.getPointByTableName(table_name);
            PointData lastLegalData = pointDataDao.getLastLegalData(point.getCalculation_time(), table_name);
            PointData lastStdData = pointDataDao.getLastStdData(point.getCalculation_time(), table_name);
            if (lastLegalData == null) {
                pointdata.setBe_std(1);
                pointdata.setBe_legal(1);
            } else {
                if (lastStdData.getX().subtract(pointdata.getX()).abs().compareTo(point.getStd_x_threshold()) <= 0 &&
                        lastStdData.getY().subtract(pointdata.getY()).abs().compareTo(point.getStd_y_threshold()) <= 0 &&
                        lastStdData.getZ().subtract(pointdata.getZ()).abs().compareTo(point.getStd_z_threshold()) <= 0) {
                    pointdata.setBe_std(1);
                } else {
                    pointdata.setBe_std(0);
                }
                if (lastLegalData.getX().subtract(pointdata.getX()).abs().compareTo(point.getLegal_x_threshold()) <= 0 &&
                        lastLegalData.getY().subtract(pointdata.getY()).abs().compareTo(point.getLegal_y_threshold()) <= 0 &&
                        lastLegalData.getZ().subtract(pointdata.getZ()).abs().compareTo(point.getLegal_z_threshold()) <= 0) {
                    pointdata.setBe_legal(1);
                } else {
                    pointdata.setBe_legal(0);
                }
            }
            if (point.getStd_x() != null) {
                pointdata.setStd_x(point.getStd_x());
                pointdata.setStd_y(point.getStd_y());
                pointdata.setStd_z(point.getStd_z());
                BigDecimal diffX = pointdata.getX().subtract(point.getStd_x());
                BigDecimal diffY = pointdata.getY().subtract(point.getStd_y());
                BigDecimal diffZ = pointdata.getZ().subtract(point.getStd_z());
                pointdata.setDiff_x(diffX);
                pointdata.setDiff_y(diffY);
                pointdata.setDiff_z(diffZ);
                if (diffX.abs().compareTo(point.getEmergency_x_threshold()) > 0) {
                    pointdata.setEmergency_x(1);
                }
                else{
                    pointdata.setEmergency_x(0);
                }
                if (diffY.abs().compareTo(point.getEmergency_y_threshold()) > 0) {
                    pointdata.setEmergency_y(1);
                }
                else{
                    pointdata.setEmergency_y(0);
                }
                if (diffZ.abs().compareTo(point.getEmergency_z_threshold()) > 0) {
                    pointdata.setEmergency_z(1);
                }
                else{
                    pointdata.setEmergency_z(0);
                }
            }
        }
        return pointDataDao.add(pointdata, table_name);
    }

    public int update(PointData pointData, String table_name){
        return pointDataDao.update(pointData, table_name);
    }

    public List<PointDataList> getPointDataList(Timestamp start_time, Timestamp end_time, String table_name) throws Exception{
        if(end_time.getTime() - start_time.getTime() > 900000){
            throw(new Exception("时间范围必须小于15分钟！"));
        }
        else {
            return pointDataDao.getPointDataList(start_time, end_time, table_name);
        }
    }

    public PageInfo<RawPointDataList> getRawPointDataList(Timestamp start_time, Timestamp end_time, String table_name, int index, int pageSize) throws Exception{
        if(end_time.getTime() - start_time.getTime() > 900000){
            throw(new Exception("时间范围必须小于15分钟！"));
        }
        else{
            PageHelper.startPage(index, pageSize);
            List<RawPointDataList> list = pointDataDao.getRawPointDataList(start_time, end_time, table_name);
            return new PageInfo<RawPointDataList>(list);
        }
    }
}
