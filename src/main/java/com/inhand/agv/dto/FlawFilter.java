package com.inhand.agv.dto;

import com.inhand.agv.common.PageParam;

public class FlawFilter extends PageParam {
    private String defectCode;
    private Long taskId;
    private String flawType;
    private String flawName;
    private Boolean confirmed;
    private Boolean uploaded;
    private String level;

    public String getDefectCode() {
        return defectCode;
    }

    public void setDefectCode(String defectCode) {
        this.defectCode = defectCode;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getFlawType() {
        return flawType;
    }

    public void setFlawType(String flawType) {
        this.flawType = flawType;
    }

    public String getFlawName() {
        return flawName;
    }

    public void setFlawName(String flawName) {
        this.flawName = flawName;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}