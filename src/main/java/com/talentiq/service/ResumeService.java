package com.talentiq.service;

import com.talentiq.config.AppConfig;
import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.dao.CandidateDAO;
import com.talentiq.dao.ResumeDAO;
import com.talentiq.dao.SkillDAO;
import com.talentiq.model.CandidateProfile;
import com.talentiq.model.CandidateSkill;
import com.talentiq.model.Resume;
import com.talentiq.model.Skill;
import com.talentiq.util.ResumeParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResumeService {

    private static final Logger LOGGER = Logger.getLogger(ResumeService.class.getName());

    private final ResumeDAO resumeDAO = new ResumeDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final SkillDAO skillDAO = new SkillDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    /**
     * Uploads and parses a resume file, storing the original on disk and metadata in the database.
     */
    public ResumeParser.ParsedResumeResult uploadAndParseResume(int candidateId, File sourceFile) throws Exception {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist.");
        }

        long maxBytes = (long) AppConfig.getResumeMaxSizeMb() * 1024 * 1024;
        if (sourceFile.length() > maxBytes) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of " + AppConfig.getResumeMaxSizeMb() + " MB.");
        }

        // 1. Ensure target directory exists
        String targetDir = AppConfig.getResumeUploadDir();
        Path dirPath = Paths.get(targetDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 2. Generate unique filename and copy file to storage
        String originalName = sourceFile.getName();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) ext = originalName.substring(dot);

        String uniqueFileName = "resume_cand_" + candidateId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path targetPath = dirPath.resolve(uniqueFileName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 3. Parse text using rule-based parser
        List<Skill> skillDictionary = skillDAO.findAllSkills();
        ResumeParser.ParsedResumeResult parseResult;
        try {
            parseResult = ResumeParser.parseFile(targetPath.toFile(), skillDictionary);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse text from file, creating fallback empty result", e);
            parseResult = new ResumeParser.ParsedResumeResult();
            parseResult.setFileName(originalName);
            parseResult.setFileType(ext.replace(".", "").toUpperCase());
            parseResult.setFileSizeBytes(sourceFile.length());
        }

        // 4. Save metadata in MySQL
        Resume resume = new Resume();
        resume.setCandidateId(candidateId);
        resume.setFileName(originalName);
        resume.setStoredFilePath(targetPath.toString());
        resume.setFileType(parseResult.getFileType() != null ? parseResult.getFileType() : "PDF");
        resume.setFileSizeBytes(sourceFile.length());
        resume.setExtractedText(parseResult.getRawText());
        resume.setPrimary(true);
        resume.setParsedSuccessfully(parseResult.getRawText() != null && !parseResult.getRawText().isBlank());

        int resumeId = resumeDAO.save(resume);
        resume.setResumeId(resumeId);

        activityLogDAO.log(null, "RESUME_UPLOAD", "Uploaded resume '" + originalName + "' for candidate ID #" + candidateId, "127.0.0.1");

        return parseResult;
    }

    /**
     * Applies verified/corrected parsed details directly to the candidate's database profile.
     */
    public boolean applyParsedInfoToCandidate(int candidateId, ResumeParser.ParsedResumeResult parsed) {
        CandidateProfile profile = candidateDAO.findById(candidateId);
        if (profile == null) return false;

        if (parsed.getTotalExperienceYears() > 0) {
            profile.setTotalExperienceYears(parsed.getTotalExperienceYears());
        }

        if (parsed.getHighestEducationLevel() != null && !parsed.getHighestEducationLevel().isBlank()) {
            profile.setHighestEducationLevel(parsed.getHighestEducationLevel());
        }

        if (parsed.getHeadline() != null && !parsed.getHeadline().isBlank()) {
            profile.setHeadline(parsed.getHeadline());
        }

        if (parsed.getSummary() != null && !parsed.getSummary().isBlank()) {
            profile.setSummary(parsed.getSummary());
        }

        boolean updated = candidateDAO.update(profile);

        // Save matched skills to candidate_skills table
        if (parsed.getMatchedSkills() != null) {
            for (Skill s : parsed.getMatchedSkills()) {
                CandidateSkill cs = new CandidateSkill();
                cs.setCandidateId(candidateId);
                cs.setSkillId(s.getSkillId());
                cs.setSkillName(s.getSkillName());
                cs.setProficiencyLevel("Intermediate");
                cs.setYearsOfExperience(Math.max(1.0, Math.min(parsed.getTotalExperienceYears(), 3.0)));
                candidateDAO.addOrUpdateSkill(cs);
            }
        }

        activityLogDAO.log(profile.getUserId(), "RESUME_INFO_APPLIED", "Applied extracted resume information to candidate #" + candidateId, "127.0.0.1");
        return updated;
    }

    public List<Resume> getCandidateResumes(int candidateId) {
        return resumeDAO.findByCandidateId(candidateId);
    }

    public Resume getPrimaryResume(int candidateId) {
        return resumeDAO.findPrimaryByCandidateId(candidateId);
    }

    public boolean setPrimaryResume(int candidateId, int resumeId) {
        return resumeDAO.setPrimary(candidateId, resumeId);
    }

    public boolean deleteResume(int resumeId) {
        Resume r = resumeDAO.findById(resumeId);
        if (r != null) {
            try {
                Path path = Paths.get(r.getStoredFilePath());
                Files.deleteIfExists(path);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to delete file from disk: " + r.getStoredFilePath(), e);
            }
            return resumeDAO.delete(resumeId);
        }
        return false;
    }
}

