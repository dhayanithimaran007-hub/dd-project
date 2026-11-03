package com.talentiq.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CandidateProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private int candidateId;
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String headline;
    private String summary;
    private double totalExperienceYears;
    private String currentJobTitle;
    private String currentCompany;
    private String location;
    private String highestEducationLevel; // Doctorate, Master, Bachelor, Associate, High School
    private BigDecimal expectedSalary;
    private String linkedinUrl;
    private String githubUrl;
    private String status = "ACTIVE";
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Associated collections
    private List<CandidateSkill> skills = new ArrayList<>();
    private List<Education> educationList = new ArrayList<>();
    private List<WorkExperience> experienceList = new ArrayList<>();
    private List<Certification> certifications = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();
    private Resume primaryResume;

    public CandidateProfile() {}

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public double getTotalExperienceYears() {
        return totalExperienceYears;
    }

    public void setTotalExperienceYears(double totalExperienceYears) {
        this.totalExperienceYears = totalExperienceYears;
    }

    public String getCurrentJobTitle() {
        return currentJobTitle;
    }

    public void setCurrentJobTitle(String currentJobTitle) {
        this.currentJobTitle = currentJobTitle;
    }

    public String getCurrentCompany() {
        return currentCompany;
    }

    public void setCurrentCompany(String currentCompany) {
        this.currentCompany = currentCompany;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHighestEducationLevel() {
        return highestEducationLevel != null ? highestEducationLevel : "Bachelor";
    }

    public void setHighestEducationLevel(String highestEducationLevel) {
        this.highestEducationLevel = highestEducationLevel;
    }

    public BigDecimal getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(BigDecimal expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<CandidateSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<CandidateSkill> skills) {
        this.skills = skills;
    }

    public List<Education> getEducationList() {
        return educationList;
    }

    public void setEducationList(List<Education> educationList) {
        this.educationList = educationList;
    }

    public List<WorkExperience> getExperienceList() {
        return experienceList;
    }

    public void setExperienceList(List<WorkExperience> experienceList) {
        this.experienceList = experienceList;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Certification> certifications) {
        this.certifications = certifications;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public Resume getPrimaryResume() {
        return primaryResume;
    }

    public void setPrimaryResume(Resume primaryResume) {
        this.primaryResume = primaryResume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CandidateProfile that = (CandidateProfile) o;
        return candidateId == that.candidateId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidateId);
    }

    @Override
    public String toString() {
        return fullName != null ? fullName : ("Candidate #" + candidateId);
    }
}

