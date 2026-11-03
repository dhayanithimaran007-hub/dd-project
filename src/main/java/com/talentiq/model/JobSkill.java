package com.talentiq.model;

import java.io.Serializable;
import java.util.Objects;

public class JobSkill implements Serializable {
    private static final long serialVersionUID = 1L;

    private int jobSkillId;
    private int jobId;
    private int skillId;
    private String skillName;
    private String categoryName;
    private String minProficiency = "Intermediate"; // Beginner, Intermediate, Advanced, Expert
    private boolean required = true; // true = required skill, false = preferred skill

    public JobSkill() {}

    public JobSkill(int jobId, int skillId, String skillName, String minProficiency, boolean required) {
        this.jobId = jobId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.minProficiency = minProficiency;
        this.required = required;
    }

    public int getJobSkillId() {
        return jobSkillId;
    }

    public void setJobSkillId(int jobSkillId) {
        this.jobSkillId = jobSkillId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
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

    public String getMinProficiency() {
        return minProficiency != null ? minProficiency : "Intermediate";
    }

    public void setMinProficiency(String minProficiency) {
        this.minProficiency = minProficiency;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSkill jobSkill = (JobSkill) o;
        return jobId == jobSkill.jobId && skillId == jobSkill.skillId && required == jobSkill.required;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, skillId, required);
    }

    @Override
    public String toString() {
        return skillName + " (" + minProficiency + ")";
    }
}

