-- ==============================================================================
-- TalentIQ – Resume & Skill Matching Management System
-- Database Schema: talentiq_db
-- Normalized MySQL 8.x DDL Schema
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS talentiq_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE talentiq_db;

-- Disable foreign key checks during initialization
SET FOREIGN_KEY_CHECKS = 0;

-- 1. ROLES TABLE
DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. USERS TABLE
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    role_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- 3. CANDIDATE PROFILES TABLE
DROP TABLE IF EXISTS candidate_profiles;
CREATE TABLE candidate_profiles (
    candidate_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    headline VARCHAR(200),
    summary TEXT,
    total_experience_years DECIMAL(4, 1) NOT NULL DEFAULT 0.0,
    current_job_title VARCHAR(120),
    current_company VARCHAR(120),
    location VARCHAR(120),
    highest_education_level VARCHAR(60) NOT NULL DEFAULT 'Bachelor',
    expected_salary DECIMAL(12, 2) DEFAULT 0.00,
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidate_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_candidate_exp ON candidate_profiles(total_experience_years);
CREATE INDEX idx_candidate_location ON candidate_profiles(location);

-- 4. RECRUITERS TABLE
DROP TABLE IF EXISTS recruiters;
CREATE TABLE recruiters (
    recruiter_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    company_name VARCHAR(150) NOT NULL,
    department VARCHAR(100),
    office_location VARCHAR(120),
    phone_extension VARCHAR(30),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recruiter_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. RESUMES TABLE
DROP TABLE IF EXISTS resumes;
CREATE TABLE resumes (
    resume_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    stored_file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL, -- PDF, DOCX, TXT
    file_size_bytes BIGINT NOT NULL,
    extracted_text MEDIUMTEXT,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    parsed_successfully BOOLEAN NOT NULL DEFAULT TRUE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resumes_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_resumes_candidate ON resumes(candidate_id);

-- 6. EDUCATION TABLE
DROP TABLE IF EXISTS education;
CREATE TABLE education (
    education_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    degree VARCHAR(150) NOT NULL,
    degree_level VARCHAR(60) NOT NULL, -- Doctorate, Master, Bachelor, Associate, High School
    institution VARCHAR(200) NOT NULL,
    field_of_study VARCHAR(150),
    start_year INT,
    end_year INT,
    grade_gpa VARCHAR(30),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_edu_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_edu_candidate ON education(candidate_id);

-- 7. WORK EXPERIENCE TABLE
DROP TABLE IF EXISTS work_experience;
CREATE TABLE work_experience (
    experience_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    job_title VARCHAR(150) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    location VARCHAR(120),
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    responsibilities TEXT,
    technologies_used VARCHAR(300),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exp_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_exp_candidate ON work_experience(candidate_id);

-- 8. SKILL CATEGORIES TABLE
DROP TABLE IF EXISTS skill_categories;
CREATE TABLE skill_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 9. SKILLS TABLE
DROP TABLE IF EXISTS skills;
CREATE TABLE skills (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    aliases VARCHAR(300),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skills_category FOREIGN KEY (category_id) REFERENCES skill_categories(category_id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE INDEX idx_skills_name ON skills(skill_name);

-- 10. CANDIDATE SKILLS TABLE
DROP TABLE IF EXISTS candidate_skills;
CREATE TABLE candidate_skills (
    candidate_skill_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    skill_id INT NOT NULL,
    proficiency_level VARCHAR(30) NOT NULL DEFAULT 'Intermediate', -- Beginner, Intermediate, Advanced, Expert
    years_of_experience DECIMAL(4, 1) NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_candidate_skill (candidate_id, skill_id),
    CONSTRAINT fk_cskill_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE,
    CONSTRAINT fk_cskill_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. CERTIFICATIONS TABLE
DROP TABLE IF EXISTS certifications;
CREATE TABLE certifications (
    certification_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    issuing_organization VARCHAR(200) NOT NULL,
    issue_date DATE,
    expiration_date DATE,
    credential_id VARCHAR(150),
    credential_url VARCHAR(300),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. PROJECTS TABLE
DROP TABLE IF EXISTS projects;
CREATE TABLE projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    role VARCHAR(120),
    description TEXT,
    technologies VARCHAR(300),
    project_url VARCHAR(300),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_proj_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. JOBS TABLE
DROP TABLE IF EXISTS jobs;
CREATE TABLE jobs (
    job_id INT AUTO_INCREMENT PRIMARY KEY,
    recruiter_id INT NOT NULL,
    title VARCHAR(180) NOT NULL,
    department VARCHAR(120),
    description TEXT NOT NULL,
    min_experience_years DECIMAL(4, 1) NOT NULL DEFAULT 0.0,
    min_education_level VARCHAR(60) NOT NULL DEFAULT 'Bachelor',
    employment_type VARCHAR(50) NOT NULL DEFAULT 'Full-time', -- Full-time, Part-time, Contract, Remote, Hybrid, On-site
    location VARCHAR(120) NOT NULL DEFAULT 'Remote',
    salary_min DECIMAL(12, 2) DEFAULT 0.00,
    salary_max DECIMAL(12, 2) DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CLOSED, DRAFT
    deadline DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_jobs_recruiter FOREIGN KEY (recruiter_id) REFERENCES recruiters(recruiter_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_title ON jobs(title);

-- 14. JOB REQUIRED SKILLS TABLE
DROP TABLE IF EXISTS job_required_skills;
CREATE TABLE job_required_skills (
    job_required_skill_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    skill_id INT NOT NULL,
    min_proficiency VARCHAR(30) NOT NULL DEFAULT 'Intermediate',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_req_skill (job_id, skill_id),
    CONSTRAINT fk_jreq_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_jreq_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 15. JOB PREFERRED SKILLS TABLE
DROP TABLE IF EXISTS job_preferred_skills;
CREATE TABLE job_preferred_skills (
    job_preferred_skill_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    skill_id INT NOT NULL,
    min_proficiency VARCHAR(30) NOT NULL DEFAULT 'Beginner',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_pref_skill (job_id, skill_id),
    CONSTRAINT fk_jpref_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_jpref_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 16. JOB APPLICATIONS TABLE
DROP TABLE IF EXISTS job_applications;
CREATE TABLE job_applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT NOT NULL,
    candidate_id INT NOT NULL,
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(40) NOT NULL DEFAULT 'APPLIED', -- APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, REJECTED, HIRED
    recruiter_notes TEXT,
    matching_score_at_application DECIMAL(5, 2) DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_candidate (job_id, candidate_id),
    CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_app_status ON job_applications(status);

-- 17. MATCH RESULTS TABLE
DROP TABLE IF EXISTS match_results;
CREATE TABLE match_results (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id INT NOT NULL,
    job_id INT NOT NULL,
    overall_score DECIMAL(5, 2) NOT NULL,
    required_skill_score DECIMAL(5, 2) NOT NULL,
    preferred_skill_score DECIMAL(5, 2) NOT NULL,
    experience_score DECIMAL(5, 2) NOT NULL,
    education_score DECIMAL(5, 2) NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_candidate_job_match (candidate_id, job_id),
    CONSTRAINT fk_match_candidate FOREIGN KEY (candidate_id) REFERENCES candidate_profiles(candidate_id) ON DELETE CASCADE,
    CONSTRAINT fk_match_job FOREIGN KEY (job_id) REFERENCES jobs(job_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_match_score ON match_results(overall_score DESC);

-- 18. MATCH DETAILS TABLE
DROP TABLE IF EXISTS match_details;
CREATE TABLE match_details (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    match_id INT NOT NULL,
    matched_required_skills TEXT,
    missing_required_skills TEXT,
    matched_preferred_skills TEXT,
    missing_preferred_skills TEXT,
    experience_compatibility VARCHAR(255),
    education_compatibility VARCHAR(255),
    recommendations TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_detail_match FOREIGN KEY (match_id) REFERENCES match_results(match_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 19. ACTIVITY LOGS TABLE
DROP TABLE IF EXISTS activity_logs;
CREATE TABLE activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action_type VARCHAR(60) NOT NULL,
    description VARCHAR(500) NOT NULL,
    ip_address VARCHAR(50) DEFAULT '127.0.0.1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;
CREATE INDEX idx_log_created ON activity_logs(created_at DESC);

-- 20. SYSTEM SETTINGS TABLE
DROP TABLE IF EXISTS system_settings;
CREATE TABLE system_settings (
    setting_key VARCHAR(80) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    setting_description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

