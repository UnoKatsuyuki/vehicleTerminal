package com.inhand.agv.common;

public class AjaxResult {
    private int code;
    private String msg;
    private Object data;

    public AjaxResult(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static AjaxResult success(Object data) {
        return new AjaxResult(200, "删除任务成功", data);
    }

    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(200, msg, data);
    }

    public static AjaxResult error(String msg) {
        return new AjaxResult(500, msg, null);
    }

    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
