package com.talentiq.dao;

import com.talentiq.model.MatchDetail;
import com.talentiq.model.MatchResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MatchResultDAO extends BaseDAO {

    public MatchResult findMatch(int candidateId, int jobId) {
        String sql = "SELECT mr.*, u.full_name AS candidate_name, j.title AS job_title " +
                     "FROM match_results mr " +
                     "JOIN candidate_profiles cp ON mr.candidate_id = cp.candidate_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "JOIN jobs j ON mr.job_id = j.job_id " +
                     "WHERE mr.candidate_id = ? AND mr.job_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MatchResult mr = mapResult(rs);
                    mr.setDetails(findDetailsByMatchId(conn, mr.getMatchId()));
                    return mr;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding match result for candidate " + candidateId + " and job " + jobId, e);
        }
        return null;
    }

    public MatchDetail findDetailsByMatchId(Connection conn, int matchId) {
        String sql = "SELECT * FROM match_details WHERE match_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MatchDetail md = new MatchDetail();
                    md.setDetailId(rs.getInt("detail_id"));
                    md.setMatchId(rs.getInt("match_id"));
                    md.setMatchedRequiredSkills(MatchDetail.parseList(rs.getString("matched_required_skills")));
                    md.setMissingRequiredSkills(MatchDetail.parseList(rs.getString("missing_required_skills")));
                    md.setMatchedPreferredSkills(MatchDetail.parseList(rs.getString("matched_preferred_skills")));
                    md.setMissingPreferredSkills(MatchDetail.parseList(rs.getString("missing_preferred_skills")));
                    md.setExperienceCompatibility(rs.getString("experience_compatibility"));
                    md.setEducationCompatibility(rs.getString("education_compatibility"));
                    md.setRecommendations(rs.getString("recommendations"));
                    return md;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding match details for match: " + matchId, e);
        }
        return null;
    }

    public boolean saveOrUpdate(MatchResult mr) {
        String matchSql = "INSERT INTO match_results (candidate_id, job_id, overall_score, required_skill_score, " +
                          "preferred_skill_score, experience_score, education_score, calculated_at) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                          "ON DUPLICATE KEY UPDATE overall_score = VALUES(overall_score), " +
                          "required_skill_score = VALUES(required_skill_score), " +
                          "preferred_skill_score = VALUES(preferred_skill_score), " +
                          "experience_score = VALUES(experience_score), " +
                          "education_score = VALUES(education_score), " +
                          "calculated_at = CURRENT_TIMESTAMP";

        String detailSql = "INSERT INTO match_details (match_id, matched_required_skills, missing_required_skills, " +
                           "matched_preferred_skills, missing_preferred_skills, experience_compatibility, " +
                           "education_compatibility, recommendations) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int matchId = -1;
                try (PreparedStatement ps = conn.prepareStatement(matchSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, mr.getCandidateId());
                    ps.setInt(2, mr.getJobId());
                    ps.setDouble(3, mr.getOverallScore());
                    ps.setDouble(4, mr.getRequiredSkillScore());
                    ps.setDouble(5, mr.getPreferredSkillScore());
                    ps.setDouble(6, mr.getExperienceScore());
                    ps.setDouble(7, mr.getEducationScore());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            matchId = keys.getInt(1);
                        }
                    }
                }

                if (matchId <= 0) {
                    // Fetch existing matchId if updated
                    String getMatchIdSql = "SELECT match_id FROM match_results WHERE candidate_id = ? AND job_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(getMatchIdSql)) {
                        ps.setInt(1, mr.getCandidateId());
                        ps.setInt(2, mr.getJobId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                matchId = rs.getInt(1);
                            }
                        }
                    }
                }

                mr.setMatchId(matchId);

                if (mr.getDetails() != null && matchId > 0) {
                    // Delete prior details
                    try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM match_details WHERE match_id = ?")) {
                        delPs.setInt(1, matchId);
                        delPs.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
                        MatchDetail d = mr.getDetails();
                        ps.setInt(1, matchId);
                        ps.setString(2, MatchDetail.toCommaString(d.getMatchedRequiredSkills()));
                        ps.setString(3, MatchDetail.toCommaString(d.getMissingRequiredSkills()));
                        ps.setString(4, MatchDetail.toCommaString(d.getMatchedPreferredSkills()));
                        ps.setString(5, MatchDetail.toCommaString(d.getMissingPreferredSkills()));
                        ps.setString(6, d.getExperienceCompatibility());
                        ps.setString(7, d.getEducationCompatibility());
                        ps.setString(8, d.getRecommendations());
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving match result", e);
            return false;
        }
    }

    public List<MatchResult> getRankedCandidatesForJob(int jobId, String skillFilter, Double minExp, String eduFilter, Double minScore) {
        List<MatchResult> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT mr.*, u.full_name AS candidate_name, j.title AS job_title, " +
                "cp.total_experience_years, cp.highest_education_level " +
                "FROM match_results mr " +
                "JOIN candidate_profiles cp ON mr.candidate_id = cp.candidate_id " +
                "JOIN users u ON cp.user_id = u.user_id " +
                "JOIN jobs j ON mr.job_id = j.job_id " +
                "WHERE mr.job_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(jobId);

        if (minScore != null && minScore > 0) {
            sql.append("AND mr.overall_score >= ? ");
            params.add(minScore);
        }

        if (minExp != null && minExp > 0) {
            sql.append("AND cp.total_experience_years >= ? ");
            params.add(minExp);
        }

        if (eduFilter != null && !eduFilter.trim().isEmpty() && !"All".equalsIgnoreCase(eduFilter)) {
            sql.append("AND cp.highest_education_level = ? ");
            params.add(eduFilter);
        }

        if (skillFilter != null && !skillFilter.trim().isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM candidate_skills cs " +
                       "JOIN skills s ON cs.skill_id = s.skill_id " +
                       "WHERE cs.candidate_id = cp.candidate_id AND LOWER(s.skill_name) LIKE ?) ");
            params.add("%" + skillFilter.trim().toLowerCase() + "%");
        }

        sql.append("ORDER BY mr.overall_score DESC, mr.required_skill_score DESC, cp.total_experience_years DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchResult mr = mapResult(rs);
                    mr.setDetails(findDetailsByMatchId(conn, mr.getMatchId()));
                    list.add(mr);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error querying ranked candidates for job: " + jobId, e);
        }
        return list;
    }

    public List<MatchResult> getMatchesForCandidate(int candidateId) {
        List<MatchResult> list = new ArrayList<>();
        String sql = "SELECT mr.*, u.full_name AS candidate_name, j.title AS job_title " +
                     "FROM match_results mr " +
                     "JOIN candidate_profiles cp ON mr.candidate_id = cp.candidate_id " +
                     "JOIN users u ON cp.user_id = u.user_id " +
                     "JOIN jobs j ON mr.job_id = j.job_id " +
                     "WHERE mr.candidate_id = ? " +
                     "ORDER BY mr.overall_score DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchResult mr = mapResult(rs);
                    mr.setDetails(findDetailsByMatchId(conn, mr.getMatchId()));
                    list.add(mr);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting matches for candidate: " + candidateId, e);
        }
        return list;
    }

    private MatchResult mapResult(ResultSet rs) throws SQLException {
        MatchResult mr = new MatchResult();
        mr.setMatchId(rs.getInt("match_id"));
        mr.setCandidateId(rs.getInt("candidate_id"));
        mr.setJobId(rs.getInt("job_id"));
        mr.setCandidateName(rs.getString("candidate_name"));
        mr.setJobTitle(rs.getString("job_title"));
        mr.setOverallScore(rs.getDouble("overall_score"));
        mr.setRequiredSkillScore(rs.getDouble("required_skill_score"));
        mr.setPreferredSkillScore(rs.getDouble("preferred_skill_score"));
        mr.setExperienceScore(rs.getDouble("experience_score"));
        mr.setEducationScore(rs.getDouble("education_score"));
        mr.setCalculatedAt(rs.getTimestamp("calculated_at"));
        return mr;
    }
}

