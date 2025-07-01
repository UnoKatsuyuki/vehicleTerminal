package com.inhand.agv.common;

import java.util.List;

public class TableDataInfo {
    private long total;
    private List<?> rows;
    private int code;
    private String msg;

    public TableDataInfo(long total, List<?> rows) {
        this.total = total;
        this.rows = rows;
        this.code = 200;
        this.msg = "操作成功";
    }

    // Getters and Setters
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
