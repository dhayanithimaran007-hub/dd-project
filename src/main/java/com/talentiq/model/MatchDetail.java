package com.talentiq.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MatchDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private int detailId;
    private int matchId;
    private List<String> matchedRequiredSkills = new ArrayList<>();
    private List<String> missingRequiredSkills = new ArrayList<>();
    private List<String> matchedPreferredSkills = new ArrayList<>();
    private List<String> missingPreferredSkills = new ArrayList<>();
    private String experienceCompatibility;
    private String educationCompatibility;
    private String recommendations;

    public MatchDetail() {}

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public List<String> getMatchedRequiredSkills() {
        return matchedRequiredSkills;
    }

    public void setMatchedRequiredSkills(List<String> matchedRequiredSkills) {
        this.matchedRequiredSkills = matchedRequiredSkills != null ? matchedRequiredSkills : new ArrayList<>();
    }

    public List<String> getMissingRequiredSkills() {
        return missingRequiredSkills;
    }

    public void setMissingRequiredSkills(List<String> missingRequiredSkills) {
        this.missingRequiredSkills = missingRequiredSkills != null ? missingRequiredSkills : new ArrayList<>();
    }

    public List<String> getMatchedPreferredSkills() {
        return matchedPreferredSkills;
    }

    public void setMatchedPreferredSkills(List<String> matchedPreferredSkills) {
        this.matchedPreferredSkills = matchedPreferredSkills != null ? matchedPreferredSkills : new ArrayList<>();
    }

    public List<String> getMissingPreferredSkills() {
        return missingPreferredSkills;
    }

    public void setMissingPreferredSkills(List<String> missingPreferredSkills) {
        this.missingPreferredSkills = missingPreferredSkills != null ? missingPreferredSkills : new ArrayList<>();
    }

    public String getExperienceCompatibility() {
        return experienceCompatibility;
    }

    public void setExperienceCompatibility(String experienceCompatibility) {
        this.experienceCompatibility = experienceCompatibility;
    }

    public String getEducationCompatibility() {
        return educationCompatibility;
    }

    public void setEducationCompatibility(String educationCompatibility) {
        this.educationCompatibility = educationCompatibility;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    // Helper conversion methods for comma-delimited SQL strings
    public static List<String> parseList(String raw) {
        if (raw == null || raw.trim().isEmpty() || "None".equalsIgnoreCase(raw.trim())) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static String toCommaString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "None";
        }
        return String.join(", ", list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchDetail that = (MatchDetail) o;
        return detailId == that.detailId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(detailId);
    }
}

