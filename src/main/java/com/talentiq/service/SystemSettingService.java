package com.talentiq.service;

import com.talentiq.config.AppConfig;
import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.dao.SystemSettingDAO;
import com.talentiq.model.SystemSetting;

import java.util.List;

public class SystemSettingService {

    private final SystemSettingDAO settingDAO = new SystemSettingDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public List<SystemSetting> getAllSettings() {
        return settingDAO.getAll();
    }

    public String getSetting(String key, String defaultValue) {
        return settingDAO.get(key, defaultValue);
    }

    public boolean saveSetting(String key, String value, String description) {
        boolean success = settingDAO.save(key, value, description);
        if (success) {
            // Update runtime weights if changed
            if ("matching.weight.required".equalsIgnoreCase(key)) {
                try { AppConfig.setRequiredSkillWeight(Double.parseDouble(value)); } catch (Exception ignored) {}
            } else if ("matching.weight.preferred".equalsIgnoreCase(key)) {
                try { AppConfig.setPreferredSkillWeight(Double.parseDouble(value)); } catch (Exception ignored) {}
            } else if ("matching.weight.experience".equalsIgnoreCase(key)) {
                try { AppConfig.setExperienceWeight(Double.parseDouble(value)); } catch (Exception ignored) {}
            } else if ("matching.weight.education".equalsIgnoreCase(key)) {
                try { AppConfig.setEducationWeight(Double.parseDouble(value)); } catch (Exception ignored) {}
            }
            activityLogDAO.log(null, "SETTING_UPDATED", "Updated setting '" + key + "' = " + value, "127.0.0.1");
        }
        return success;
    }
}

