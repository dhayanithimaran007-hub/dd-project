package com.talentiq.model;

import java.io.Serializable;
import java.util.Objects;

public class Education implements Serializable {
    private static final long serialVersionUID = 1L;

    private int educationId;
    private int candidateId;
    private String degree;
    private String degreeLevel; // Doctorate, Master, Bachelor, Associate, High School
    private String institution;
    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    private String gradeGpa;

    public Education() {}

    public Education(int candidateId, String degree, String degreeLevel, String institution, String fieldOfStudy, Integer startYear, Integer endYear, String gradeGpa) {
        this.candidateId = candidateId;
        this.degree = degree;
        this.degreeLevel = degreeLevel;
        this.institution = institution;
        this.fieldOfStudy = fieldOfStudy;
        this.startYear = startYear;
        this.endYear = endYear;
        this.gradeGpa = gradeGpa;
    }

    public int getEducationId() {
        return educationId;
    }

    public void setEducationId(int educationId) {
        this.educationId = educationId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getDegreeLevel() {
        return degreeLevel != null ? degreeLevel : "Bachelor";
    }

    public void setDegreeLevel(String degreeLevel) {
        this.degreeLevel = degreeLevel;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public String getGradeGpa() {
        return gradeGpa;
    }

    public void setGradeGpa(String gradeGpa) {
        this.gradeGpa = gradeGpa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Education education = (Education) o;
        return educationId == education.educationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(educationId);
    }

    @Override
    public String toString() {
        return degree + " - " + institution;
    }
}

