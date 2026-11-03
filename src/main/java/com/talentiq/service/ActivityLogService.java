package com.talentiq.service;

import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.model.ActivityLog;

import java.util.List;

public class ActivityLogService {

    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public List<ActivityLog> getRecentActivities(int limit) {
        return activityLogDAO.getRecent(limit);
    }

    public void logActivity(Integer userId, String actionType, String description) {
        activityLogDAO.log(userId, actionType, description, "127.0.0.1");
    }
}

