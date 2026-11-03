package com.talentiq.service;

import com.talentiq.config.AppConfig;
import com.talentiq.dao.CandidateDAO;
import com.talentiq.dao.JobDAO;
import com.talentiq.dao.MatchResultDAO;
import com.talentiq.model.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Deterministic rule-based resume and skill matching engine.
 * Strictly avoids AI, Machine Learning, and external NLP services.
 * Implements configurable weighted scoring according to exact specifications:
 *
 * Final Score = (Required Skill Score * 0.60) + (Preferred Skill Score * 0.20) +
 *               (Experience Score * 0.10) + (Education Score * 0.10)
 */
public class MatchingService {

    private static final Logger LOGGER = Logger.getLogger(MatchingService.class.getName());

    private final MatchResultDAO matchResultDAO = new MatchResultDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final JobDAO jobDAO = new JobDAO();

    /**
     * Calculates the deterministic match result between a candidate and a job.
     */
    public MatchResult calculateMatch(CandidateProfile candidate, Job job) {
        if (candidate == null || job == null) {
            throw new IllegalArgumentException("Candidate and Job cannot be null");
        }

        double reqWeight = AppConfig.getRequiredSkillWeight();
        double prefWeight = AppConfig.getPreferredSkillWeight();
        double expWeight = AppConfig.getExperienceWeight();
        double eduWeight = AppConfig.getEducationWeight();

        // 1. Process Required Skills
        List<JobSkill> jobRequired = job.getRequiredSkills() != null ? job.getRequiredSkills() : new ArrayList<>();
        List<CandidateSkill> candSkills = candidate.getSkills() != null ? candidate.getSkills() : new ArrayList<>();

        Set<String> candidateSkillNames = candSkills.stream()
                .map(cs -> cs.getSkillName().trim().toLowerCase())
                .collect(Collectors.toSet());

        Set<Integer> candidateSkillIds = candSkills.stream()
                .map(CandidateSkill::getSkillId)
                .collect(Collectors.toSet());

        List<String> matchedReqList = new ArrayList<>();
        List<String> missingReqList = new ArrayList<>();

        for (JobSkill js : jobRequired) {
            String nameLower = js.getSkillName().trim().toLowerCase();
            if (candidateSkillIds.contains(js.getSkillId()) || candidateSkillNames.contains(nameLower)) {
                matchedReqList.add(js.getSkillName());
            } else {
                missingReqList.add(js.getSkillName());
            }
        }

        double reqScore;
        if (jobRequired.isEmpty()) {
            reqScore = 100.0;
        } else {
            reqScore = (matchedReqList.size() / (double) jobRequired.size()) * 100.0;
        }

        // 2. Process Preferred Skills
        List<JobSkill> jobPreferred = job.getPreferredSkills() != null ? job.getPreferredSkills() : new ArrayList<>();
        List<String> matchedPrefList = new ArrayList<>();
        List<String> missingPrefList = new ArrayList<>();

        for (JobSkill js : jobPreferred) {
            String nameLower = js.getSkillName().trim().toLowerCase();
            if (candidateSkillIds.contains(js.getSkillId()) || candidateSkillNames.contains(nameLower)) {
                matchedPrefList.add(js.getSkillName());
            } else {
                missingPrefList.add(js.getSkillName());
            }
        }

        double prefScore;
        if (jobPreferred.isEmpty()) {
            prefScore = 100.0;
        } else {
            prefScore = (matchedPrefList.size() / (double) jobPreferred.size()) * 100.0;
        }

        // 3. Process Experience Compatibility
        double candExp = Math.max(0.0, candidate.getTotalExperienceYears());
        double jobMinExp = Math.max(0.0, job.getMinExperienceYears());
        double expScore;
        String expCompatText;

        if (jobMinExp <= 0.0) {
            expScore = 100.0;
            expCompatText = String.format(Locale.US, "Satisfied: No minimum experience required (Candidate has %.1f yrs)", candExp);
        } else if (candExp >= jobMinExp) {
            expScore = 100.0;
            expCompatText = String.format(Locale.US, "Qualified: %.1f yrs meets or exceeds requirement of %.1f yrs (100.0%%)", candExp, jobMinExp);
        } else {
            expScore = Math.min(100.0, Math.max(0.0, (candExp / jobMinExp) * 100.0));
            expCompatText = String.format(Locale.US, "Partial: %.1f yrs vs minimum requirement of %.1f yrs (%.1f%%)", candExp, jobMinExp, expScore);
        }

        // 4. Process Education Compatibility
        int candEduRank = getEducationRank(candidate.getHighestEducationLevel());
        int jobEduRank = getEducationRank(job.getMinEducationLevel());
        double eduScore;
        String eduCompatText;

        if (candEduRank >= jobEduRank) {
            eduScore = 100.0;
            eduCompatText = String.format("Qualified: %s meets or exceeds required %s (100.0%%)",
                    candidate.getHighestEducationLevel(), job.getMinEducationLevel());
        } else {
            int diff = jobEduRank - candEduRank;
            if (diff == 1) {
                eduScore = 75.0;
            } else if (diff == 2) {
                eduScore = 50.0;
            } else {
                eduScore = 25.0;
            }
            eduCompatText = String.format("Underqualified: %s is below required %s (%.1f%%)",
                    candidate.getHighestEducationLevel(), job.getMinEducationLevel(), eduScore);
        }

        // 5. Final Weighted Score
        double overallScore = (reqScore * reqWeight) +
                              (prefScore * prefWeight) +
                              (expScore * expWeight) +
                              (eduScore * eduWeight);

        overallScore = Math.round(overallScore * 100.0) / 100.0;
        reqScore = Math.round(reqScore * 100.0) / 100.0;
        prefScore = Math.round(prefScore * 100.0) / 100.0;
        expScore = Math.round(expScore * 100.0) / 100.0;
        eduScore = Math.round(eduScore * 100.0) / 100.0;

        // 6. Build Result & Detail Objects
        MatchResult mr = new MatchResult();
        mr.setCandidateId(candidate.getCandidateId());
        mr.setJobId(job.getJobId());
        mr.setCandidateName(candidate.getFullName());
        mr.setJobTitle(job.getTitle());
        mr.setOverallScore(overallScore);
        mr.setRequiredSkillScore(reqScore);
        mr.setPreferredSkillScore(prefScore);
        mr.setExperienceScore(expScore);
        mr.setEducationScore(eduScore);

        MatchDetail md = new MatchDetail();
        md.setMatchedRequiredSkills(matchedReqList);
        md.setMissingRequiredSkills(missingReqList);
        md.setMatchedPreferredSkills(matchedPrefList);
        md.setMissingPreferredSkills(missingPrefList);
        md.setExperienceCompatibility(expCompatText);
        md.setEducationCompatibility(eduCompatText);

        StringBuilder rec = new StringBuilder();
        if (overallScore >= 90.0) {
            rec.append("Outstanding match. Candidate satisfies primary technical, experience, and educational criteria. Recommend immediate interview.");
        } else if (overallScore >= 75.0) {
            rec.append("Strong candidate profile. Highly competitive fit. Review specific missing competencies: ")
               .append(missingReqList.isEmpty() ? "No core gaps." : String.join(", ", missingReqList));
        } else if (overallScore >= 50.0) {
            rec.append("Moderate match. Candidate has foundational skills but lacks key requirements: ")
               .append(missingReqList.isEmpty() ? "Experience/Education gap" : String.join(", ", missingReqList));
        } else {
            rec.append("Low overall alignment. Significant skill and experience gaps for this role.");
        }
        md.setRecommendations(rec.toString());

        mr.setDetails(md);
        return mr;
    }

    /**
     * Calculates match and saves to database.
     */
    public MatchResult calculateAndSaveMatch(int candidateId, int jobId) {
        CandidateProfile candidate = candidateDAO.findById(candidateId);
        Job job = jobDAO.findById(jobId);
        if (candidate == null || job == null) return null;

        MatchResult mr = calculateMatch(candidate, job);
        matchResultDAO.saveOrUpdate(mr);
        return mr;
    }

    /**
     * Evaluates all candidates against a specific job and persists the rankings.
     */
    public List<MatchResult> evaluateAllCandidatesForJob(int jobId) {
        Job job = jobDAO.findById(jobId);
        if (job == null) return Collections.emptyList();

        List<CandidateProfile> candidates = candidateDAO.findAll(null, null, null);
        List<MatchResult> results = new ArrayList<>();

        for (CandidateProfile c : candidates) {
            MatchResult mr = calculateMatch(c, job);
            matchResultDAO.saveOrUpdate(mr);
            results.add(mr);
        }

        results.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        return results;
    }

    /**
     * Evaluates all active jobs for a specific candidate.
     */
    public List<MatchResult> evaluateCandidateForAllJobs(int candidateId) {
        CandidateProfile candidate = candidateDAO.findById(candidateId);
        if (candidate == null) return Collections.emptyList();

        List<Job> jobs = jobDAO.findAll(null, Job.STATUS_ACTIVE, null, null);
        List<MatchResult> results = new ArrayList<>();

        for (Job j : jobs) {
            MatchResult mr = calculateMatch(candidate, j);
            matchResultDAO.saveOrUpdate(mr);
            results.add(mr);
        }

        results.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        return results;
    }

    public MatchResult getMatch(int candidateId, int jobId) {
        MatchResult mr = matchResultDAO.findMatch(candidateId, jobId);
        if (mr == null) {
            mr = calculateAndSaveMatch(candidateId, jobId);
        }
        return mr;
    }

    public static int getEducationRank(String level) {
        if (level == null) return 3; // Default Bachelor
        String l = level.trim().toLowerCase();
        if (l.contains("doctor") || l.contains("phd") || l.contains("ph.d")) return 5;
        if (l.contains("master") || l.contains("ms") || l.contains("mba") || l.contains("mca") || l.contains("mtech")) return 4;
        if (l.contains("bachelor") || l.contains("bs") || l.contains("btech") || l.contains("be") || l.contains("bca")) return 3;
        if (l.contains("associate") || l.contains("diploma")) return 2;
        if (l.contains("high school") || l.contains("secondary")) return 1;
        return 3;
    }
}

