package com.talentiq.ui.analytics;

import com.talentiq.service.AnalyticsService;
import com.talentiq.service.CandidateService;
import com.talentiq.service.JobService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.ExportUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class AnalyticsReportsPanel extends JPanel {

    private final AnalyticsService analyticsService;
    private final CandidateService candidateService;
    private final JobService jobService;

    // 6 Chart Panels
    private final JPanel pnlChart1 = new JPanel(new BorderLayout());
    private final JPanel pnlChart2 = new JPanel(new BorderLayout());
    private final JPanel pnlChart3 = new JPanel(new BorderLayout());
    private final JPanel pnlChart4 = new JPanel(new BorderLayout());
    private final JPanel pnlChart5 = new JPanel(new BorderLayout());
    private final JPanel pnlChart6 = new JPanel(new BorderLayout());

    public AnalyticsReportsPanel(AnalyticsService analyticsService, CandidateService candidateService, JobService jobService) {
        this.analyticsService = analyticsService;
        this.candidateService = candidateService;
        this.jobService = jobService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("TalentIQ Analytics & Institutional Intelligence");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton btnExportCandidates = new ModernButton("Export Candidates (CSV)", ModernButton.ButtonType.SECONDARY);
        JButton btnExportJobs = new ModernButton("Export Jobs (CSV)", ModernButton.ButtonType.SECONDARY);
        JButton btnRefresh = new ModernButton("Refresh Charts", ModernButton.ButtonType.PRIMARY);

        btnExportCandidates.addActionListener(e -> exportCandidatesCSV());
        btnExportJobs.addActionListener(e -> exportJobsCSV());
        btnRefresh.addActionListener(e -> loadCharts());

        btnBar.add(btnExportCandidates);
        btnBar.add(btnExportJobs);
        btnBar.add(btnRefresh);
        header.add(btnBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 6 Charts Grid (2 columns x 3 rows)
        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 14));
        grid.setPreferredSize(new Dimension(850, 800));

        pnlChart1.setBorder(BorderFactory.createTitledBorder("Candidate Experience Distribution"));
        pnlChart2.setBorder(BorderFactory.createTitledBorder("Applications per Job Opening"));
        pnlChart3.setBorder(BorderFactory.createTitledBorder("Top Candidate Skills Indexed"));
        pnlChart4.setBorder(BorderFactory.createTitledBorder("Most Demanded Technical Skills"));
        pnlChart5.setBorder(BorderFactory.createTitledBorder("Matching Score Distribution (Pie)"));
        pnlChart6.setBorder(BorderFactory.createTitledBorder("Application Recruitment Statuses"));

        grid.add(pnlChart1);
        grid.add(pnlChart2);
        grid.add(pnlChart3);
        grid.add(pnlChart4);
        grid.add(pnlChart5);
        grid.add(pnlChart6);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        loadCharts();
    }

    public void loadCharts() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private JFreeChart c1, c2, c3, c4, c5, c6;

            @Override
            protected Void doInBackground() {
                c1 = ChartFactory.createBarChart(null, "Tenure Bracket", "Candidates",
                        analyticsService.getExperienceDataset(), PlotOrientation.VERTICAL, false, true, false);

                c2 = ChartFactory.createBarChart(null, "Job Vacancy", "Applications",
                        analyticsService.getApplicationsPerJobDataset(), PlotOrientation.VERTICAL, false, true, false);

                c3 = ChartFactory.createBarChart(null, "Skill", "Candidate Count",
                        analyticsService.getTopCandidateSkillsDataset(8), PlotOrientation.HORIZONTAL, false, true, false);

                c4 = ChartFactory.createBarChart(null, "Skill", "Job Requirement Count",
                        analyticsService.getTopDemandedSkillsDataset(8), PlotOrientation.HORIZONTAL, false, true, false);

                c5 = ChartFactory.createPieChart(null, analyticsService.getMatchingScoreDataset(), true, true, false);

                c6 = ChartFactory.createPieChart(null, analyticsService.getApplicationStatusDataset(), true, true, false);

                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                setChartToPanel(pnlChart1, c1);
                setChartToPanel(pnlChart2, c2);
                setChartToPanel(pnlChart3, c3);
                setChartToPanel(pnlChart4, c4);
                setChartToPanel(pnlChart5, c5);
                setChartToPanel(pnlChart6, c6);
            }
        };
        worker.execute();
    }

    private void setChartToPanel(JPanel panel, JFreeChart chart) {
        panel.removeAll();
        if (chart != null) {
            ChartPanel cp = new ChartPanel(chart);
            panel.add(cp, BorderLayout.CENTER);
        }
        panel.revalidate();
        panel.repaint();
    }

    private void exportCandidatesCSV() {
        var list = candidateService.getAllCandidates(null, null, null);
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone", "Headline", "Total Exp", "Education", "Company", "Location"}, 0);
        for (var c : list) {
            model.addRow(new Object[]{
                    c.getCandidateId(), c.getFullName(), c.getEmail(), c.getPhone(),
                    c.getHeadline(), c.getTotalExperienceYears(), c.getHighestEducationLevel(),
                    c.getCurrentCompany(), c.getLocation()
            });
        }
        JTable dummy = new JTable(model);
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("TalentIQ_Candidates_Export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ExportUtil.exportTableToCSV(dummy, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Candidates successfully exported to CSV!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportJobsCSV() {
        var list = jobService.getJobs(null, null, null, null);
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Title", "Department", "Company", "Location", "Type", "Min Exp", "Min Edu", "Status", "Applications"}, 0);
        for (var j : list) {
            model.addRow(new Object[]{
                    j.getJobId(), j.getTitle(), j.getDepartment(), j.getCompanyName(),
                    j.getLocation(), j.getEmploymentType(), j.getMinExperienceYears(),
                    j.getMinEducationLevel(), j.getStatus(), j.getApplicationCount()
            });
        }
        JTable dummy = new JTable(model);
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("TalentIQ_Jobs_Export.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ExportUtil.exportTableToCSV(dummy, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Jobs successfully exported to CSV!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

