package com.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class RawPointDataList implements Serializable {
    private int status;
    private Timestamp datetime;
    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal z;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

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

    @Override
    public String toString() {
        return "RawPointDataList{" +
                "status=" + status +
                ", datetime=" + datetime +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                "}\n";
    }
}
