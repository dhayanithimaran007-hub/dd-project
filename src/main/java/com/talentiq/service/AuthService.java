package com.talentiq.service;

import com.talentiq.dao.ActivityLogDAO;
import com.talentiq.dao.CandidateDAO;
import com.talentiq.dao.RecruiterDAO;
import com.talentiq.dao.UserDAO;
import com.talentiq.model.CandidateProfile;
import com.talentiq.model.Recruiter;
import com.talentiq.model.Role;
import com.talentiq.model.User;
import com.talentiq.util.PasswordUtil;
import com.talentiq.util.SessionManager;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    private final UserDAO userDAO = new UserDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final RecruiterDAO recruiterDAO = new RecruiterDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public boolean login(String usernameOrEmail, String plainPassword) {
        if (usernameOrEmail == null || plainPassword == null || usernameOrEmail.isBlank() || plainPassword.isBlank()) {
            return false;
        }

        User user = userDAO.findByUsername(usernameOrEmail.trim());
        if (user == null) {
            user = userDAO.findByEmail(usernameOrEmail.trim());
        }

        if (user == null || !user.isActive()) {
            return false;
        }

        if (PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
            CandidateProfile candidate = null;
            Recruiter recruiter = null;

            if (user.isCandidate()) {
                candidate = candidateDAO.findByUserId(user.getUserId());
            } else if (user.isRecruiter()) {
                recruiter = recruiterDAO.findByUserId(user.getUserId());
            }

            SessionManager.getInstance().login(user, candidate, recruiter);
            activityLogDAO.log(user.getUserId(), "USER_LOGIN", "User '" + user.getUsername() + "' logged in successfully.", "127.0.0.1");
            return true;
        }

        return false;
    }

    public boolean registerCandidate(String username, String plainPassword, String email, String fullName, String phone, String headline) throws Exception {
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setFullName(fullName.trim());
        user.setPhone(phone != null ? phone.trim() : "");
        user.setRoleId(Role.ROLE_CANDIDATE_ID);
        user.setActive(true);

        int userId = userDAO.create(user, plainPassword);
        if (userId > 0) {
            CandidateProfile profile = new CandidateProfile();
            profile.setUserId(userId);
            profile.setFullName(fullName);
            profile.setEmail(email);
            profile.setPhone(phone);
            profile.setHeadline(headline != null && !headline.isBlank() ? headline.trim() : "Software Professional");
            profile.setStatus("ACTIVE");
            candidateDAO.create(profile);

            activityLogDAO.log(userId, "CANDIDATE_REGISTER", "New candidate registered: " + fullName + " (" + username + ")", "127.0.0.1");
            return true;
        }

        return false;
    }

    public void logout() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            activityLogDAO.log(user.getUserId(), "USER_LOGOUT", "User '" + user.getUsername() + "' logged out.", "127.0.0.1");
        }
        SessionManager.getInstance().logout();
    }
}

