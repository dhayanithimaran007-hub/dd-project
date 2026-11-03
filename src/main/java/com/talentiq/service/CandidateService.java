package com.talentiq.service;

import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.dao.CandidateDAO;
import com.talentiq.dao.UserDAO;
import com.talentiq.model.*;

import java.util.List;

public class CandidateService {

    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public CandidateProfile getCandidateById(int candidateId) {
        return candidateDAO.findById(candidateId);
    }

    public CandidateProfile getCandidateByUserId(int userId) {
        return candidateDAO.findByUserId(userId);
    }

    public List<CandidateProfile> getAllCandidates(String keyword, Double minExp, String educationLevel) {
        return candidateDAO.findAll(keyword, minExp, educationLevel);
    }

    public boolean updateCandidateProfile(CandidateProfile profile) {
        boolean success = candidateDAO.update(profile);
        if (success && profile.getUserId() > 0) {
            User user = userDAO.findById(profile.getUserId());
            if (user != null) {
                user.setFullName(profile.getFullName());
                user.setEmail(profile.getEmail());
                user.setPhone(profile.getPhone());
                userDAO.update(user);
            }
            activityLogDAO.log(profile.getUserId(), "PROFILE_UPDATED", "Candidate profile updated for " + profile.getFullName(), "127.0.0.1");
        }
        return success;
    }

    public boolean deleteCandidate(int candidateId) {
        CandidateProfile p = candidateDAO.findById(candidateId);
        boolean success = candidateDAO.delete(candidateId);
        if (success && p != null) {
            userDAO.delete(p.getUserId());
            activityLogDAO.log(null, "CANDIDATE_DELETED", "Deleted candidate ID #" + candidateId + " (" + p.getFullName() + ")", "127.0.0.1");
        }
        return success;
    }

    // Skills
    public List<CandidateSkill> getSkills(int candidateId) {
        return candidateDAO.getSkills(candidateId);
    }

    public boolean addOrUpdateSkill(CandidateSkill skill) {
        return candidateDAO.addOrUpdateSkill(skill);
    }

    public boolean deleteSkill(int candidateId, int skillId) {
        return candidateDAO.deleteSkill(candidateId, skillId);
    }

    // Education
    public List<Education> getEducation(int candidateId) {
        return candidateDAO.getEducation(candidateId);
    }

    public boolean addEducation(Education edu) {
        return candidateDAO.addEducation(edu);
    }

    public boolean deleteEducation(int educationId) {
        return candidateDAO.deleteEducation(educationId);
    }

    // Work Experience
    public List<WorkExperience> getExperience(int candidateId) {
        return candidateDAO.getExperience(candidateId);
    }

    public boolean addExperience(WorkExperience exp) {
        return candidateDAO.addExperience(exp);
    }

    public boolean deleteExperience(int experienceId) {
        return candidateDAO.deleteExperience(experienceId);
    }

    // Certifications
    public List<Certification> getCertifications(int candidateId) {
        return candidateDAO.getCertifications(candidateId);
    }

    public boolean addCertification(Certification cert) {
        return candidateDAO.addCertification(cert);
    }

    public boolean deleteCertification(int certId) {
        return candidateDAO.deleteCertification(certId);
    }

    // Projects
    public List<Project> getProjects(int candidateId) {
        return candidateDAO.getProjects(candidateId);
    }

    public boolean addProject(Project project) {
        return candidateDAO.addProject(project);
    }

    public boolean deleteProject(int projectId) {
        return candidateDAO.deleteProject(projectId);
    }
}

