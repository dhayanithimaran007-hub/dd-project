package com.talentiq.model;

import java.io.Serializable;
import java.util.Objects;

public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    private int projectId;
    private int candidateId;
    private String title;
    private String role;
    private String description;
    private String technologies;
    private String projectUrl;

    public Project() {}

    public Project(int candidateId, String title, String role, String description, String technologies, String projectUrl) {
        this.candidateId = candidateId;
        this.title = title;
        this.role = role;
        this.description = description;
        this.technologies = technologies;
        this.projectUrl = projectUrl;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String technologies) {
        this.technologies = technologies;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return projectId == project.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    @Override
    public String toString() {
        return title;
    }
}

