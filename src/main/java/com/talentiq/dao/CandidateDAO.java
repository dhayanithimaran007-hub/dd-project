package com.talentiq.dao;

import com.talentiq.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CandidateDAO extends BaseDAO {

    public CandidateProfile findById(int candidateId) {
        String sql = "SELECT c.*, u.full_name, u.email, u.phone " +
                     "FROM candidate_profiles c " +
                     "JOIN users u ON c.user_id = u.user_id " +
                     "WHERE c.candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CandidateProfile cp = mapRow(rs);
                    loadAssociations(conn, cp);
                    return cp;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding candidate profile by id: " + candidateId, e);
        }
        return null;
    }

    public CandidateProfile findByUserId(int userId) {
        String sql = "SELECT c.*, u.full_name, u.email, u.phone " +
                     "FROM candidate_profiles c " +
                     "JOIN users u ON c.user_id = u.user_id " +
                     "WHERE c.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CandidateProfile cp = mapRow(rs);
                    loadAssociations(conn, cp);
                    return cp;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding candidate profile by user id: " + userId, e);
        }
        return null;
    }

    public List<CandidateProfile> findAll(String keyword, Double minExp, String educationLevel) {
        List<CandidateProfile> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, u.full_name, u.email, u.phone " +
                "FROM candidate_profiles c " +
                "JOIN users u ON c.user_id = u.user_id WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.full_name LIKE ? OR u.email LIKE ? OR c.headline LIKE ? OR c.current_job_title LIKE ? OR c.location LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (minExp != null && minExp > 0) {
            sql.append("AND c.total_experience_years >= ? ");
            params.add(minExp);
        }

        if (educationLevel != null && !educationLevel.trim().isEmpty() && !"All".equalsIgnoreCase(educationLevel)) {
            sql.append("AND c.highest_education_level = ? ");
            params.add(educationLevel);
        }

        sql.append("ORDER BY c.total_experience_years DESC, c.created_at DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CandidateProfile cp = mapRow(rs);
                    loadAssociations(conn, cp);
                    list.add(cp);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error querying candidate profiles", e);
        }
        return list;
    }

    public int create(CandidateProfile p) throws SQLException {
        String sql = "INSERT INTO candidate_profiles (user_id, headline, summary, total_experience_years, " +
                     "current_job_title, current_company, location, highest_education_level, expected_salary, " +
                     "linkedin_url, github_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getUserId());
            ps.setString(2, p.getHeadline());
            ps.setString(3, p.getSummary());
            ps.setDouble(4, p.getTotalExperienceYears());
            ps.setString(5, p.getCurrentJobTitle());
            ps.setString(6, p.getCurrentCompany());
            ps.setString(7, p.getLocation());
            ps.setString(8, p.getHighestEducationLevel());
            ps.setBigDecimal(9, p.getExpectedSalary());
            ps.setString(10, p.getLinkedinUrl());
            ps.setString(11, p.getGithubUrl());
            ps.setString(12, p.getStatus() != null ? p.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    p.setCandidateId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean update(CandidateProfile p) {
        String sql = "UPDATE candidate_profiles SET headline = ?, summary = ?, total_experience_years = ?, " +
                     "current_job_title = ?, current_company = ?, location = ?, highest_education_level = ?, " +
                     "expected_salary = ?, linkedin_url = ?, github_url = ?, status = ? WHERE candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getHeadline());
            ps.setString(2, p.getSummary());
            ps.setDouble(3, p.getTotalExperienceYears());
            ps.setString(4, p.getCurrentJobTitle());
            ps.setString(5, p.getCurrentCompany());
            ps.setString(6, p.getLocation());
            ps.setString(7, p.getHighestEducationLevel());
            ps.setBigDecimal(8, p.getExpectedSalary());
            ps.setString(9, p.getLinkedinUrl());
            ps.setString(10, p.getGithubUrl());
            ps.setString(11, p.getStatus());
            ps.setInt(12, p.getCandidateId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating candidate profile: " + p.getCandidateId(), e);
            return false;
        }
    }

    public boolean delete(int candidateId) {
        String sql = "DELETE FROM candidate_profiles WHERE candidate_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting candidate profile: " + candidateId, e);
            return false;
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM candidate_profiles";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting candidates", e);
        }
        return 0;
    }

    // --- Sub-entity: Skills ---
    public List<CandidateSkill> getSkills(int candidateId) {
        List<CandidateSkill> list = new ArrayList<>();
        String sql = "SELECT cs.*, s.skill_name, sc.category_name " +
                     "FROM candidate_skills cs " +
                     "JOIN skills s ON cs.skill_id = s.skill_id " +
                     "JOIN skill_categories sc ON s.category_id = sc.category_id " +
                     "WHERE cs.candidate_id = ? ORDER BY sc.display_order, s.skill_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CandidateSkill cs = new CandidateSkill();
                    cs.setCandidateSkillId(rs.getInt("candidate_skill_id"));
                    cs.setCandidateId(rs.getInt("candidate_id"));
                    cs.setSkillId(rs.getInt("skill_id"));
                    cs.setSkillName(rs.getString("skill_name"));
                    cs.setCategoryName(rs.getString("category_name"));
                    cs.setProficiencyLevel(rs.getString("proficiency_level"));
                    cs.setYearsOfExperience(rs.getDouble("years_of_experience"));
                    list.add(cs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching skills for candidate: " + candidateId, e);
        }
        return list;
    }

    public boolean addOrUpdateSkill(CandidateSkill cs) {
        String sql = "INSERT INTO candidate_skills (candidate_id, skill_id, proficiency_level, years_of_experience) " +
                     "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE proficiency_level = VALUES(proficiency_level), " +
                     "years_of_experience = VALUES(years_of_experience)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cs.getCandidateId());
            ps.setInt(2, cs.getSkillId());
            ps.setString(3, cs.getProficiencyLevel());
            ps.setDouble(4, cs.getYearsOfExperience());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving candidate skill", e);
            return false;
        }
    }

    public boolean deleteSkill(int candidateId, int skillId) {
        String sql = "DELETE FROM candidate_skills WHERE candidate_id = ? AND skill_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, skillId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting candidate skill", e);
            return false;
        }
    }

    // --- Sub-entity: Education ---
    public List<Education> getEducation(int candidateId) {
        List<Education> list = new ArrayList<>();
        String sql = "SELECT * FROM education WHERE candidate_id = ? ORDER BY end_year DESC, start_year DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Education edu = new Education();
                    edu.setEducationId(rs.getInt("education_id"));
                    edu.setCandidateId(rs.getInt("candidate_id"));
                    edu.setDegree(rs.getString("degree"));
                    edu.setDegreeLevel(rs.getString("degree_level"));
                    edu.setInstitution(rs.getString("institution"));
                    edu.setFieldOfStudy(rs.getString("field_of_study"));
                    edu.setStartYear((Integer) rs.getObject("start_year"));
                    edu.setEndYear((Integer) rs.getObject("end_year"));
                    edu.setGradeGpa(rs.getString("grade_gpa"));
                    list.add(edu);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching education for candidate: " + candidateId, e);
        }
        return list;
    }

    public boolean addEducation(Education edu) {
        String sql = "INSERT INTO education (candidate_id, degree, degree_level, institution, field_of_study, start_year, end_year, grade_gpa) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, edu.getCandidateId());
            ps.setString(2, edu.getDegree());
            ps.setString(3, edu.getDegreeLevel());
            ps.setString(4, edu.getInstitution());
            ps.setString(5, edu.getFieldOfStudy());
            ps.setObject(6, edu.getStartYear());
            ps.setObject(7, edu.getEndYear());
            ps.setString(8, edu.getGradeGpa());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) edu.setEducationId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding education", e);
        }
        return false;
    }

    public boolean deleteEducation(int educationId) {
        String sql = "DELETE FROM education WHERE education_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, educationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting education: " + educationId, e);
            return false;
        }
    }

    // --- Sub-entity: Work Experience ---
    public List<WorkExperience> getExperience(int candidateId) {
        List<WorkExperience> list = new ArrayList<>();
        String sql = "SELECT * FROM work_experience WHERE candidate_id = ? ORDER BY start_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WorkExperience exp = new WorkExperience();
                    exp.setExperienceId(rs.getInt("experience_id"));
                    exp.setCandidateId(rs.getInt("candidate_id"));
                    exp.setJobTitle(rs.getString("job_title"));
                    exp.setCompanyName(rs.getString("company_name"));
                    exp.setLocation(rs.getString("location"));
                    Date sDate = rs.getDate("start_date");
                    if (sDate != null) exp.setStartDate(sDate.toLocalDate());
                    Date eDate = rs.getDate("end_date");
                    if (eDate != null) exp.setEndDate(eDate.toLocalDate());
                    exp.setCurrent(rs.getBoolean("is_current"));
                    exp.setResponsibilities(rs.getString("responsibilities"));
                    exp.setTechnologiesUsed(rs.getString("technologies_used"));
                    list.add(exp);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching work experience for candidate: " + candidateId, e);
        }
        return list;
    }

    public boolean addExperience(WorkExperience exp) {
        String sql = "INSERT INTO work_experience (candidate_id, job_title, company_name, location, start_date, end_date, is_current, responsibilities, technologies_used) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, exp.getCandidateId());
            ps.setString(2, exp.getJobTitle());
            ps.setString(3, exp.getCompanyName());
            ps.setString(4, exp.getLocation());
            ps.setDate(5, exp.getStartDate() != null ? Date.valueOf(exp.getStartDate()) : Date.valueOf(java.time.LocalDate.now()));
            ps.setDate(6, exp.getEndDate() != null ? Date.valueOf(exp.getEndDate()) : null);
            ps.setBoolean(7, exp.isCurrent());
            ps.setString(8, exp.getResponsibilities());
            ps.setString(9, exp.getTechnologiesUsed());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) exp.setExperienceId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding work experience", e);
        }
        return false;
    }

    public boolean deleteExperience(int experienceId) {
        String sql = "DELETE FROM work_experience WHERE experience_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, experienceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting work experience: " + experienceId, e);
            return false;
        }
    }

    // --- Sub-entity: Certifications ---
    public List<Certification> getCertifications(int candidateId) {
        List<Certification> list = new ArrayList<>();
        String sql = "SELECT * FROM certifications WHERE candidate_id = ? ORDER BY issue_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Certification c = new Certification();
                    c.setCertificationId(rs.getInt("certification_id"));
                    c.setCandidateId(rs.getInt("candidate_id"));
                    c.setName(rs.getString("name"));
                    c.setIssuingOrganization(rs.getString("issuing_organization"));
                    Date isDate = rs.getDate("issue_date");
                    if (isDate != null) c.setIssueDate(isDate.toLocalDate());
                    Date exDate = rs.getDate("expiration_date");
                    if (exDate != null) c.setExpirationDate(exDate.toLocalDate());
                    c.setCredentialId(rs.getString("credential_id"));
                    c.setCredentialUrl(rs.getString("credential_url"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching certifications for candidate: " + candidateId, e);
        }
        return list;
    }

    public boolean addCertification(Certification cert) {
        String sql = "INSERT INTO certifications (candidate_id, name, issuing_organization, issue_date, expiration_date, credential_id, credential_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cert.getCandidateId());
            ps.setString(2, cert.getName());
            ps.setString(3, cert.getIssuingOrganization());
            ps.setDate(4, cert.getIssueDate() != null ? Date.valueOf(cert.getIssueDate()) : null);
            ps.setDate(5, cert.getExpirationDate() != null ? Date.valueOf(cert.getExpirationDate()) : null);
            ps.setString(6, cert.getCredentialId());
            ps.setString(7, cert.getCredentialUrl());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) cert.setCertificationId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding certification", e);
        }
        return false;
    }

    public boolean deleteCertification(int certificationId) {
        String sql = "DELETE FROM certifications WHERE certification_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting certification: " + certificationId, e);
            return false;
        }
    }

    // --- Sub-entity: Projects ---
    public List<Project> getProjects(int candidateId) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE candidate_id = ? ORDER BY project_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Project p = new Project();
                    p.setProjectId(rs.getInt("project_id"));
                    p.setCandidateId(rs.getInt("candidate_id"));
                    p.setTitle(rs.getString("title"));
                    p.setRole(rs.getString("role"));
                    p.setDescription(rs.getString("description"));
                    p.setTechnologies(rs.getString("technologies"));
                    p.setProjectUrl(rs.getString("project_url"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching projects for candidate: " + candidateId, e);
        }
        return list;
    }

    public boolean addProject(Project proj) {
        String sql = "INSERT INTO projects (candidate_id, title, role, description, technologies, project_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, proj.getCandidateId());
            ps.setString(2, proj.getTitle());
            ps.setString(3, proj.getRole());
            ps.setString(4, proj.getDescription());
            ps.setString(5, proj.getTechnologies());
            ps.setString(6, proj.getProjectUrl());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) proj.setProjectId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding project", e);
        }
        return false;
    }

    public boolean deleteProject(int projectId) {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting project: " + projectId, e);
            return false;
        }
    }

    private void loadAssociations(Connection conn, CandidateProfile cp) {
        cp.setSkills(getSkills(cp.getCandidateId()));
        cp.setEducationList(getEducation(cp.getCandidateId()));
        cp.setExperienceList(getExperience(cp.getCandidateId()));
        cp.setCertifications(getCertifications(cp.getCandidateId()));
        cp.setProjects(getProjects(cp.getCandidateId()));
    }

    private CandidateProfile mapRow(ResultSet rs) throws SQLException {
        CandidateProfile p = new CandidateProfile();
        p.setCandidateId(rs.getInt("candidate_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setFullName(rs.getString("full_name"));
        p.setEmail(rs.getString("email"));
        p.setPhone(rs.getString("phone"));
        p.setHeadline(rs.getString("headline"));
        p.setSummary(rs.getString("summary"));
        p.setTotalExperienceYears(rs.getDouble("total_experience_years"));
        p.setCurrentJobTitle(rs.getString("current_job_title"));
        p.setCurrentCompany(rs.getString("current_company"));
        p.setLocation(rs.getString("location"));
        p.setHighestEducationLevel(rs.getString("highest_education_level"));
        p.setExpectedSalary(rs.getBigDecimal("expected_salary"));
        p.setLinkedinUrl(rs.getString("linkedin_url"));
        p.setGithubUrl(rs.getString("github_url"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}

