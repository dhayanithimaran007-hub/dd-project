package com.talentiq.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class SystemSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private String settingKey;
    private String settingValue;
    private String settingDescription;
    private Timestamp updatedAt;

    public SystemSetting() {}

    public SystemSetting(String settingKey, String settingValue, String settingDescription) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingDescription = settingDescription;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingDescription() {
        return settingDescription;
    }

    public void setSettingDescription(String settingDescription) {
        this.settingDescription = settingDescription;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemSetting that = (SystemSetting) o;
        return Objects.equals(settingKey, that.settingKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settingKey);
    }

    @Override
    public String toString() {
        return settingKey + " = " + settingValue;
    }
}

