package com.talentiq.dao;

import com.talentiq.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ActivityLogDAO extends BaseDAO {

    public void log(Integer userId, String actionType, String description, String ipAddress) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description, ip_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null && userId > 0) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.setString(4, ipAddress != null ? ipAddress : "127.0.0.1");
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to record activity log", e);
        }
    }

    public List<ActivityLog> getRecent(int limit) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.username " +
                     "FROM activity_logs al " +
                     "LEFT JOIN users u ON al.user_id = u.user_id " +
                     "ORDER BY al.created_at DESC LIMIT " + limit;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId((Integer) rs.getObject("user_id"));
                log.setUsername(rs.getString("username"));
                log.setActionType(rs.getString("action_type"));
                log.setDescription(rs.getString("description"));
                log.setIpAddress(rs.getString("ip_address"));
                log.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(log);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching recent activity logs", e);
        }
        return list;
    }
}

