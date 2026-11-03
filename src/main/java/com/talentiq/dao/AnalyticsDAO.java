package com.talentiq.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class AnalyticsDAO extends BaseDAO {

    public Map<String, Integer> getDashboardCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM candidate_profiles) AS candidates, " +
                "(SELECT COUNT(*) FROM recruiters) AS recruiters, " +
                "(SELECT COUNT(*) FROM resumes) AS resumes, " +
                "(SELECT COUNT(*) FROM jobs WHERE status = 'ACTIVE') AS active_jobs, " +
                "(SELECT COUNT(*) FROM jobs) AS total_jobs, " +
                "(SELECT COUNT(*) FROM job_applications) AS applications, " +
                "(SELECT COUNT(*) FROM job_applications WHERE status = 'SHORTLISTED') AS shortlisted, " +
                "(SELECT COUNT(*) FROM job_applications WHERE status = 'HIRED') AS hired";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                counts.put("candidates", rs.getInt("candidates"));
                counts.put("recruiters", rs.getInt("recruiters"));
                counts.put("resumes", rs.getInt("resumes"));
                counts.put("active_jobs", rs.getInt("active_jobs"));
                counts.put("total_jobs", rs.getInt("total_jobs"));
                counts.put("applications", rs.getInt("applications"));
                counts.put("shortlisted", rs.getInt("shortlisted"));
                counts.put("hired", rs.getInt("hired"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching dashboard summary counts", e);
        }
        return counts;
    }

    public Map<String, Integer> getExperienceDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Entry Level (0-2 yrs)", 0);
        map.put("Mid Level (3-5 yrs)", 0);
        map.put("Senior Level (6-8 yrs)", 0);
        map.put("Lead / Principal (8+ yrs)", 0);

        String sql = "SELECT " +
                "SUM(CASE WHEN total_experience_years <= 2.0 THEN 1 ELSE 0 END) AS entry_lvl, " +
                "SUM(CASE WHEN total_experience_years > 2.0 AND total_experience_years <= 5.0 THEN 1 ELSE 0 END) AS mid_lvl, " +
                "SUM(CASE WHEN total_experience_years > 5.0 AND total_experience_years <= 8.0 THEN 1 ELSE 0 END) AS senior_lvl, " +
                "SUM(CASE WHEN total_experience_years > 8.0 THEN 1 ELSE 0 END) AS lead_lvl " +
                "FROM candidate_profiles";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                map.put("Entry Level (0-2 yrs)", rs.getInt("entry_lvl"));
                map.put("Mid Level (3-5 yrs)", rs.getInt("mid_lvl"));
                map.put("Senior Level (6-8 yrs)", rs.getInt("senior_lvl"));
                map.put("Lead / Principal (8+ yrs)", rs.getInt("lead_lvl"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error calculating experience distribution", e);
        }
        return map;
    }

    public Map<String, Integer> getApplicationsPerJob() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT j.title, COUNT(ja.application_id) AS app_count " +
                     "FROM jobs j " +
                     "LEFT JOIN job_applications ja ON j.job_id = ja.job_id " +
                     "GROUP BY j.job_id, j.title " +
                     "ORDER BY app_count DESC LIMIT 10";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("title"), rs.getInt("app_count"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching applications per job", e);
        }
        return map;
    }

    public Map<String, Integer> getTopCandidateSkills(int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT s.skill_name, COUNT(cs.candidate_id) AS cnt " +
                     "FROM skills s " +
                     "JOIN candidate_skills cs ON s.skill_id = cs.skill_id " +
                     "GROUP BY s.skill_id, s.skill_name " +
                     "ORDER BY cnt DESC, s.skill_name ASC LIMIT " + limit;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("skill_name"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching top candidate skills", e);
        }
        return map;
    }

    public Map<String, Integer> getTopDemandedSkills(int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT s.skill_name, COUNT(*) AS cnt FROM (" +
                     "SELECT skill_id FROM job_required_skills " +
                     "UNION ALL " +
                     "SELECT skill_id FROM job_preferred_skills" +
                     ") reqs " +
                     "JOIN skills s ON reqs.skill_id = s.skill_id " +
                     "GROUP BY s.skill_id, s.skill_name " +
                     "ORDER BY cnt DESC, s.skill_name ASC LIMIT " + limit;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("skill_name"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching top demanded skills", e);
        }
        return map;
    }

    public Map<String, Integer> getMatchingScoreDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("0% - 25% (Low)", 0);
        map.put("26% - 50% (Moderate)", 0);
        map.put("51% - 75% (Strong)", 0);
        map.put("76% - 100% (Exceptional)", 0);

        String sql = "SELECT " +
                "SUM(CASE WHEN overall_score <= 25.0 THEN 1 ELSE 0 END) AS r1, " +
                "SUM(CASE WHEN overall_score > 25.0 AND overall_score <= 50.0 THEN 1 ELSE 0 END) AS r2, " +
                "SUM(CASE WHEN overall_score > 50.0 AND overall_score <= 75.0 THEN 1 ELSE 0 END) AS r3, " +
                "SUM(CASE WHEN overall_score > 75.0 THEN 1 ELSE 0 END) AS r4 " +
                "FROM match_results";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                map.put("0% - 25% (Low)", rs.getInt("r1"));
                map.put("26% - 50% (Moderate)", rs.getInt("r2"));
                map.put("51% - 75% (Strong)", rs.getInt("r3"));
                map.put("76% - 100% (Exceptional)", rs.getInt("r4"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching match score distribution", e);
        }
        return map;
    }

    public Map<String, Integer> getApplicationStatusBreakdown() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM job_applications GROUP BY status ORDER BY cnt DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching application status breakdown", e);
        }
        return map;
    }
}

