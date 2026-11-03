package com.talentiq.ui.admin;

import com.talentiq.model.ActivityLog;
import com.talentiq.service.ActivityLogService;
import com.talentiq.service.AnalyticsService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.StatCard;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.DateUtil;
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

public class AdminDashboardPanel extends JPanel {

    private final AnalyticsService analyticsService;
    private final ActivityLogService logService;

    // Stat cards
    private final StatCard cardCandidates = new StatCard("Total Candidates", "0", "Registered candidates", ThemeManager.COLOR_PRIMARY);
    private final StatCard cardRecruiters = new StatCard("Total Recruiters", "0", "Recruitment specialists", ThemeManager.COLOR_INFO);
    private final StatCard cardResumes = new StatCard("Total Resumes", "0", "Uploaded documents", ThemeManager.COLOR_SUCCESS);
    private final StatCard cardJobs = new StatCard("Total Jobs", "0", "Active openings", ThemeManager.COLOR_WARNING);
    private final StatCard cardApplications = new StatCard("Total Applications", "0", "Job submissions", new Color(139, 92, 246));
    private final StatCard cardShortlisted = new StatCard("Shortlisted", "0", "Screened candidates", new Color(236, 72, 153));

    // Charts
    private final JPanel chartContainer1 = new JPanel(new BorderLayout());
    private final JPanel chartContainer2 = new JPanel(new BorderLayout());

    // Activities Table
    private final DefaultTableModel logTableModel = new DefaultTableModel(
            new String[]{"ID", "Action", "Description", "User", "IP Address", "Timestamp"}, 0);
    private final ModernTable logTable = new ModernTable(logTableModel);

    public AdminDashboardPanel(AnalyticsService analyticsService, ActivityLogService logService) {
        this.analyticsService = analyticsService;
        this.logService = logService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblHeading = new JLabel("System Administration & Recruitment Operations");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeading.setForeground(UIManager.getColor("Label.foreground"));

        JButton btnRefresh = new ModernButton("Refresh Dashboard", ModernButton.ButtonType.PRIMARY);
        btnRefresh.addActionListener(e -> loadData());

        topBar.add(lblHeading, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        content.add(topBar);
        content.add(Box.createVerticalStrut(16));

        // 6 Stat Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.add(cardCandidates);
        statsGrid.add(cardRecruiters);
        statsGrid.add(cardResumes);
        statsGrid.add(cardJobs);
        statsGrid.add(cardApplications);
        statsGrid.add(cardShortlisted);
        content.add(statsGrid);
        content.add(Box.createVerticalStrut(20));

        // Charts Section
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 14, 14));
        chartsGrid.setOpaque(false);
        chartsGrid.setPreferredSize(new Dimension(800, 260));

        chartContainer1.setBorder(BorderFactory.createTitledBorder("Candidate Experience Distribution"));
        chartContainer2.setBorder(BorderFactory.createTitledBorder("Applications per Job Opening"));

        chartsGrid.add(chartContainer1);
        chartsGrid.add(chartContainer2);
        content.add(chartsGrid);
        content.add(Box.createVerticalStrut(20));

        // Activity Logs Section
        JPanel logSection = new JPanel(new BorderLayout());
        logSection.setOpaque(false);
        logSection.setBorder(BorderFactory.createTitledBorder("Recent System Activities"));

        logTable.setPreferredScrollableViewportSize(new Dimension(800, 180));
        logSection.add(new JScrollPane(logTable), BorderLayout.CENTER);
        content.add(logSection);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Map<String, Integer> counts;
            private List<ActivityLog> logs;
            private JFreeChart expChart;
            private JFreeChart appChart;

            @Override
            protected Void doInBackground() {
                counts = analyticsService.getDashboardCounts();
                logs = logService.getRecentActivities(20);

                expChart = ChartFactory.createBarChart(
                        null, "Experience Bracket", "Candidates",
                        analyticsService.getExperienceDataset(),
                        PlotOrientation.VERTICAL, false, true, false
                );

                appChart = ChartFactory.createBarChart(
                        null, "Job Vacancy", "Applications",
                        analyticsService.getApplicationsPerJobDataset(),
                        PlotOrientation.VERTICAL, false, true, false
                );
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                if (counts != null) {
                    cardCandidates.setValue(String.valueOf(counts.getOrDefault("candidates", 0)));
                    cardRecruiters.setValue(String.valueOf(counts.getOrDefault("recruiters", 0)));
                    cardResumes.setValue(String.valueOf(counts.getOrDefault("resumes", 0)));
                    cardJobs.setValue(counts.getOrDefault("active_jobs", 0) + " / " + counts.getOrDefault("total_jobs", 0));
                    cardApplications.setValue(String.valueOf(counts.getOrDefault("applications", 0)));
                    cardShortlisted.setValue(counts.getOrDefault("shortlisted", 0) + " (Hired: " + counts.getOrDefault("hired", 0) + ")");
                }

                // Render Charts
                chartContainer1.removeAll();
                if (expChart != null) {
                    ChartPanel cp1 = new ChartPanel(expChart);
                    cp1.setPreferredSize(new Dimension(380, 220));
                    chartContainer1.add(cp1, BorderLayout.CENTER);
                }
                chartContainer1.revalidate();
                chartContainer1.repaint();

                chartContainer2.removeAll();
                if (appChart != null) {
                    ChartPanel cp2 = new ChartPanel(appChart);
                    cp2.setPreferredSize(new Dimension(380, 220));
                    chartContainer2.add(cp2, BorderLayout.CENTER);
                }
                chartContainer2.revalidate();
                chartContainer2.repaint();

                // Render Logs Table
                logTableModel.setRowCount(0);
                if (logs != null) {
                    for (ActivityLog l : logs) {
                        logTableModel.addRow(new Object[]{
                                l.getLogId(),
                                l.getActionType(),
                                l.getDescription(),
                                l.getUsername() != null ? l.getUsername() : "System",
                                l.getIpAddress(),
                                DateUtil.formatTimestamp(l.getCreatedAt())
                        });
                    }
                }
            }
        };
        worker.execute();
    }
}

