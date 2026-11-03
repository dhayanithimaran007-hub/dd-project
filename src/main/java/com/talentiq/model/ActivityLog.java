package com.talentiq.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class ActivityLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private int logId;
    private Integer userId;
    private String username;
    private String actionType;
    private String description;
    private String ipAddress = "127.0.0.1";
    private Timestamp createdAt;

    public ActivityLog() {}

    public ActivityLog(Integer userId, String actionType, String description) {
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityLog that = (ActivityLog) o;
        return logId == that.logId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }
}

