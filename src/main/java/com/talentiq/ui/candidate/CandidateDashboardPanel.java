package com.talentiq.ui.candidate;

import com.talentiq.model.CandidateProfile;
import com.talentiq.model.JobApplication;
import com.talentiq.service.CandidateService;
import com.talentiq.service.RankingService;
import com.talentiq.service.ResumeService;
import com.talentiq.ui.components.BadgeCellRenderer;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.StatCard;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.DateUtil;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class CandidateDashboardPanel extends JPanel {

    private final CandidateService candidateService;
    private final ResumeService resumeService;
    private final RankingService rankingService;
    private final Consumer<String> navigationCallback;

    private final JLabel lblWelcome = new JLabel("Welcome to TalentIQ");
    private final StatCard cardSkills = new StatCard("Technical Skills", "0", "Indexed competencies", ThemeManager.COLOR_PRIMARY);
    private final StatCard cardExp = new StatCard("Experience", "0 yrs", "Professional tenure", ThemeManager.COLOR_INFO);
    private final StatCard cardApps = new StatCard("Active Applications", "0", "Jobs submitted", ThemeManager.COLOR_SUCCESS);
    private final StatCard cardResumes = new StatCard("Primary Resume", "None", "Active document", ThemeManager.COLOR_WARNING);

    private final DefaultTableModel appTableModel = new DefaultTableModel(
            new String[]{"App ID", "Position Title", "Company", "Match Score", "Status", "Date"}, 0);
    private final ModernTable appTable = new ModernTable(appTableModel);

    public CandidateDashboardPanel(CandidateService candidateService, ResumeService resumeService,
                                   RankingService rankingService, Consumer<String> navigationCallback) {
        this.candidateService = candidateService;
        this.resumeService = resumeService;
        this.rankingService = rankingService;
        this.navigationCallback = navigationCallback;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Welcome banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setOpaque(false);

        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(ThemeManager.COLOR_PRIMARY);

        JLabel lblSub = new JLabel("Your centralized portal for resume parsing, skill profiling, and job matching.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel welcomeBox = new JPanel();
        welcomeBox.setLayout(new BoxLayout(welcomeBox, BoxLayout.Y_AXIS));
        welcomeBox.setOpaque(false);
        welcomeBox.add(lblWelcome);
        welcomeBox.add(Box.createVerticalStrut(4));
        welcomeBox.add(lblSub);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        quickActions.setOpaque(false);
        JButton btnUpload = new ModernButton("Upload Resume", ModernButton.ButtonType.PRIMARY);
        JButton btnBrowse = new ModernButton("Browse Jobs", ModernButton.ButtonType.SUCCESS);

        btnUpload.addActionListener(e -> {
            if (navigationCallback != null) navigationCallback.accept("CANDIDATE_RESUME");
        });
        btnBrowse.addActionListener(e -> {
            if (navigationCallback != null) navigationCallback.accept("CANDIDATE_JOBS");
        });

        quickActions.add(btnUpload);
        quickActions.add(btnBrowse);

        banner.add(welcomeBox, BorderLayout.WEST);
        banner.add(quickActions, BorderLayout.EAST);
        content.add(banner);
        content.add(Box.createVerticalStrut(16));

        // Stat Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.add(cardSkills);
        statsGrid.add(cardExp);
        statsGrid.add(cardApps);
        statsGrid.add(cardResumes);
        content.add(statsGrid);
        content.add(Box.createVerticalStrut(18));

        // Recent Applications Table Card
        JPanel appSection = new JPanel(new BorderLayout(8, 8));
        appSection.setBorder(BorderFactory.createTitledBorder("My Recent Applications"));
        appTable.getColumnModel().getColumn(4).setCellRenderer(new BadgeCellRenderer());
        appSection.add(new JScrollPane(appTable), BorderLayout.CENTER);
        content.add(appSection);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        loadDashboard();
    }

    public void loadDashboard() {
        CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
        if (cp == null) return;

        lblWelcome.setText("Welcome back, " + cp.getFullName() + "!");

        int skillCount = cp.getSkills() != null ? cp.getSkills().size() : 0;
        cardSkills.setValue(String.valueOf(skillCount));
        cardExp.setValue(cp.getTotalExperienceYears() + " yrs");

        List<JobApplication> apps = rankingService.getApplicationsForCandidate(cp.getCandidateId());
        cardApps.setValue(String.valueOf(apps.size()));

        var primaryResume = resumeService.getPrimaryResume(cp.getCandidateId());
        cardResumes.setValue(primaryResume != null ? primaryResume.getFileType() : "None");
        cardResumes.setSubtitle(primaryResume != null ? primaryResume.getFileName() : "Upload a PDF/DOCX");

        appTableModel.setRowCount(0);
        for (JobApplication a : apps) {
            appTableModel.addRow(new Object[]{
                    a.getApplicationId(),
                    a.getJobTitle(),
                    a.getCompanyName(),
                    a.getMatchingScoreAtApplication() + "%",
                    a.getStatus(),
                    DateUtil.formatTimestamp(a.getAppliedDate())
            });
        }
    }
}

