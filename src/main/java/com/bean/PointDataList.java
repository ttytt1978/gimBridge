package com.bean;

import org.red5.io.object.Serializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class PointDataList implements Serializable {
    private Timestamp datetime;         //(监测时间)
    private BigDecimal x;               //(东西向测量值)
    private BigDecimal y;               //(南北向测量值)
    private BigDecimal z;               //(海拔向测量值)
    private BigDecimal std_x;           //(东西向基准)
    private BigDecimal std_y;           //(南北向基准)
    private BigDecimal std_z;           //(海拔向基准)
    private BigDecimal diff_x;          //(东西向误差)
    private BigDecimal diff_y;          //(南北向误差)
    private BigDecimal diff_z;          //(海拔向误差)
    private int emergency_x;            //(东西向预警)
    private int emergency_y;            //(南北向预警)
    private int emergency_z;            //(海拔向预警)

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public BigDecimal getX() {
        return x;
    }

    public void setX(BigDecimal x) {
        this.x = x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }

    public BigDecimal getZ() {
        return z;
    }

    public void setZ(BigDecimal z) {
        this.z = z;
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

    public BigDecimal getDiff_x() {
        return diff_x;
    }

    public void setDiff_x(BigDecimal diff_x) {
        this.diff_x = diff_x;
    }

    public BigDecimal getDiff_y() {
        return diff_y;
    }

    public void setDiff_y(BigDecimal diff_y) {
        this.diff_y = diff_y;
    }

    public BigDecimal getDiff_z() {
        return diff_z;
    }

    public void setDiff_z(BigDecimal diff_z) {
        this.diff_z = diff_z;
    }

    public int getEmergency_x() {
        return emergency_x;
    }

    public void setEmergency_x(int emergency_x) {
        this.emergency_x = emergency_x;
    }

    public int getEmergency_y() {
        return emergency_y;
    }

    public void setEmergency_y(int emergency_y) {
        this.emergency_y = emergency_y;
    }

    public int getEmergency_z() {
        return emergency_z;
    }

    public void setEmergency_z(int emergency_z) {
        this.emergency_z = emergency_z;
    }

    @Override
    public String toString() {
        return "PointDataList{" +
                "datetime=" + datetime +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", std_x=" + std_x +
                ", std_y=" + std_y +
                ", std_z=" + std_z +
                ", diff_x=" + diff_x +
                ", diff_y=" + diff_y +
                ", diff_z=" + diff_z +
                ", emergency_x=" + emergency_x +
                ", emergency_y=" + emergency_y +
                ", emergency_z=" + emergency_z +
                "}\n";
    }
}
