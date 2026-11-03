package com.talentiq.service;

import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.dao.JobDAO;
import com.talentiq.model.Job;
import com.talentiq.model.JobSkill;

import java.sql.SQLException;
import java.util.List;

public class JobService {

    private final JobDAO jobDAO = new JobDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public Job getJobById(int jobId) {
        return jobDAO.findById(jobId);
    }

    public List<Job> getJobs(String keyword, String status, String employmentType, Integer recruiterId) {
        return jobDAO.findAll(keyword, status, employmentType, recruiterId);
    }

    public int createJob(Job job) throws SQLException {
        int id = jobDAO.create(job);
        activityLogDAO.log(null, "JOB_POSTED", "Posted new job opening: " + job.getTitle() + " (ID #" + id + ")", "127.0.0.1");
        return id;
    }

    public boolean updateJob(Job job) {
        boolean success = jobDAO.update(job);
        if (success) {
            activityLogDAO.log(null, "JOB_UPDATED", "Updated job posting: " + job.getTitle() + " (ID #" + job.getJobId() + ")", "127.0.0.1");
        }
        return success;
    }

    public boolean setJobStatus(int jobId, String status) {
        boolean success = jobDAO.updateStatus(jobId, status);
        if (success) {
            activityLogDAO.log(null, "JOB_STATUS_CHANGE", "Changed status of job #" + jobId + " to " + status, "127.0.0.1");
        }
        return success;
    }

    public boolean deleteJob(int jobId) {
        Job job = jobDAO.findById(jobId);
        boolean success = jobDAO.delete(jobId);
        if (success && job != null) {
            activityLogDAO.log(null, "JOB_DELETED", "Deleted job opening: " + job.getTitle() + " (ID #" + jobId + ")", "127.0.0.1");
        }
        return success;
    }

    public List<JobSkill> getRequiredSkills(int jobId) {
        return jobDAO.getRequiredSkills(jobId);
    }

    public List<JobSkill> getPreferredSkills(int jobId) {
        return jobDAO.getPreferredSkills(jobId);
    }
}

