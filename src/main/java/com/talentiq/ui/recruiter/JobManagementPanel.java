package com.talentiq.ui.recruiter;

import com.talentiq.model.Job;
import com.talentiq.model.Recruiter;
import com.talentiq.service.JobService;
import com.talentiq.service.MatchingService;
import com.talentiq.service.SkillService;
import com.talentiq.ui.components.BadgeCellRenderer;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.SearchFilterPanel;
import com.talentiq.ui.dialogs.JobEditDialog;
import com.talentiq.util.DateUtil;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class JobManagementPanel extends JPanel {

    private final JobService jobService;
    private final SkillService skillService;
    private final MatchingService matchingService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Job ID", "Job Title", "Department", "Company", "Type", "Min Exp", "Min Edu", "Status", "Applications", "Deadline"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    private final SearchFilterPanel searchPanel = new SearchFilterPanel("Search title, location...", new String[]{"All Statuses", "ACTIVE", "CLOSED", "DRAFT"});

    public JobManagementPanel(JobService jobService, SkillService skillService, MatchingService matchingService) {
        this.jobService = jobService;
        this.skillService = skillService;
        this.matchingService = matchingService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Job Vacancy & Requirements Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(lblTitle, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        JButton btnCreate = new ModernButton("+ Post New Job", ModernButton.ButtonType.PRIMARY);
        JButton btnEdit = new ModernButton("Edit Job", ModernButton.ButtonType.SECONDARY);
        JButton btnToggle = new ModernButton("Activate / Close", ModernButton.ButtonType.SECONDARY);
        JButton btnDelete = new ModernButton("Delete", ModernButton.ButtonType.DANGER);
        JButton btnRefresh = new ModernButton("Refresh", ModernButton.ButtonType.SECONDARY);

        btnBar.add(btnCreate);
        btnBar.add(btnEdit);
        btnBar.add(btnToggle);
        btnBar.add(btnDelete);
        btnBar.add(btnRefresh);

        topBar.add(btnBar, BorderLayout.EAST);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(topBar, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        table.getColumnModel().getColumn(7).setCellRenderer(new BadgeCellRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Actions
        btnCreate.addActionListener(e -> showJobDialog(new Job()));
        btnEdit.addActionListener(e -> {
            Job j = getSelectedJob();
            if (j != null) showJobDialog(j);
        });

        btnToggle.addActionListener(e -> {
            Job j = getSelectedJob();
            if (j != null) {
                String newStatus = Job.STATUS_ACTIVE.equalsIgnoreCase(j.getStatus()) ? Job.STATUS_CLOSED : Job.STATUS_ACTIVE;
                jobService.setJobStatus(j.getJobId(), newStatus);
                loadJobs();
            }
        });

        btnDelete.addActionListener(e -> {
            Job j = getSelectedJob();
            if (j != null) {
                int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete job '" + j.getTitle() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    jobService.deleteJob(j.getJobId());
                    loadJobs();
                }
            }
        });

        btnRefresh.addActionListener(e -> loadJobs());
        searchPanel.addSearchListener(e -> loadJobs());
        searchPanel.addResetListener(e -> loadJobs());

        loadJobs();
    }

    public void loadJobs() {
        String kw = searchPanel.getSearchText();
        String filter = searchPanel.getSelectedFilter();
        String status = "All Statuses".equalsIgnoreCase(filter) ? null : filter;

        Recruiter r = SessionManager.getInstance().getCurrentRecruiter();
        Integer recruiterId = (r != null && !SessionManager.getInstance().isAdmin()) ? r.getRecruiterId() : null;

        List<Job> list = jobService.getJobs(kw, status, null, recruiterId);
        tableModel.setRowCount(0);

        for (Job j : list) {
            tableModel.addRow(new Object[]{
                    j.getJobId(),
                    j.getTitle(),
                    j.getDepartment(),
                    j.getCompanyName(),
                    j.getEmploymentType(),
                    j.getMinExperienceYears() + " yrs",
                    j.getMinEducationLevel(),
                    j.getStatus(),
                    j.getApplicationCount(),
                    DateUtil.formatDate(j.getDeadline())
            });
        }
    }

    private Job getSelectedJob() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job opening first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int jobId = (int) tableModel.getValueAt(row, 0);
        return jobService.getJobById(jobId);
    }

    private void showJobDialog(Job job) {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        Recruiter r = SessionManager.getInstance().getCurrentRecruiter();
        int rId = r != null ? r.getRecruiterId() : 1;

        JobEditDialog dlg = new JobEditDialog(topFrame, job, rId, jobService, skillService);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadJobs();
        }
    }
}

