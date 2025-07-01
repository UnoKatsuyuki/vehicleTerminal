package com.inhand.agv.dto;

public class TaskDTO {
    private Long id;
    private String taskName;
    private String startPos;
    private String taskTrip;
    private String executor;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }
}
