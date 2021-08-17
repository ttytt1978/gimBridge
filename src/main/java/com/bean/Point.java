package com.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Point {
    private int id;                 //(监测点编号)
    private String point_name;       //(监测点名称)
    private String unit;            //（所属单位）
    private int port;               //(数据端口号)
    private BigDecimal lon;         //(初始经度)
    private BigDecimal lat;         //(初始纬度)
    private BigDecimal start_x;     //(初始东西)
    private BigDecimal start_y;     //(初始南北)
    private BigDecimal start_z;     //(初始海拔)
    private Timestamp calculation_time; //(计算时间)
    private BigDecimal std_x;       //(东西向基准)
    private BigDecimal std_y;       //(南北向基准)
    private BigDecimal std_z;       //(海拔向基准)
    private BigDecimal legal_x_threshold;   //(东西有效阈值)
    private BigDecimal legal_y_threshold;   //(南北有效阈值)
    private BigDecimal legal_z_threshold;   //(海拔有效阈值)
    private BigDecimal std_x_threshold;     //(东西基准阈值)
    private BigDecimal std_y_threshold;     //(南北基准阈值)
    private BigDecimal std_z_threshold;     //(海拔基准阈值)
    private BigDecimal emergency_x_threshold;   //(东西预警阈值)
    private BigDecimal emergency_y_threshold;   //(南北预警阈值)
    private BigDecimal emergency_z_threshold;   //(海拔预警阈值)
    private String table_name;      //(表名)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPointName() {
        return point_name;
    }

    public void setPointName(String pointName) {
        this.point_name = pointName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getStart_x() {
        return start_x;
    }

    public void setStart_x(BigDecimal start_x) {
        this.start_x = start_x;
    }

    public BigDecimal getStart_y() {
        return start_y;
    }

    public void setStart_y(BigDecimal start_y) {
        this.start_y = start_y;
    }

    public BigDecimal getStart_z() {
        return start_z;
    }

    public void setStart_z(BigDecimal start_z) {
        this.start_z = start_z;
    }

    public Timestamp getCalculation_time() {
        return calculation_time;
    }

    public void setCalculation_time(Timestamp calculation_time) {
        this.calculation_time = calculation_time;
    }

    public BigDecimal getStd_x() {
        return std_x;
    }

    public void setStd_x(BigDecimal std_x) {
        this.std_x = std_x;
    }

    public BigDecimal getStd_y() {
        return std_y;
    }

    public void setStd_y(BigDecimal std_y) {
        this.std_y = std_y;
    }

    public BigDecimal getStd_z() {
        return std_z;
    }

    public void setStd_z(BigDecimal std_z) {
        this.std_z = std_z;
    }

    public BigDecimal getLegal_x_threshold() {
        return legal_x_threshold;
    }

    public void setLegal_x_threshold(BigDecimal legal_x_threshold) {
        this.legal_x_threshold = legal_x_threshold;
    }

    public BigDecimal getLegal_y_threshold() {
        return legal_y_threshold;
    }

    public void setLegal_y_threshold(BigDecimal legal_y_threshold) {
        this.legal_y_threshold = legal_y_threshold;
    }

    public BigDecimal getLegal_z_threshold() {
        return legal_z_threshold;
    }

    public void setLegal_z_threshold(BigDecimal legal_z_threshold) {
        this.legal_z_threshold = legal_z_threshold;
    }

    public BigDecimal getStd_x_threshold() {
        return std_x_threshold;
    }

    public void setStd_x_threshold(BigDecimal std_x_threshold) {
        this.std_x_threshold = std_x_threshold;
    }

    public BigDecimal getStd_y_threshold() {
        return std_y_threshold;
    }

    public void setStd_y_threshold(BigDecimal std_y_threshold) {
        this.std_y_threshold = std_y_threshold;
    }

    public BigDecimal getStd_z_threshold() {
        return std_z_threshold;
    }

    public void setStd_z_threshold(BigDecimal std_z_threshold) {
        this.std_z_threshold = std_z_threshold;
    }

    public BigDecimal getEmergency_x_threshold() {
        return emergency_x_threshold;
    }

    public void setEmergency_x_threshold(BigDecimal emergency_x_threshold) {
        this.emergency_x_threshold = emergency_x_threshold;
    }

    public BigDecimal getEmergency_y_threshold() {
        return emergency_y_threshold;
    }

    public void setEmergency_y_threshold(BigDecimal emergency_y_threshold) {
        this.emergency_y_threshold = emergency_y_threshold;
    }

    public BigDecimal getEmergency_z_threshold() {
        return emergency_z_threshold;
    }

    public void setEmergency_z_threshold(BigDecimal emergency_z_threshold) {
        this.emergency_z_threshold = emergency_z_threshold;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    @Override
    public String toString() {
        return "PointManage{" +
                "id=" + id +
                ", point_name='" + point_name + '\'' +
                ", unit='" + unit + '\'' +
                ", port=" + port +
                ", lon=" + lon +
                ", lat=" + lat +
                ", start_x=" + start_x +
                ", start_y=" + start_y +
                ", start_z=" + start_z +
                ", calculation_time=" + calculation_time +
                ", std_x=" + std_x +
                ", std_y=" + std_y +
                ", std_z=" + std_z +
                ", legal_x_threshold=" + legal_x_threshold +
                ", legal_y_threshold=" + legal_y_threshold +
                ", legal_z_threshold=" + legal_z_threshold +
                ", std_x_threshold=" + std_x_threshold +
                ", std_y_threshold=" + std_y_threshold +
                ", std_z_threshold=" + std_z_threshold +
                ", emergency_x_threshold=" + emergency_x_threshold +
                ", emergency_y_threshold=" + emergency_y_threshold +
                ", emergency_z_threshold=" + emergency_z_threshold +
                ", table_name='" + table_name + '\'' +
                "}\n";
    }
}
