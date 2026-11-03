package com.talentiq.dao;

import com.talentiq.model.SystemSetting;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SystemSettingDAO extends BaseDAO {

    public List<SystemSetting> getAll() {
        List<SystemSetting> list = new ArrayList<>();
        String sql = "SELECT * FROM system_settings ORDER BY setting_key";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SystemSetting s = new SystemSetting();
                s.setSettingKey(rs.getString("setting_key"));
                s.setSettingValue(rs.getString("setting_value"));
                s.setSettingDescription(rs.getString("setting_description"));
                s.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(s);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching system settings", e);
        }
        return list;
    }

    public String get(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("setting_value");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error reading system setting: " + key, e);
        }
        return defaultValue;
    }

    public boolean save(String key, String value, String description) {
        String sql = "INSERT INTO system_settings (setting_key, setting_value, setting_description) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), " +
                     "setting_description = COALESCE(VALUES(setting_description), setting_description)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving system setting: " + key, e);
            return false;
        }
    }
}

