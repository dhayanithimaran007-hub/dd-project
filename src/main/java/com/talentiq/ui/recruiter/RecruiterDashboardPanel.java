package com.talentiq.ui.recruiter;

import com.talentiq.model.Job;
import com.talentiq.service.AnalyticsService;
import com.talentiq.service.JobService;
import com.talentiq.ui.components.BadgeCellRenderer;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.StatCard;
import com.talentiq.ui.theme.ThemeManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class RecruiterDashboardPanel extends JPanel {

    private final JobService jobService;
    private final AnalyticsService analyticsService;
    private final java.util.function.Consumer<String> navigationCallback;

    private final StatCard cardJobs = new StatCard("Active Vacancies", "0", "Open job listings", ThemeManager.COLOR_PRIMARY);
    private final StatCard cardCandidates = new StatCard("Talent Pool", "0", "Available candidates", ThemeManager.COLOR_INFO);
    private final StatCard cardApplications = new StatCard("Applications", "0", "Total applicant submissions", ThemeManager.COLOR_SUCCESS);
    private final StatCard cardShortlisted = new StatCard("Shortlisted", "0", "Passed initial screening", ThemeManager.COLOR_WARNING);

    private final DefaultTableModel jobsTableModel = new DefaultTableModel(
            new String[]{"Job ID", "Position Title", "Department", "Type", "Status", "Applications"}, 0);
    private final ModernTable jobsTable = new ModernTable(jobsTableModel);

    private final JPanel chartContainer = new JPanel(new BorderLayout());

    public RecruiterDashboardPanel(JobService jobService, AnalyticsService analyticsService,
                                   java.util.function.Consumer<String> navigationCallback) {
        this.jobService = jobService;
        this.analyticsService = analyticsService;
        this.navigationCallback = navigationCallback;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Recruitment Workspace & Talent Analytics");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(lblTitle, BorderLayout.WEST);

        JButton btnRankAction = new ModernButton("Go to Candidate Ranking →", ModernButton.ButtonType.PRIMARY);
        btnRankAction.addActionListener(e -> {
            if (navigationCallback != null) navigationCallback.accept("RECRUITER_RANKING");
        });
        topBar.add(btnRankAction, BorderLayout.EAST);
        content.add(topBar);
        content.add(Box.createVerticalStrut(16));

        // 4 Stat Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.add(cardJobs);
        statsGrid.add(cardCandidates);
        statsGrid.add(cardApplications);
        statsGrid.add(cardShortlisted);
        content.add(statsGrid);
        content.add(Box.createVerticalStrut(18));

        // Split Section: Active Jobs Table on Left, Top Demanded Skills Chart on Right
        JPanel middleSplit = new JPanel(new GridLayout(1, 2, 14, 14));
        middleSplit.setPreferredSize(new Dimension(800, 360));

        JPanel jobsPanel = new JPanel(new BorderLayout());
        jobsPanel.setBorder(BorderFactory.createTitledBorder("Active Vacancies (Quick Access)"));
        jobsTable.getColumnModel().getColumn(4).setCellRenderer(new BadgeCellRenderer());
        jobsPanel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);

        JPanel btnQuickRankPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        JButton btnQuickRank = new ModernButton("Rank Applicants for Selected Job", ModernButton.ButtonType.PRIMARY);
        btnQuickRank.addActionListener(e -> {
            if (navigationCallback != null) navigationCallback.accept("RECRUITER_RANKING");
        });
        btnQuickRankPanel.add(btnQuickRank);
        jobsPanel.add(btnQuickRankPanel, BorderLayout.SOUTH);
        middleSplit.add(jobsPanel);

        chartContainer.setBorder(BorderFactory.createTitledBorder("Most Demanded Technical Skills"));
        middleSplit.add(chartContainer);
        content.add(middleSplit);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        loadDashboard();
    }

    public void loadDashboard() {
        Map<String, Integer> counts = analyticsService.getDashboardCounts();
        cardJobs.setValue(String.valueOf(counts.getOrDefault("active_jobs", 0)));
        cardCandidates.setValue(String.valueOf(counts.getOrDefault("candidates", 0)));
        cardApplications.setValue(String.valueOf(counts.getOrDefault("applications", 0)));
        cardShortlisted.setValue(String.valueOf(counts.getOrDefault("shortlisted", 0)));

        // Load jobs table
        jobsTableModel.setRowCount(0);
        List<Job> jobs = jobService.getJobs(null, Job.STATUS_ACTIVE, null, null);
        for (Job j : jobs) {
            jobsTableModel.addRow(new Object[]{
                    j.getJobId(),
                    j.getTitle(),
                    j.getDepartment(),
                    j.getEmploymentType(),
                    j.getStatus(),
                    j.getApplicationCount()
            });
        }

        // Render Demanded Skills Chart
        JFreeChart chart = ChartFactory.createBarChart(
                null, "Skills", "Demand in Jobs",
                analyticsService.getTopDemandedSkillsDataset(8),
                PlotOrientation.HORIZONTAL, false, true, false
        );

        chartContainer.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        chartContainer.add(cp, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }
}

