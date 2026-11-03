package com.talentiq.dao;

import com.talentiq.model.JobApplication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JobApplicationDAO extends BaseDAO {

    public JobApplication findById(int applicationId) {
        String sql = "SELECT ja.*, j.title AS job_title, r.company_name, u.full_name AS candidate_name, " +
                     "u.email AS candidate_email, cp.total_experience_years " +
                     "FROM job_applications ja " +
                     "JOIN jobs j ON ja.job_id = j.job_id " +
                     "JOIN recruiters r ON j.recruiter_id = r.recruiter_id " +
                     "JOIN candidate_profiles cp ON ja.candidate_id = cp.candidate_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE ja.application_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding application by id: " + applicationId, e);
        }
        return null;
    }

    public JobApplication findExisting(int jobId, int candidateId) {
        String sql = "SELECT ja.*, j.title AS job_title, r.company_name, u.full_name AS candidate_name, " +
                     "u.email AS candidate_email, cp.total_experience_years " +
                     "FROM job_applications ja " +
                     "JOIN jobs j ON ja.job_id = j.job_id " +
                     "JOIN recruiters r ON j.recruiter_id = r.recruiter_id " +
                     "JOIN candidate_profiles cp ON ja.candidate_id = cp.candidate_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE ja.job_id = ? AND ja.candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            ps.setInt(2, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking existing application", e);
        }
        return null;
    }

    public List<JobApplication> findByJob(int jobId) {
        List<JobApplication> list = new ArrayList<>();
        String sql = "SELECT ja.*, j.title AS job_title, r.company_name, u.full_name AS candidate_name, " +
                     "u.email AS candidate_email, cp.total_experience_years " +
                     "FROM job_applications ja " +
                     "JOIN jobs j ON ja.job_id = j.job_id " +
                     "JOIN recruiters r ON j.recruiter_id = r.recruiter_id " +
                     "JOIN candidate_profiles cp ON ja.candidate_id = cp.candidate_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE ja.job_id = ? ORDER BY ja.matching_score_at_application DESC, ja.applied_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching applications for job: " + jobId, e);
        }
        return list;
    }

    public List<JobApplication> findByCandidate(int candidateId) {
        List<JobApplication> list = new ArrayList<>();
        String sql = "SELECT ja.*, j.title AS job_title, r.company_name, u.full_name AS candidate_name, " +
                     "u.email AS candidate_email, cp.total_experience_years " +
                     "FROM job_applications ja " +
                     "JOIN jobs j ON ja.job_id = j.job_id " +
                     "JOIN recruiters r ON j.recruiter_id = r.recruiter_id " +
                     "JOIN candidate_profiles cp ON ja.candidate_id = cp.candidate_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "WHERE ja.candidate_id = ? ORDER BY ja.applied_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching applications for candidate: " + candidateId, e);
        }
        return list;
    }

    public int create(JobApplication app) throws SQLException {
        String sql = "INSERT INTO job_applications (job_id, candidate_id, status, recruiter_notes, matching_score_at_application) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, app.getJobId());
            ps.setInt(2, app.getCandidateId());
            ps.setString(3, app.getStatus() != null ? app.getStatus() : JobApplication.STATUS_APPLIED);
            ps.setString(4, app.getRecruiterNotes());
            ps.setDouble(5, app.getMatchingScoreAtApplication());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    app.setApplicationId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean updateStatus(int applicationId, String status, String notes) {
        String sql = "UPDATE job_applications SET status = ?, recruiter_notes = ? WHERE application_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setInt(3, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating application status: " + applicationId, e);
            return false;
        }
    }

    public boolean delete(int applicationId) {
        String sql = "DELETE FROM job_applications WHERE application_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting application: " + applicationId, e);
            return false;
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM job_applications";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting applications", e);
        }
        return 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM job_applications WHERE status = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting applications by status: " + status, e);
        }
        return 0;
    }

    private JobApplication mapRow(ResultSet rs) throws SQLException {
        JobApplication app = new JobApplication();
        app.setApplicationId(rs.getInt("application_id"));
        app.setJobId(rs.getInt("job_id"));
        app.setCandidateId(rs.getInt("candidate_id"));
        app.setJobTitle(rs.getString("job_title"));
        app.setCompanyName(rs.getString("company_name"));
        app.setCandidateName(rs.getString("candidate_name"));
        app.setCandidateEmail(rs.getString("candidate_email"));
        app.setCandidateTotalExperience(rs.getDouble("total_experience_years"));
        app.setAppliedDate(rs.getTimestamp("applied_date"));
        app.setStatus(rs.getString("status"));
        app.setRecruiterNotes(rs.getString("recruiter_notes"));
        app.setMatchingScoreAtApplication(rs.getDouble("matching_score_at_application"));
        app.setUpdatedAt(rs.getTimestamp("updated_at"));
        return app;
    }
}

