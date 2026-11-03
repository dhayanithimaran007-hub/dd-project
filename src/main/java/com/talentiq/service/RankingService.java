package com.talentiq.service;

import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.dao.JobApplicationDAO;
import com.talentiq.dao.MatchResultDAO;
import com.talentiq.model.JobApplication;
import com.talentiq.model.MatchResult;

import java.sql.SQLException;
import java.util.List;

public class RankingService {

    private final MatchResultDAO matchResultDAO = new MatchResultDAO();
    private final JobApplicationDAO jobApplicationDAO = new JobApplicationDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public List<MatchResult> getRankedCandidates(int jobId, String skillFilter, Double minExp, String eduFilter, Double minScore) {
        return matchResultDAO.getRankedCandidatesForJob(jobId, skillFilter, minExp, eduFilter, minScore);
    }

    public boolean updateApplicationStatus(int jobId, int candidateId, String newStatus, String notes) {
        JobApplication existing = jobApplicationDAO.findExisting(jobId, candidateId);
        if (existing != null) {
            boolean updated = jobApplicationDAO.updateStatus(existing.getApplicationId(), newStatus, notes);
            if (updated) {
                activityLogDAO.log(null, "STATUS_UPDATED", "Updated status of candidate #" + candidateId + " for job #" + jobId + " to " + newStatus, "127.0.0.1");
            }
            return updated;
        } else {
            // Create application with that status
            JobApplication app = new JobApplication();
            app.setJobId(jobId);
            app.setCandidateId(candidateId);
            app.setStatus(newStatus);
            app.setRecruiterNotes(notes);

            MatchResult mr = matchResultDAO.findMatch(candidateId, jobId);
            if (mr != null) {
                app.setMatchingScoreAtApplication(mr.getOverallScore());
            }

            try {
                int id = jobApplicationDAO.create(app);
                if (id > 0) {
                    activityLogDAO.log(null, "STATUS_UPDATED", "Set status of candidate #" + candidateId + " for job #" + jobId + " to " + newStatus, "127.0.0.1");
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    public List<JobApplication> getApplicationsForJob(int jobId) {
        return jobApplicationDAO.findByJob(jobId);
    }

    public List<JobApplication> getApplicationsForCandidate(int candidateId) {
        return jobApplicationDAO.findByCandidate(candidateId);
    }

    public boolean applyForJob(int jobId, int candidateId, double matchScore) throws Exception {
        JobApplication existing = jobApplicationDAO.findExisting(jobId, candidateId);
        if (existing != null) {
            throw new IllegalStateException("Candidate has already applied for this position.");
        }

        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setCandidateId(candidateId);
        app.setStatus(JobApplication.STATUS_APPLIED);
        app.setMatchingScoreAtApplication(matchScore);

        int id = jobApplicationDAO.create(app);
        if (id > 0) {
            activityLogDAO.log(null, "JOB_APPLIED", "Candidate #" + candidateId + " applied for job #" + jobId + " (Score: " + matchScore + "%)", "127.0.0.1");
            return true;
        }
        return false;
    }
}

