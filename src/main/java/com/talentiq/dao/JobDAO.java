package com.talentiq.dao;

import com.talentiq.model.Job;
import com.talentiq.model.JobSkill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JobDAO extends BaseDAO {

    public Job findById(int jobId) {
        String sql = "SELECT j.*, r.company_name, u.full_name AS recruiter_name, " +
                     "(SELECT COUNT(*) FROM job_applications ja WHERE ja.job_id = j.job_id) AS app_count " +
                     "FROM jobs j " +
                     "JOIN recruiters r ON j.recruiter_id = r.recruiter_id " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE j.job_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Job job = mapRow(rs);
                    job.setRequiredSkills(getRequiredSkills(jobId));
                    job.setPreferredSkills(getPreferredSkills(jobId));
                    return job;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding job by id: " + jobId, e);
        }
        return null;
    }

    public List<Job> findAll(String keyword, String status, String employmentType, Integer recruiterId) {
        List<Job> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT j.*, r.company_name, u.full_name AS recruiter_name, " +
                "(SELECT COUNT(*) FROM job_applications ja WHERE ja.job_id = j.job_id) AS app_count " +
                "FROM jobs j " +
                "JOIN recruiters r ON j.recruiter_id = r.recruiter_id " +
                "JOIN users u ON r.user_id = u.user_id WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (j.title LIKE ? OR j.description LIKE ? OR j.department LIKE ? OR r.company_name LIKE ? OR j.location LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            sql.append("AND j.status = ? ");
            params.add(status.toUpperCase());
        }

        if (employmentType != null && !employmentType.trim().isEmpty() && !"All".equalsIgnoreCase(employmentType)) {
            sql.append("AND j.employment_type = ? ");
            params.add(employmentType);
        }

        if (recruiterId != null && recruiterId > 0) {
            sql.append("AND j.recruiter_id = ? ");
            params.add(recruiterId);
        }

        sql.append("ORDER BY j.created_at DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Job job = mapRow(rs);
                    job.setRequiredSkills(getRequiredSkills(job.getJobId()));
                    job.setPreferredSkills(getPreferredSkills(job.getJobId()));
                    list.add(job);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error querying jobs", e);
        }
        return list;
    }

    public int create(Job job) throws SQLException {
        String sql = "INSERT INTO jobs (recruiter_id, title, department, description, min_experience_years, " +
                     "min_education_level, employment_type, location, salary_min, salary_max, status, deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, job.getRecruiterId());
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getDepartment());
            ps.setString(4, job.getDescription());
            ps.setDouble(5, job.getMinExperienceYears());
            ps.setString(6, job.getMinEducationLevel());
            ps.setString(7, job.getEmploymentType());
            ps.setString(8, job.getLocation());
            ps.setBigDecimal(9, job.getSalaryMin());
            ps.setBigDecimal(10, job.getSalaryMax());
            ps.setString(11, job.getStatus() != null ? job.getStatus() : Job.STATUS_ACTIVE);
            ps.setDate(12, job.getDeadline() != null ? Date.valueOf(job.getDeadline()) : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    job.setJobId(id);
                    setRequiredSkills(id, job.getRequiredSkills());
                    setPreferredSkills(id, job.getPreferredSkills());
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean update(Job job) {
        String sql = "UPDATE jobs SET title = ?, department = ?, description = ?, min_experience_years = ?, " +
                     "min_education_level = ?, employment_type = ?, location = ?, salary_min = ?, salary_max = ?, " +
                     "status = ?, deadline = ? WHERE job_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDepartment());
            ps.setString(3, job.getDescription());
            ps.setDouble(4, job.getMinExperienceYears());
            ps.setString(5, job.getMinEducationLevel());
            ps.setString(6, job.getEmploymentType());
            ps.setString(7, job.getLocation());
            ps.setBigDecimal(8, job.getSalaryMin());
            ps.setBigDecimal(9, job.getSalaryMax());
            ps.setString(10, job.getStatus());
            ps.setDate(11, job.getDeadline() != null ? Date.valueOf(job.getDeadline()) : null);
            ps.setInt(12, job.getJobId());
            boolean success = ps.executeUpdate() > 0;
            if (success) {
                setRequiredSkills(job.getJobId(), job.getRequiredSkills());
                setPreferredSkills(job.getJobId(), job.getPreferredSkills());
            }
            return success;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating job: " + job.getJobId(), e);
            return false;
        }
    }

    public boolean updateStatus(int jobId, String status) {
        String sql = "UPDATE jobs SET status = ? WHERE job_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, jobId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating job status: " + jobId, e);
            return false;
        }
    }

    public boolean delete(int jobId) {
        String sql = "DELETE FROM jobs WHERE job_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting job: " + jobId, e);
            return false;
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM jobs";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting jobs", e);
        }
        return 0;
    }

    // --- Required Skills ---
    public List<JobSkill> getRequiredSkills(int jobId) {
        List<JobSkill> list = new ArrayList<>();
        String sql = "SELECT jrs.*, s.skill_name, sc.category_name " +
                     "FROM job_required_skills jrs " +
                     "JOIN skills s ON jrs.skill_id = s.skill_id " +
                     "JOIN skill_categories sc ON s.category_id = sc.category_id " +
                     "WHERE jrs.job_id = ? ORDER BY sc.display_order, s.skill_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JobSkill js = new JobSkill();
                    js.setJobSkillId(rs.getInt("job_required_skill_id"));
                    js.setJobId(rs.getInt("job_id"));
                    js.setSkillId(rs.getInt("skill_id"));
                    js.setSkillName(rs.getString("skill_name"));
                    js.setCategoryName(rs.getString("category_name"));
                    js.setMinProficiency(rs.getString("min_proficiency"));
                    js.setRequired(true);
                    list.add(js);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting required skills for job: " + jobId, e);
        }
        return list;
    }

    public void setRequiredSkills(int jobId, List<JobSkill> skills) {
        String deleteSql = "DELETE FROM job_required_skills WHERE job_id = ?";
        String insertSql = "INSERT INTO job_required_skills (job_id, skill_id, min_proficiency) VALUES (?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delPs = conn.prepareStatement(deleteSql);
                 PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                delPs.setInt(1, jobId);
                delPs.executeUpdate();

                if (skills != null) {
                    for (JobSkill s : skills) {
                        insPs.setInt(1, jobId);
                        insPs.setInt(2, s.getSkillId());
                        insPs.setString(3, s.getMinProficiency() != null ? s.getMinProficiency() : "Intermediate");
                        insPs.addBatch();
                    }
                    insPs.executeBatch();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error setting required skills for job: " + jobId, e);
        }
    }

    // --- Preferred Skills ---
    public List<JobSkill> getPreferredSkills(int jobId) {
        List<JobSkill> list = new ArrayList<>();
        String sql = "SELECT jps.*, s.skill_name, sc.category_name " +
                     "FROM job_preferred_skills jps " +
                     "JOIN skills s ON jps.skill_id = s.skill_id " +
                     "JOIN skill_categories sc ON s.category_id = sc.category_id " +
                     "WHERE jps.job_id = ? ORDER BY sc.display_order, s.skill_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JobSkill js = new JobSkill();
                    js.setJobSkillId(rs.getInt("job_preferred_skill_id"));
                    js.setJobId(rs.getInt("job_id"));
                    js.setSkillId(rs.getInt("skill_id"));
                    js.setSkillName(rs.getString("skill_name"));
                    js.setCategoryName(rs.getString("category_name"));
                    js.setMinProficiency(rs.getString("min_proficiency"));
                    js.setRequired(false);
                    list.add(js);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting preferred skills for job: " + jobId, e);
        }
        return list;
    }

    public void setPreferredSkills(int jobId, List<JobSkill> skills) {
        String deleteSql = "DELETE FROM job_preferred_skills WHERE job_id = ?";
        String insertSql = "INSERT INTO job_preferred_skills (job_id, skill_id, min_proficiency) VALUES (?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delPs = conn.prepareStatement(deleteSql);
                 PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                delPs.setInt(1, jobId);
                delPs.executeUpdate();

                if (skills != null) {
                    for (JobSkill s : skills) {
                        insPs.setInt(1, jobId);
                        insPs.setInt(2, s.getSkillId());
                        insPs.setString(3, s.getMinProficiency() != null ? s.getMinProficiency() : "Beginner");
                        insPs.addBatch();
                    }
                    insPs.executeBatch();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error setting preferred skills for job: " + jobId, e);
        }
    }

    private Job mapRow(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setJobId(rs.getInt("job_id"));
        job.setRecruiterId(rs.getInt("recruiter_id"));
        job.setCompanyName(rs.getString("company_name"));
        job.setRecruiterName(rs.getString("recruiter_name"));
        job.setTitle(rs.getString("title"));
        job.setDepartment(rs.getString("department"));
        job.setDescription(rs.getString("description"));
        job.setMinExperienceYears(rs.getDouble("min_experience_years"));
        job.setMinEducationLevel(rs.getString("min_education_level"));
        job.setEmploymentType(rs.getString("employment_type"));
        job.setLocation(rs.getString("location"));
        job.setSalaryMin(rs.getBigDecimal("salary_min"));
        job.setSalaryMax(rs.getBigDecimal("salary_max"));
        job.setStatus(rs.getString("status"));
        Date deadline = rs.getDate("deadline");
        if (deadline != null) job.setDeadline(deadline.toLocalDate());
        job.setCreatedAt(rs.getTimestamp("created_at"));
        job.setUpdatedAt(rs.getTimestamp("updated_at"));
        try {
            job.setApplicationCount(rs.getInt("app_count"));
        } catch (Exception ignored) {}
        return job;
    }
}

