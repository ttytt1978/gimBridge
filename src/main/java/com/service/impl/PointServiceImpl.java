package com.service.impl;

import com.bean.Point;
import com.bean.PointData;
import com.dao.PointDao;
import com.dao.PointDataDao;
import com.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

@Service
public class PointServiceImpl implements PointService {

    @Autowired
    PointDao pointDao;

    @Autowired
    PointDataDao pointDataDao;

    public int add(Point point){
        return pointDao.add(point);
    }

    public Point getPointByPointName(String pointName){
        return pointDao.getPointByPointName(pointName);
    }

    public String getTableNameByPort(int port){
        return pointDao.getTableNameByPort(port);
    }

    public List<Integer> getWorkPortList(){
        return pointDao.getWorkPortList();
    }

    public int update(Point point){
        return pointDao.update(point);
    }

    public void startCalculate(Point point) {
        point.setCalculation_time(new Timestamp(System.currentTimeMillis()));
        point.setStd_x(null);
        point.setStd_y(null);
        point.setStd_z(null);
        pointDao.update(point);
    }

    public List<Point> searchNoCalculatePoint(){
        return pointDao.searchNoCalculatePoint();
    }

    @Transactional
    public void calculateStd(){
        List<Point> list = searchNoCalculatePoint();
        int calculateNumber = 10000;
        for(Point point: list){
            if(pointDataDao.getNoCalculateDataCount(point.getCalculation_time(), point.getTable_name()) >= calculateNumber){
                List<PointData> pointDataList= pointDataDao.getNoCalculateDataList(point.getCalculation_time(), point.getTable_name());
                BigDecimal sumX = BigDecimal.valueOf(0);
                BigDecimal sumY = BigDecimal.valueOf(0);
                BigDecimal sumZ = BigDecimal.valueOf(0);
                for(int i = 0; i < calculateNumber; i++){
                    PointData pointData = pointDataList.get(i);
                    sumX = sumX.add(pointData.getX());
                    sumY = sumY.add(pointData.getY());
                    sumZ = sumZ.add(pointData.getZ());
                }
                sumX = sumX.divide(BigDecimal.valueOf(calculateNumber), RoundingMode.HALF_UP);
                sumY = sumY.divide(BigDecimal.valueOf(calculateNumber), RoundingMode.HALF_UP);
                sumZ = sumZ.divide(BigDecimal.valueOf(calculateNumber), RoundingMode.HALF_UP);
                for(PointData pointData: pointDataList){
                    pointData.setStd_x(sumX);
                    pointData.setStd_y(sumY);
                    pointData.setStd_z(sumZ);
                    pointData.setDiff_x(pointData.getX().subtract(sumX));
                    pointData.setDiff_y(pointData.getY().subtract(sumY));
                    pointData.setDiff_z(pointData.getZ().subtract(sumZ));
                    if(pointData.getDiff_x().abs().compareTo(point.getStd_x_threshold()) > 0){
                        pointData.setEmergency_x(1);
                    }
                    if(pointData.getDiff_y().abs().compareTo(point.getStd_y_threshold()) > 0){
                        pointData.setEmergency_y(1);
                    }
                    if(pointData.getDiff_z().abs().compareTo(point.getStd_z_threshold()) > 0){
                        pointData.setEmergency_z(1);
                    }
                    pointDataDao.update(pointData, point.getTable_name());
                }
                point.setStd_x(sumX);
                point.setStd_y(sumY);
                point.setStd_z(sumZ);
                pointDao.update(point);
            }
        }
    }
}
