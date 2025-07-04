package com.inhand.agv.dto;

public class FlawDTO {
    private Long id;
    private String defectCode;
    private Long taskId;
    private Integer round;
    private String flawType;
    private String flawName;
    private String flawDesc;
    private Double flawDistance;
    private String flawImage;
    private String flawImageUrl;
    private String flawRtsp;
    private Boolean shown;
    private Boolean confirmed;
    private Boolean uploaded;
    private String remark;
    private Double flawLength;
    private Double flawArea;
    private String level;
    private Integer countNum;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
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

    public String getFlawDesc() {
        return flawDesc;
    }

    public void setFlawDesc(String flawDesc) {
        this.flawDesc = flawDesc;
    }

    public Double getFlawDistance() {
        return flawDistance;
    }

    public void setFlawDistance(Double flawDistance) {
        this.flawDistance = flawDistance;
    }

    public String getFlawImage() {
        return flawImage;
    }

    public void setFlawImage(String flawImage) {
        this.flawImage = flawImage;
    }

    public String getFlawImageUrl() {
        return flawImageUrl;
    }

    public void setFlawImageUrl(String flawImageUrl) {
        this.flawImageUrl = flawImageUrl;
    }

    public String getFlawRtsp() {
        return flawRtsp;
    }

    public void setFlawRtsp(String flawRtsp) {
        this.flawRtsp = flawRtsp;
    }

    public Boolean getShown() {
        return shown;
    }

    public void setShown(Boolean shown) {
        this.shown = shown;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getFlawLength() {
        return flawLength;
    }

    public void setFlawLength(Double flawLength) {
        this.flawLength = flawLength;
    }

    public Double getFlawArea() {
        return flawArea;
    }

    public void setFlawArea(Double flawArea) {
        this.flawArea = flawArea;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getCountNum() {
        return countNum;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }
}