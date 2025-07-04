package com.inhand.agv.entity;

import javax.persistence.*; // 引入JPA注解
import java.util.Date;

@Entity // 标记这是一个JPA实体
@Table(name = "agv_task") // 映射到数据库表名
public class AgvTask {
    @Id // 标记为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键生成策略，金仓可能需要SEQUENCE或AUTO
    private Long id;

    @Column(name = "task_code", unique = true, nullable = false) // 映射到列名，unique表示唯一，nullable表示非空
    private String taskCode;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "start_pos", nullable = false)
    private String startPos;

    @Column(name = "task_trip", nullable = false)
    private String taskTrip;

    @Column(name = "creator", nullable = false)
    private String creator;

    @Column(name = "executor", nullable = false)
    private String executor;

    @Column(name = "exec_time")
    private Date execTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "create_time", nullable = false)
    private Date createTime;

    @Column(name = "task_status", nullable = false)
    private String taskStatus;

    @Column(name = "round")
    private Integer round;

    @Column(name = "uploaded")
    private Boolean uploaded;

    @Column(name = "remark")
    private String remark;

    @Column(name = "cloud_task_id")
    private Long cloudTaskId;

    @Column(name = "delete_flag", nullable = false)
    private Boolean deleteFlag;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStartPos() {
        return startPos;
    }

    public void setStartPos(String startPos) {
        this.startPos = startPos;
    }

    public String getTaskTrip() {
        return taskTrip;
    }

    public void setTaskTrip(String taskTrip) {
        this.taskTrip = taskTrip;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Date getExecTime() {
        return execTime;
    }

    public void setExecTime(Date execTime) {
        this.execTime = execTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
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

    public Long getCloudTaskId() {
        return cloudTaskId;
    }

    public void setCloudTaskId(Long cloudTaskId) {
        this.cloudTaskId = cloudTaskId;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}