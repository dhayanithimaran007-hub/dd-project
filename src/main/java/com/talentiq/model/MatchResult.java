package com.talentiq.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class MatchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private int matchId;
    private int candidateId;
    private int jobId;
    private String candidateName;
    private String jobTitle;
    private double overallScore;
    private double requiredSkillScore;
    private double preferredSkillScore;
    private double experienceScore;
    private double educationScore;
    private Timestamp calculatedAt;

    private MatchDetail details;

    public MatchResult() {}

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public double getRequiredSkillScore() {
        return requiredSkillScore;
    }

    public void setRequiredSkillScore(double requiredSkillScore) {
        this.requiredSkillScore = requiredSkillScore;
    }

    public double getPreferredSkillScore() {
        return preferredSkillScore;
    }

    public void setPreferredSkillScore(double preferredSkillScore) {
        this.preferredSkillScore = preferredSkillScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public double getEducationScore() {
        return educationScore;
    }

    public void setEducationScore(double educationScore) {
        this.educationScore = educationScore;
    }

    public Timestamp getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Timestamp calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public MatchDetail getDetails() {
        return details;
    }

    public void setDetails(MatchDetail details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchResult that = (MatchResult) o;
        return candidateId == that.candidateId && jobId == that.jobId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidateId, jobId);
    }
}

