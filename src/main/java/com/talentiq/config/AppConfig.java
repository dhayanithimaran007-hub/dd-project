package com.talentiq.config;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application configuration holder with runtime overrides.
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());

    private static double requiredSkillWeight = 0.60;
    private static double preferredSkillWeight = 0.20;
    private static double experienceWeight = 0.10;
    private static double educationWeight = 0.10;
    private static String appName = "TalentIQ – Resume & Skill Matching Management System";
    private static String appVersion = "1.0.0";
    private static String resumeUploadDir = "uploads/resumes";
    private static int resumeMaxSizeMb = 10;

    static {
        loadProperties();
    }

    private AppConfig() {
    }

    private static void loadProperties() {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                appName = props.getProperty("app.name", appName);
                appVersion = props.getProperty("app.version", appVersion);
                requiredSkillWeight = Double.parseDouble(props.getProperty("matching.weight.required", "0.60"));
                preferredSkillWeight = Double.parseDouble(props.getProperty("matching.weight.preferred", "0.20"));
                experienceWeight = Double.parseDouble(props.getProperty("matching.weight.experience", "0.10"));
                educationWeight = Double.parseDouble(props.getProperty("matching.weight.education", "0.10"));
                resumeUploadDir = props.getProperty("resume.upload.dir", resumeUploadDir);
                resumeMaxSizeMb = Integer.parseInt(props.getProperty("resume.max.size.mb", "10"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load application.properties, using built-in defaults", e);
        }
    }

    public static double getRequiredSkillWeight() {
        return requiredSkillWeight;
    }

    public static void setRequiredSkillWeight(double weight) {
        requiredSkillWeight = weight;
    }

    public static double getPreferredSkillWeight() {
        return preferredSkillWeight;
    }

    public static void setPreferredSkillWeight(double weight) {
        preferredSkillWeight = weight;
    }

    public static double getExperienceWeight() {
        return experienceWeight;
    }

    public static void setExperienceWeight(double weight) {
        experienceWeight = weight;
    }

    public static double getEducationWeight() {
        return educationWeight;
    }

    public static void setEducationWeight(double weight) {
        educationWeight = weight;
    }

    public static String getAppName() {
        return appName;
    }

    public static String getAppVersion() {
        return appVersion;
    }

    public static String getResumeUploadDir() {
        return resumeUploadDir;
    }

    public static int getResumeMaxSizeMb() {
        return resumeMaxSizeMb;
    }
}

