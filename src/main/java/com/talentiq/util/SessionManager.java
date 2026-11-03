package com.talentiq.util;

import com.talentiq.model.CandidateProfile;
import com.talentiq.model.Recruiter;
import com.talentiq.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe singleton session manager tracking current authentication state and active user.
 */
public class SessionManager {

    private static SessionManager instance;

    private User currentUser;
    private CandidateProfile currentCandidate;
    private Recruiter currentRecruiter;

    public interface SessionListener {
        void onUserLoggedIn(User user);
        void onUserLoggedOut();
    }

    private final List<SessionListener> listeners = new ArrayList<>();

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public synchronized void login(User user, CandidateProfile candidate, Recruiter recruiter) {
        this.currentUser = user;
        this.currentCandidate = candidate;
        this.currentRecruiter = recruiter;

        for (SessionListener l : new ArrayList<>(listeners)) {
            try {
                l.onUserLoggedIn(user);
            } catch (Exception ignored) {}
        }
    }

    public synchronized void logout() {
        this.currentUser = null;
        this.currentCandidate = null;
        this.currentRecruiter = null;

        for (SessionListener l : new ArrayList<>(listeners)) {
            try {
                l.onUserLoggedOut();
            } catch (Exception ignored) {}
        }
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized CandidateProfile getCurrentCandidate() {
        return currentCandidate;
    }

    public synchronized void setCurrentCandidate(CandidateProfile candidate) {
        this.currentCandidate = candidate;
    }

    public synchronized Recruiter getCurrentRecruiter() {
        return currentRecruiter;
    }

    public synchronized void setCurrentRecruiter(Recruiter recruiter) {
        this.currentRecruiter = recruiter;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public synchronized boolean isRecruiter() {
        return currentUser != null && currentUser.isRecruiter();
    }

    public synchronized boolean isCandidate() {
        return currentUser != null && currentUser.isCandidate();
    }

    public synchronized void addSessionListener(SessionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeSessionListener(SessionListener listener) {
        listeners.remove(listener);
    }
}

