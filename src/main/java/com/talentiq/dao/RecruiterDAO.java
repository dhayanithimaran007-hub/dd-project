package com.talentiq.dao;

import com.talentiq.model.Recruiter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RecruiterDAO extends BaseDAO {

    public Recruiter findById(int recruiterId) {
        String sql = "SELECT r.*, u.full_name, u.email, u.phone " +
                     "FROM recruiters r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.recruiter_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recruiterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding recruiter by id: " + recruiterId, e);
        }
        return null;
    }

    public Recruiter findByUserId(int userId) {
        String sql = "SELECT r.*, u.full_name, u.email, u.phone " +
                     "FROM recruiters r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding recruiter by user id: " + userId, e);
        }
        return null;
    }

    public List<Recruiter> findAll() {
        List<Recruiter> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name, u.email, u.phone " +
                     "FROM recruiters r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "ORDER BY r.company_name, u.full_name";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all recruiters", e);
        }
        return list;
    }

    public int create(Recruiter r) throws SQLException {
        String sql = "INSERT INTO recruiters (user_id, company_name, department, office_location, phone_extension) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setString(2, r.getCompanyName());
            ps.setString(3, r.getDepartment());
            ps.setString(4, r.getOfficeLocation());
            ps.setString(5, r.getPhoneExtension());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    r.setRecruiterId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean update(Recruiter r) {
        String sql = "UPDATE recruiters SET company_name = ?, department = ?, office_location = ?, phone_extension = ? " +
                     "WHERE recruiter_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getCompanyName());
            ps.setString(2, r.getDepartment());
            ps.setString(3, r.getOfficeLocation());
            ps.setString(4, r.getPhoneExtension());
            ps.setInt(5, r.getRecruiterId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating recruiter: " + r.getRecruiterId(), e);
            return false;
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM recruiters";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting recruiters", e);
        }
        return 0;
    }

    private Recruiter mapRow(ResultSet rs) throws SQLException {
        Recruiter r = new Recruiter();
        r.setRecruiterId(rs.getInt("recruiter_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setCompanyName(rs.getString("company_name"));
        r.setDepartment(rs.getString("department"));
        r.setOfficeLocation(rs.getString("office_location"));
        r.setPhoneExtension(rs.getString("phone_extension"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setPhone(rs.getString("phone"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }
}

