package com.talentiq.ui.candidate;

import com.talentiq.model.CandidateProfile;
import com.talentiq.model.Job;
import com.talentiq.model.MatchResult;
import com.talentiq.service.JobService;
import com.talentiq.service.MatchingService;
import com.talentiq.service.RankingService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.ProgressBarCellRenderer;
import com.talentiq.ui.dialogs.MatchDetailDialog;
import com.talentiq.util.DateUtil;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandidateJobsPanel extends JPanel {

    private final JobService jobService;
    private final MatchingService matchingService;
    private final RankingService rankingService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Job ID", "Position Title", "Company", "Location", "Type", "Min Exp", "My Match Score", "Deadline"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    private final Map<Integer, MatchResult> matchCache = new HashMap<>();

    public CandidateJobsPanel(JobService jobService, MatchingService matchingService, RankingService rankingService) {
        this.jobService = jobService;
        this.matchingService = matchingService;
        this.rankingService = rankingService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Explore Vacancies with Real-Time Skill Matching Scores");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new ModernButton("Refresh Jobs", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> loadJobs());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        table.getColumnModel().getColumn(6).setCellRenderer(new ProgressBarCellRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setOpaque(false);

        JButton btnViewBreakdown = new ModernButton("View Score Breakdown", ModernButton.ButtonType.SECONDARY);
        JButton btnApply = new ModernButton("Apply for Selected Job", ModernButton.ButtonType.PRIMARY);

        btnViewBreakdown.addActionListener(e -> showSelectedBreakdown());
        btnApply.addActionListener(e -> applySelectedJob());

        bottomBar.add(btnViewBreakdown);
        bottomBar.add(btnApply);
        add(bottomBar, BorderLayout.SOUTH);

        loadJobs();
    }

    public void loadJobs() {
        CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
        if (cp == null) {
            tableModel.setRowCount(0);
            return;
        }

        matchCache.clear();
        tableModel.setRowCount(0);

        List<Job> jobs = jobService.getJobs(null, Job.STATUS_ACTIVE, null, null);
        for (Job j : jobs) {
            MatchResult mr = matchingService.calculateMatch(cp, j);
            matchCache.put(j.getJobId(), mr);

            tableModel.addRow(new Object[]{
                    j.getJobId(),
                    j.getTitle(),
                    j.getCompanyName(),
                    j.getLocation(),
                    j.getEmploymentType(),
                    j.getMinExperienceYears() + " yrs",
                    mr.getOverallScore(),
                    DateUtil.formatDate(j.getDeadline())
            });
        }
    }

    private void showSelectedBreakdown() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job row first.");
            return;
        }
        int jobId = (int) tableModel.getValueAt(row, 0);
        MatchResult mr = matchCache.get(jobId);
        if (mr != null) {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new MatchDetailDialog(topFrame, mr, rankingService, null).setVisible(true);
        }
    }

    private void applySelectedJob() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job row first.");
            return;
        }
        int jobId = (int) tableModel.getValueAt(row, 0);
        String jobTitle = (String) tableModel.getValueAt(row, 1);
        CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
        if (cp == null) return;

        MatchResult mr = matchCache.get(jobId);
        double score = mr != null ? mr.getOverallScore() : 0.0;

        try {
            boolean success = rankingService.applyForJob(jobId, cp.getCandidateId(), score);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Successfully submitted application for: " + jobTitle + "!\nCalculated Match Score: " + score + "%",
                        "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IllegalStateException dup) {
            JOptionPane.showMessageDialog(this, dup.getMessage(), "Duplicate Application", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to apply: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

