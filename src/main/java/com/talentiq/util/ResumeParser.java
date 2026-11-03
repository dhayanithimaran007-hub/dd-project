package com.talentiq.util;

import com.talentiq.model.Skill;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traditional rule-based resume text parser using regular expressions and keyword matching.
 * Strictly avoids any AI, ML, or external NLP services.
 */
public class ResumeParser {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:\\+?\\d{1,3}[-.\\s]?)?(?:\\(?\\d{2,4}\\)?[-.\\s]?)?\\d{3,4}[-.\\s]?\\d{4}"
    );

    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(?i)(\\d+(?:\\.\\d+)?)\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s+(?:experience|exp|working)"
    );

    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile(
            "\\b(20\\d{2}|19\\d{2})\\s*[-–—to]+\\s*(20\\d{2}|19\\d{2}|present|current|now)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Degree Patterns
    private static final Pattern PHD_PATTERN = Pattern.compile("(?i)\\b(ph\\.?d|doctorate|doctor of philosophy)\\b");
    private static final Pattern MASTER_PATTERN = Pattern.compile("(?i)\\b(master'?s?|m\\.?s\\.?|m\\.?tech|m\\.?sc|mba|mca|m\\.?eng)\\b");
    private static final Pattern BACHELOR_PATTERN = Pattern.compile("(?i)\\b(bachelor'?s?|b\\.?s\\.?|b\\.?tech|b\\.?e\\.?|b\\.?sc|bca|bba|b\\.?eng)\\b");
    private static final Pattern ASSOCIATE_PATTERN = Pattern.compile("(?i)\\b(associate'?s?|diploma)\\b");

    private ResumeParser() {}

    /**
     * DTO containing all rule-extracted fields from a resume document.
     */
    public static class ParsedResumeResult {
        private String fileName;
        private String fileType;
        private long fileSizeBytes;
        private String rawText = "";
        private String candidateName = "";
        private String email = "";
        private String phone = "";
        private String location = "";
        private String highestEducationLevel = "Bachelor";
        private double totalExperienceYears = 0.0;
        private List<Skill> matchedSkills = new ArrayList<>();
        private String headline = "";
        private String summary = "";

        public ParsedResumeResult() {}

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public long getFileSizeBytes() { return fileSizeBytes; }
        public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

        public String getRawText() { return rawText; }
        public void setRawText(String rawText) { this.rawText = rawText; }

        public String getCandidateName() { return candidateName; }
        public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getHighestEducationLevel() { return highestEducationLevel; }
        public void setHighestEducationLevel(String highestEducationLevel) { this.highestEducationLevel = highestEducationLevel; }

        public double getTotalExperienceYears() { return totalExperienceYears; }
        public void setTotalExperienceYears(double totalExperienceYears) { this.totalExperienceYears = totalExperienceYears; }

        public List<Skill> getMatchedSkills() { return matchedSkills; }
        public void setMatchedSkills(List<Skill> matchedSkills) { this.matchedSkills = matchedSkills; }

        public String getHeadline() { return headline; }
        public void setHeadline(String headline) { this.headline = headline; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    /**
     * Parses a local resume file (PDF, DOCX, or TXT) against a list of known skills.
     */
    public static ParsedResumeResult parseFile(File file, List<Skill> skillDictionary) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        String fileName = file.getName();
        String extension = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx > 0) {
            extension = fileName.substring(dotIdx + 1).toUpperCase();
        }

        String text;
        if ("PDF".equalsIgnoreCase(extension)) {
            text = PDFTextExtractor.extractText(file);
        } else if ("DOCX".equalsIgnoreCase(extension)) {
            text = DocxTextExtractor.extractText(file);
        } else if ("TXT".equalsIgnoreCase(extension)) {
            text = Files.readString(file.toPath());
        } else {
            throw new IllegalArgumentException("Unsupported resume file format: " + extension + ". Supported: PDF, DOCX, TXT");
        }

        ParsedResumeResult result = parseText(text, skillDictionary);
        result.setFileName(fileName);
        result.setFileType(extension);
        result.setFileSizeBytes(file.length());
        return result;
    }

    /**
     * Deterministically parses plain text into structured candidate fields using regular expressions.
     */
    public static ParsedResumeResult parseText(String rawText, List<Skill> skillDictionary) {
        ParsedResumeResult result = new ParsedResumeResult();
        if (rawText == null || rawText.isBlank()) {
            return result;
        }

        result.setRawText(rawText);

        // 1. Extract Email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(rawText);
        if (emailMatcher.find()) {
            result.setEmail(emailMatcher.group().trim());
        }

        // 2. Extract Phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(rawText);
        if (phoneMatcher.find()) {
            result.setPhone(phoneMatcher.group().trim());
        }

        // 3. Extract Name candidate from first lines
        String[] lines = rawText.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // Filter out common header words
            String lower = trimmed.toLowerCase();
            if (lower.contains("resume") || lower.contains("curriculum vitae") || lower.contains("cv")
                    || lower.contains("page") || lower.contains("email") || lower.contains("@")
                    || lower.contains("phone") || lower.contains("http") || lower.length() > 50) {
                continue;
            }
            // Candidate name is likely the first prominent line
            // Strip suffixes like "Ph.D." or "PMP" or titles after dash
            int dashIdx = trimmed.indexOf(" - ");
            if (dashIdx > 0) {
                result.setCandidateName(trimmed.substring(0, dashIdx).trim());
                result.setHeadline(trimmed.substring(dashIdx + 3).trim());
            } else {
                result.setCandidateName(trimmed);
            }
            break;
        }

        // 4. Extract Highest Education Level
        if (PHD_PATTERN.matcher(rawText).find()) {
            result.setHighestEducationLevel("Doctorate");
        } else if (MASTER_PATTERN.matcher(rawText).find()) {
            result.setHighestEducationLevel("Master");
        } else if (BACHELOR_PATTERN.matcher(rawText).find()) {
            result.setHighestEducationLevel("Bachelor");
        } else if (ASSOCIATE_PATTERN.matcher(rawText).find()) {
            result.setHighestEducationLevel("Associate");
        } else {
            result.setHighestEducationLevel("Bachelor");
        }

        // 5. Extract Experience Years
        double extractedExp = 0.0;
        Matcher expMatcher = EXPERIENCE_PATTERN.matcher(rawText);
        if (expMatcher.find()) {
            try {
                extractedExp = Double.parseDouble(expMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        // If not found directly, estimate by date ranges
        if (extractedExp <= 0.0) {
            extractedExp = estimateExperienceFromDateRanges(rawText);
        }
        result.setTotalExperienceYears(extractedExp);

        // 6. Extract Skills from centralized skill dictionary
        if (skillDictionary != null && !skillDictionary.isEmpty()) {
            List<Skill> matched = new ArrayList<>();
            for (Skill skill : skillDictionary) {
                if (matchesSkillInText(skill, rawText)) {
                    matched.add(skill);
                }
            }
            result.setMatchedSkills(matched);
        }

        // 7. Extract Summary snippet
        int summaryIdx = rawText.toLowerCase().indexOf("summary");
        if (summaryIdx >= 0 && summaryIdx + 7 < rawText.length()) {
            String sub = rawText.substring(summaryIdx + 7).trim();
            if (sub.startsWith(":") || sub.startsWith("-")) {
                sub = sub.substring(1).trim();
            }
            int endIdx = sub.indexOf("\n\n");
            if (endIdx > 0) {
                result.setSummary(sub.substring(0, Math.min(endIdx, 500)).trim());
            } else {
                result.setSummary(sub.substring(0, Math.min(sub.length(), 350)).trim());
            }
        }

        return result;
    }

    /**
     * Checks if a skill or any of its aliases match within the text using word boundaries.
     */
    public static boolean matchesSkillInText(Skill skill, String text) {
        if (skill == null || text == null) return false;
        if (containsWord(text, skill.getSkillName())) {
            return true;
        }

        if (skill.getAliases() != null && !skill.getAliases().isBlank()) {
            String[] aliases = skill.getAliases().split("[,;|]");
            for (String alias : aliases) {
                if (containsWord(text, alias.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsWord(String source, String word) {
        if (word == null || word.trim().isEmpty() || source == null) return false;
        String trimmed = word.trim();

        // If word contains special punctuation like C++ or C# or .NET or Node.js
        String escaped = Pattern.quote(trimmed);
        String regex = "(?i)(^|[^a-zA-Z0-9_#+.-])" + escaped + "([^a-zA-Z0-9_#+.-]|$)";
        return Pattern.compile(regex).matcher(source).find();
    }

    private static double estimateExperienceFromDateRanges(String text) {
        Matcher m = DATE_RANGE_PATTERN.matcher(text);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        double maxSpan = 0.0;
        int earliestYear = currentYear;
        boolean foundAny = false;

        while (m.find()) {
            try {
                int start = Integer.parseInt(m.group(1));
                int end;
                String g2 = m.group(2).toLowerCase();
                if (g2.contains("present") || g2.contains("current") || g2.contains("now")) {
                    end = currentYear;
                } else {
                    end = Integer.parseInt(g2);
                }

                if (start <= end && start >= 1970 && end <= currentYear + 1) {
                    foundAny = true;
                    if (start < earliestYear) {
                        earliestYear = start;
                    }
                    double span = end - start;
                    if (span > maxSpan) maxSpan = span;
                }
            } catch (Exception ignored) {}
        }

        if (foundAny && earliestYear < currentYear) {
            return Math.min(currentYear - earliestYear, 35.0);
        }
        return maxSpan;
    }
}
