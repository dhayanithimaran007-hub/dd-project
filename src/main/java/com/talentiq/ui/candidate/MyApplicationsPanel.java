package com.talentiq.ui.candidate;

import com.talentiq.model.CandidateProfile;
import com.talentiq.model.JobApplication;
import com.talentiq.service.RankingService;
import com.talentiq.ui.components.BadgeCellRenderer;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.ProgressBarCellRenderer;
import com.talentiq.util.DateUtil;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyApplicationsPanel extends JPanel {

    private final RankingService rankingService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"App ID", "Position Title", "Company", "Match Score at Apply", "Current Status", "Applied Date", "Recruiter Feedback"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    public MyApplicationsPanel(RankingService rankingService) {
        this.rankingService = rankingService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("My Active Applications & Status Tracking");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new ModernButton("Refresh Applications", ModernButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> loadApplications());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        table.getColumnModel().getColumn(3).setCellRenderer(new ProgressBarCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new BadgeCellRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadApplications();
    }

    public void loadApplications() {
        CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
        if (cp == null) {
            tableModel.setRowCount(0);
            return;
        }

        tableModel.setRowCount(0);
        List<JobApplication> apps = rankingService.getApplicationsForCandidate(cp.getCandidateId());
        for (JobApplication a : apps) {
            tableModel.addRow(new Object[]{
                    a.getApplicationId(),
                    a.getJobTitle(),
                    a.getCompanyName(),
                    a.getMatchingScoreAtApplication(),
                    a.getStatus(),
                    DateUtil.formatTimestamp(a.getAppliedDate()),
                    a.getRecruiterNotes() != null ? a.getRecruiterNotes() : "Under Review"
            });
        }
    }
}

