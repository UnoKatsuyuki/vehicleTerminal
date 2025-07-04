package com.inhand.agv.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "agv_flaw")
public class AgvFlaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "defect_code", unique = true, nullable = false)
    private String defectCode; // 缺陷编号

    @Column(name = "task_id")
    private Long taskId; // 所属任务ID

    @Column(name = "round")
    private Integer round; // 巡视轮次

    @Column(name = "flaw_type")
    private String flawType; // 缺陷类型

    @Column(name = "flaw_name")
    private String flawName; // 缺陷名称

    @Column(name = "flaw_desc")
    private String flawDesc; // 缺陷描述

    @Column(name = "flaw_distance")
    private Double flawDistance; // 距离原点的距离

    @Column(name = "flaw_image")
    private String flawImage; // 缺陷图片路径

    @Column(name = "flaw_image_url")
    private String flawImageUrl; // 缺陷图片URL

    @Column(name = "flaw_rtsp")
    private String flawRtsp; // 缺陷视频流地址

    @Column(name = "shown")
    private Boolean shown; // 是否已弹窗提示

    @Column(name = "confirmed")
    private Boolean confirmed; // 是否确认属实

    @Column(name = "uploaded")
    private Boolean uploaded; // 是否已上传

    @Column(name = "create_time")
    private Date createTime; // 创建时间

    @Column(name = "remark")
    private String remark; // 补充说明

    @Column(name = "flaw_length")
    private Double flawLength; // 缺陷长度

    @Column(name = "flaw_area")
    private Double flawArea; // 缺陷面积

    @Column(name = "flaw_level")
    private String level; // 缺陷等级

    @Column(name = "count_num")
    private Integer countNum; // 缺陷数量

    @Column(name = "delete_flag")
    private Boolean deleteFlag; // 逻辑删除标志

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}