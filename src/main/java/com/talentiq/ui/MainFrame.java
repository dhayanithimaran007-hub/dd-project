package com.talentiq.ui;

import com.talentiq.config.AppConfig;
import com.talentiq.model.User;
import com.talentiq.service.*;
import com.talentiq.ui.admin.*;
import com.talentiq.ui.analytics.AnalyticsReportsPanel;
import com.talentiq.ui.auth.LoginDialog;
import com.talentiq.ui.candidate.*;
import com.talentiq.ui.components.SidebarPanel;
import com.talentiq.ui.components.TopNavBar;
import com.talentiq.ui.recruiter.*;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements SessionManager.SessionListener {

    // Services
    private final AuthService authService = new AuthService();
    private final CandidateService candidateService = new CandidateService();
    private final JobService jobService = new JobService();
    private final SkillService skillService = new SkillService();
    private final ResumeService resumeService = new ResumeService();
    private final MatchingService matchingService = new MatchingService();
    private final RankingService rankingService = new RankingService();
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final ActivityLogService logService = new ActivityLogService();
    private final SystemSettingService settingService = new SystemSettingService();

    // UI Panels
    private TopNavBar topNavBar;
    private SidebarPanel sidebarPanel;
    private final JPanel contentCardPanel = new JPanel(new CardLayout());

    // Screen views
    private AdminDashboardPanel adminDashboardPanel;
    private UserManagementPanel userManagementPanel;
    private CandidateDirectoryPanel candidateDirectoryPanel;
    private JobManagementPanel jobManagementPanel;
    private SkillManagementPanel skillManagementPanel;
    private SystemSettingsPanel systemSettingsPanel;
    private ActivityLogPanel activityLogPanel;

    private RecruiterDashboardPanel recruiterDashboardPanel;
    private CandidateRankingPanel candidateRankingPanel;
    private CandidateComparisonPanel candidateComparisonPanel;
    private SkillGapAnalysisPanel skillGapAnalysisPanel;
    private JobApplicationManagementPanel applicationManagementPanel;

    private CandidateDashboardPanel candidateDashboardPanel;
    private CandidateProfilePanel candidateProfilePanel;
    private ResumeUploadPanel resumeUploadPanel;
    private CandidateJobsPanel candidateJobsPanel;
    private MyApplicationsPanel myApplicationsPanel;

    private AnalyticsReportsPanel analyticsReportsPanel;

    public MainFrame() {
        setTitle(AppConfig.getAppName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 880);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        SessionManager.getInstance().addSessionListener(this);

        initViews();
        layoutComponents();
    }

    private void initViews() {
        topNavBar = new TopNavBar(this, authService);
        sidebarPanel = new SidebarPanel(this::navigateTo);

        adminDashboardPanel = new AdminDashboardPanel(analyticsService, logService);
        userManagementPanel = new UserManagementPanel();
        candidateDirectoryPanel = new CandidateDirectoryPanel(candidateService, authService);
        jobManagementPanel = new JobManagementPanel(jobService, skillService, matchingService);
        skillManagementPanel = new SkillManagementPanel(skillService);
        systemSettingsPanel = new SystemSettingsPanel(settingService);
        activityLogPanel = new ActivityLogPanel(logService);

        recruiterDashboardPanel = new RecruiterDashboardPanel(jobService, analyticsService, this::navigateTo);
        candidateRankingPanel = new CandidateRankingPanel(jobService, matchingService, rankingService, candidateService);
        candidateComparisonPanel = new CandidateComparisonPanel(jobService, matchingService, candidateService);
        skillGapAnalysisPanel = new SkillGapAnalysisPanel(jobService, candidateService, matchingService);
        applicationManagementPanel = new JobApplicationManagementPanel(jobService, rankingService);

        candidateDashboardPanel = new CandidateDashboardPanel(candidateService, resumeService, rankingService, this::navigateTo);
        candidateProfilePanel = new CandidateProfilePanel(candidateService, skillService);
        resumeUploadPanel = new ResumeUploadPanel(resumeService, skillService);
        candidateJobsPanel = new CandidateJobsPanel(jobService, matchingService, rankingService);
        myApplicationsPanel = new MyApplicationsPanel(rankingService);

        analyticsReportsPanel = new AnalyticsReportsPanel(analyticsService, candidateService, jobService);

        // Register card views
        contentCardPanel.add(adminDashboardPanel, "ADMIN_DASHBOARD");
        contentCardPanel.add(userManagementPanel, "ADMIN_USERS");
        contentCardPanel.add(candidateDirectoryPanel, "ADMIN_CANDIDATES");
        contentCardPanel.add(jobManagementPanel, "ADMIN_JOBS");
        contentCardPanel.add(skillManagementPanel, "ADMIN_SKILLS");
        contentCardPanel.add(systemSettingsPanel, "ADMIN_SETTINGS");
        contentCardPanel.add(activityLogPanel, "ADMIN_LOGS");

        contentCardPanel.add(recruiterDashboardPanel, "RECRUITER_DASHBOARD");
        contentCardPanel.add(candidateRankingPanel, "RECRUITER_RANKING");
        contentCardPanel.add(candidateComparisonPanel, "RECRUITER_COMPARISON");
        contentCardPanel.add(skillGapAnalysisPanel, "RECRUITER_GAP_ANALYSIS");
        contentCardPanel.add(applicationManagementPanel, "RECRUITER_APPLICATIONS");

        contentCardPanel.add(candidateDashboardPanel, "CANDIDATE_DASHBOARD");
        contentCardPanel.add(candidateProfilePanel, "CANDIDATE_PROFILE");
        contentCardPanel.add(resumeUploadPanel, "CANDIDATE_RESUME");
        contentCardPanel.add(candidateJobsPanel, "CANDIDATE_JOBS");
        contentCardPanel.add(myApplicationsPanel, "CANDIDATE_APPLICATIONS");

        contentCardPanel.add(analyticsReportsPanel, "REPORTS");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        add(topNavBar, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentCardPanel, BorderLayout.CENTER);
    }

    public void navigateTo(String viewKey) {
        CardLayout cl = (CardLayout) contentCardPanel.getLayout();
        cl.show(contentCardPanel, viewKey);

        topNavBar.setViewTitle(formatViewTitle(viewKey));

        // Refresh view data on entry
        switch (viewKey) {
            case "ADMIN_DASHBOARD":
                adminDashboardPanel.loadData();
                break;
            case "ADMIN_USERS":
                userManagementPanel.loadUsers();
                break;
            case "ADMIN_CANDIDATES":
                candidateDirectoryPanel.loadCandidates();
                break;
            case "ADMIN_JOBS":
            case "RECRUITER_JOBS":
                jobManagementPanel.loadJobs();
                break;
            case "RECRUITER_DASHBOARD":
                recruiterDashboardPanel.loadDashboard();
                break;
            case "RECRUITER_RANKING":
                candidateRankingPanel.loadJobs();
                candidateRankingPanel.calculateAndRank();
                break;
            case "RECRUITER_COMPARISON":
                candidateComparisonPanel.loadData();
                break;
            case "RECRUITER_GAP_ANALYSIS":
                skillGapAnalysisPanel.loadDropdowns();
                break;
            case "RECRUITER_APPLICATIONS":
                applicationManagementPanel.loadJobs();
                break;
            case "CANDIDATE_DASHBOARD":
                candidateDashboardPanel.loadDashboard();
                break;
            case "CANDIDATE_PROFILE":
                candidateProfilePanel.loadCurrentCandidate();
                break;
            case "CANDIDATE_RESUME":
                resumeUploadPanel.loadResumes();
                break;
            case "CANDIDATE_JOBS":
                candidateJobsPanel.loadJobs();
                break;
            case "CANDIDATE_APPLICATIONS":
                myApplicationsPanel.loadApplications();
                break;
            case "REPORTS":
                analyticsReportsPanel.loadCharts();
                break;
        }
    }

    private String formatViewTitle(String key) {
        if (key == null) return "Dashboard";
        String clean = key.replace("ADMIN_", "").replace("RECRUITER_", "").replace("CANDIDATE_", "");
        String[] words = clean.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public void onUserLoggedIn(User user) {
        topNavBar.updateUserStatus();
        sidebarPanel.rebuildMenu();
        setVisible(true);
    }

    @Override
    public void onUserLoggedOut() {
        setVisible(false);
        showLogin();
    }

    public void showLogin() {
        LoginDialog dlg = new LoginDialog(this, authService);
        dlg.setVisible(true);
        if (!dlg.isLoggedIn()) {
            System.exit(0);
        }
    }
}

