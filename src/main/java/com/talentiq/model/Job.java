package com.talentiq.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Job implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_DRAFT = "DRAFT";

    private int jobId;
    private int recruiterId;
    private String recruiterName;
    private String companyName;
    private String title;
    private String department;
    private String description;
    private double minExperienceYears = 0.0;
    private String minEducationLevel = "Bachelor";
    private String employmentType = "Full-time";
    private String location = "Remote";
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String status = STATUS_ACTIVE;
    private LocalDate deadline;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Associated skills
    private List<JobSkill> requiredSkills = new ArrayList<>();
    private List<JobSkill> preferredSkills = new ArrayList<>();
    private int applicationCount = 0;

    public Job() {}

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(int recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMinExperienceYears() {
        return minExperienceYears;
    }

    public void setMinExperienceYears(double minExperienceYears) {
        this.minExperienceYears = minExperienceYears;
    }

    public String getMinEducationLevel() {
        return minEducationLevel != null ? minEducationLevel : "Bachelor";
    }

    public void setMinEducationLevel(String minEducationLevel) {
        this.minEducationLevel = minEducationLevel;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(BigDecimal salaryMin) {
        this.salaryMin = salaryMin;
    }

    public BigDecimal getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(BigDecimal salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<JobSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<JobSkill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<JobSkill> getPreferredSkills() {
        return preferredSkills;
    }

    public void setPreferredSkills(List<JobSkill> preferredSkills) {
        this.preferredSkills = preferredSkills;
    }

    public int getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(int applicationCount) {
        this.applicationCount = applicationCount;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return jobId == job.jobId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return title + (companyName != null ? " (" + companyName + ")" : "");
    }
}

