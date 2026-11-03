package com.talentiq.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class JobApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_APPLIED = "APPLIED";
    public static final String STATUS_SHORTLISTED = "SHORTLISTED";
    public static final String STATUS_INTERVIEW = "INTERVIEW_SCHEDULED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_HIRED = "HIRED";

    private int applicationId;
    private int jobId;
    private int candidateId;
    private String jobTitle;
    private String companyName;
    private String candidateName;
    private String candidateEmail;
    private double candidateTotalExperience;
    private Timestamp appliedDate;
    private String status = STATUS_APPLIED;
    private String recruiterNotes;
    private double matchingScoreAtApplication;
    private Timestamp updatedAt;

    public JobApplication() {}

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public double getCandidateTotalExperience() {
        return candidateTotalExperience;
    }

    public void setCandidateTotalExperience(double candidateTotalExperience) {
        this.candidateTotalExperience = candidateTotalExperience;
    }

    public Timestamp getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(Timestamp appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecruiterNotes() {
        return recruiterNotes;
    }

    public void setRecruiterNotes(String recruiterNotes) {
        this.recruiterNotes = recruiterNotes;
    }

    public double getMatchingScoreAtApplication() {
        return matchingScoreAtApplication;
    }

    public void setMatchingScoreAtApplication(double matchingScoreAtApplication) {
        this.matchingScoreAtApplication = matchingScoreAtApplication;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobApplication that = (JobApplication) o;
        return applicationId == that.applicationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId);
    }
}

