package com.talentiq.ui.recruiter;

import com.talentiq.model.Job;
import com.talentiq.model.JobApplication;
import com.talentiq.service.JobService;
import com.talentiq.service.RankingService;
import com.talentiq.ui.components.BadgeCellRenderer;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.ProgressBarCellRenderer;
import com.talentiq.util.DateUtil;
import com.talentiq.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class JobApplicationManagementPanel extends JPanel {

    private final JobService jobService;
    private final RankingService rankingService;

    private final JComboBox<Job> cmbJobs = new JComboBox<>();
    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"All Statuses", "APPLIED", "SHORTLISTED", "INTERVIEW_SCHEDULED", "HIRED", "REJECTED"});

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"App ID", "Job Title", "Candidate Name", "Email", "Experience", "Match Score", "Status", "Applied Date", "Notes"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    public JobApplicationManagementPanel(JobService jobService, RankingService rankingService) {
        this.jobService = jobService;
        this.rankingService = rankingService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Candidate Applications & Recruitment Workflow");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setOpaque(false);

        controls.add(new JLabel("Filter by Job:"));
        cmbJobs.setPreferredSize(new Dimension(280, 32));
        cmbJobs.addActionListener(e -> loadApplications());
        controls.add(cmbJobs);

        controls.add(new JLabel("Status:"));
        cmbStatusFilter.setPreferredSize(new Dimension(160, 32));
        cmbStatusFilter.addActionListener(e -> loadApplications());
        controls.add(cmbStatusFilter);

        header.add(controls, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        table.getColumnModel().getColumn(5).setCellRenderer(new ProgressBarCellRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new BadgeCellRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setOpaque(false);

        JButton btnShortlist = new ModernButton("Shortlist", ModernButton.ButtonType.SUCCESS);
        JButton btnInterview = new ModernButton("Schedule Interview", ModernButton.ButtonType.PRIMARY);
        JButton btnHire = new ModernButton("Hire", ModernButton.ButtonType.SUCCESS);
        JButton btnReject = new ModernButton("Reject", ModernButton.ButtonType.DANGER);
        JButton btnNotes = new ModernButton("Edit Recruiter Notes", ModernButton.ButtonType.SECONDARY);
        JButton btnExport = new ModernButton("Export CSV", ModernButton.ButtonType.SECONDARY);

        btnShortlist.addActionListener(e -> updateSelectedStatus("SHORTLISTED"));
        btnInterview.addActionListener(e -> updateSelectedStatus("INTERVIEW_SCHEDULED"));
        btnHire.addActionListener(e -> updateSelectedStatus("HIRED"));
        btnReject.addActionListener(e -> updateSelectedStatus("REJECTED"));
        btnNotes.addActionListener(e -> editNotes());
        btnExport.addActionListener(e -> exportCSV());

        bottomBar.add(btnShortlist);
        bottomBar.add(btnInterview);
        bottomBar.add(btnHire);
        bottomBar.add(btnReject);
        bottomBar.add(btnNotes);
        bottomBar.add(btnExport);
        add(bottomBar, BorderLayout.SOUTH);

        loadJobs();
    }

    public void loadJobs() {
        cmbJobs.removeAllItems();
        Job allJob = new Job();
        allJob.setJobId(0);
        allJob.setTitle("All Job Openings");
        cmbJobs.addItem(allJob);

        List<Job> jobs = jobService.getJobs(null, null, null, null);
        for (Job j : jobs) {
            cmbJobs.addItem(j);
        }
        if (cmbJobs.getItemCount() > 0) cmbJobs.setSelectedIndex(0);
    }

    private void loadApplications() {
        Job job = (Job) cmbJobs.getSelectedItem();
        int jobId = job != null ? job.getJobId() : 0;
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();

        List<JobApplication> apps;
        if (jobId == 0) {
            // Load all by querying each job
            apps = rankingService.getApplicationsForJob(1); // placeholder, let's load all jobs
            List<Job> allJ = jobService.getJobs(null, null, null, null);
            apps = new java.util.ArrayList<>();
            for (Job j : allJ) {
                apps.addAll(rankingService.getApplicationsForJob(j.getJobId()));
            }
        } else {
            apps = rankingService.getApplicationsForJob(jobId);
        }

        tableModel.setRowCount(0);
        for (JobApplication a : apps) {
            if (statusFilter != null && !"All Statuses".equalsIgnoreCase(statusFilter) && !statusFilter.equalsIgnoreCase(a.getStatus())) {
                continue;
            }
            tableModel.addRow(new Object[]{
                    a.getApplicationId(),
                    a.getJobTitle(),
                    a.getCandidateName(),
                    a.getCandidateEmail(),
                    a.getCandidateTotalExperience() + " yrs",
                    a.getMatchingScoreAtApplication(),
                    a.getStatus(),
                    DateUtil.formatTimestamp(a.getAppliedDate()),
                    a.getRecruiterNotes() != null ? a.getRecruiterNotes() : ""
            });
        }
    }

    private void updateSelectedStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application row first.");
            return;
        }
        int appId = (int) tableModel.getValueAt(row, 0);
        String curNotes = (String) tableModel.getValueAt(row, 8);
        new com.talentiq.dao.JobApplicationDAO().updateStatus(appId, newStatus, curNotes);
        JOptionPane.showMessageDialog(this, "Application status changed to: " + newStatus);
        loadApplications();
    }

    private void editNotes() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application row first.");
            return;
        }
        int appId = (int) tableModel.getValueAt(row, 0);
        String curNotes = (String) tableModel.getValueAt(row, 8);
        String status = (String) tableModel.getValueAt(row, 6);

        String newNotes = JOptionPane.showInputDialog(this, "Enter recruiter feedback/notes:", curNotes);
        if (newNotes != null) {
            new com.talentiq.dao.JobApplicationDAO().updateStatus(appId, status, newNotes);
            loadApplications();
        }
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("TalentIQ_Applications.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ExportUtil.exportTableToCSV(table, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Applications exported to CSV successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

