package com.talentiq.dao;

import com.talentiq.model.Resume;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ResumeDAO extends BaseDAO {

    public List<Resume> findByCandidateId(int candidateId) {
        List<Resume> list = new ArrayList<>();
        String sql = "SELECT * FROM resumes WHERE candidate_id = ? ORDER BY is_primary DESC, uploaded_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching resumes for candidate: " + candidateId, e);
        }
        return list;
    }

    public Resume findPrimaryByCandidateId(int candidateId) {
        String sql = "SELECT * FROM resumes WHERE candidate_id = ? AND is_primary = TRUE LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding primary resume for candidate: " + candidateId, e);
        }
        return null;
    }

    public Resume findById(int resumeId) {
        String sql = "SELECT * FROM resumes WHERE resume_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resumeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding resume by id: " + resumeId, e);
        }
        return null;
    }

    public int save(Resume r) throws SQLException {
        // If this resume is primary, clear previous primary flags for candidate
        if (r.isPrimary()) {
            clearPrimaryFlag(r.getCandidateId());
        }

        String sql = "INSERT INTO resumes (candidate_id, file_name, stored_file_path, file_type, file_size_bytes, " +
                     "extracted_text, is_primary, parsed_successfully) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getCandidateId());
            ps.setString(2, r.getFileName());
            ps.setString(3, r.getStoredFilePath());
            ps.setString(4, r.getFileType());
            ps.setLong(5, r.getFileSizeBytes());
            ps.setString(6, r.getExtractedText());
            ps.setBoolean(7, r.isPrimary());
            ps.setBoolean(8, r.isParsedSuccessfully());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    r.setResumeId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean setPrimary(int candidateId, int resumeId) {
        clearPrimaryFlag(candidateId);
        String sql = "UPDATE resumes SET is_primary = TRUE WHERE resume_id = ? AND candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resumeId);
            ps.setInt(2, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error setting primary resume", e);
            return false;
        }
    }

    private void clearPrimaryFlag(int candidateId) {
        String sql = "UPDATE resumes SET is_primary = FALSE WHERE candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clearing primary flag for candidate: " + candidateId, e);
        }
    }

    public boolean delete(int resumeId) {
        String sql = "DELETE FROM resumes WHERE resume_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resumeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting resume: " + resumeId, e);
            return false;
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM resumes";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting resumes", e);
        }
        return 0;
    }

    private Resume mapRow(ResultSet rs) throws SQLException {
        Resume r = new Resume();
        r.setResumeId(rs.getInt("resume_id"));
        r.setCandidateId(rs.getInt("candidate_id"));
        r.setFileName(rs.getString("file_name"));
        r.setStoredFilePath(rs.getString("stored_file_path"));
        r.setFileType(rs.getString("file_type"));
        r.setFileSizeBytes(rs.getLong("file_size_bytes"));
        r.setExtractedText(rs.getString("extracted_text"));
        r.setPrimary(rs.getBoolean("is_primary"));
        r.setParsedSuccessfully(rs.getBoolean("parsed_successfully"));
        r.setUploadedAt(rs.getTimestamp("uploaded_at"));
        return r;
    }
}

