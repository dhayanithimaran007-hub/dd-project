package com.talentiq.model;

import java.io.Serializable;
import java.util.Objects;

public class CandidateSkill implements Serializable {
    private static final long serialVersionUID = 1L;

    private int candidateSkillId;
    private int candidateId;
    private int skillId;
    private String skillName;
    private String categoryName;
    private String proficiencyLevel; // Beginner, Intermediate, Advanced, Expert
    private double yearsOfExperience = 1.0;

    public CandidateSkill() {}

    public CandidateSkill(int candidateId, int skillId, String skillName, String proficiencyLevel, double yearsOfExperience) {
        this.candidateId = candidateId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel;
        this.yearsOfExperience = yearsOfExperience;
    }

    public int getCandidateSkillId() {
        return candidateSkillId;
    }

    public void setCandidateSkillId(int candidateSkillId) {
        this.candidateSkillId = candidateSkillId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel != null ? proficiencyLevel : "Intermediate";
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public double getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(double yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CandidateSkill that = (CandidateSkill) o;
        return skillId == that.skillId && candidateId == that.candidateId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidateId, skillId);
    }

    @Override
    public String toString() {
        return skillName + " (" + proficiencyLevel + ", " + yearsOfExperience + " yrs)";
    }
}

