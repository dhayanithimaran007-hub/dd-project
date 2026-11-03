package com.talentiq.ui.recruiter;

import com.talentiq.model.CandidateProfile;
import com.talentiq.model.Job;
import com.talentiq.model.MatchDetail;
import com.talentiq.model.MatchResult;
import com.talentiq.service.CandidateService;
import com.talentiq.service.JobService;
import com.talentiq.service.MatchingService;
import com.talentiq.service.RankingService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.components.ProgressBarCellRenderer;
import com.talentiq.ui.dialogs.CandidateDetailDialog;
import com.talentiq.ui.dialogs.MatchDetailDialog;
import com.talentiq.util.DateUtil;
import com.talentiq.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class CandidateRankingPanel extends JPanel {

    private final JobService jobService;
    private final MatchingService matchingService;
    private final RankingService rankingService;
    private final CandidateService candidateService;

    private final JComboBox<Job> cmbJobs = new JComboBox<>();
    private final JTextField txtSkillFilter = new JTextField(12);
    private final JSpinner spnMinExp = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 30.0, 1.0));
    private final JComboBox<String> cmbEducation = new JComboBox<>(new String[]{"All", "Doctorate", "Master", "Bachelor", "Associate", "High School"});
    private final JComboBox<String> cmbMinScore = new JComboBox<>(new String[]{"All Scores", ">= 80% (Top Tier)", ">= 60% (Strong)", ">= 40% (Moderate)"});

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Candidate ID", "Rank", "Candidate Name", "Match %", "Req Skills (60%)", "Pref Skills (20%)", "Exp (10%)", "Edu (10%)", "Matched Skills", "Missing Required Skills"}, 0);
    private final ModernTable table = new ModernTable(tableModel);

    private List<MatchResult> currentResults;

    public CandidateRankingPanel(JobService jobService, MatchingService matchingService,
                                 RankingService rankingService, CandidateService candidateService) {
        this.jobService = jobService;
        this.matchingService = matchingService;
        this.rankingService = rankingService;
        this.candidateService = candidateService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header and Job Selector
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel lblHeading = new JLabel("Deterministic Candidate Ranking & Skill Evaluation");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(lblHeading, BorderLayout.NORTH);

        JPanel jobSelectBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        jobSelectBar.setOpaque(false);
        jobSelectBar.add(new JLabel("Select Target Job Vacancy:"));

        cmbJobs.setPreferredSize(new Dimension(320, 34));
        cmbJobs.addActionListener(e -> calculateAndRank());
        jobSelectBar.add(cmbJobs);

        JButton btnRecalc = new ModernButton("Re-evaluate All", ModernButton.ButtonType.PRIMARY);
        btnRecalc.addActionListener(e -> {
            Job j = (Job) cmbJobs.getSelectedItem();
            if (j != null) {
                matchingService.evaluateAllCandidatesForJob(j.getJobId());
                calculateAndRank();
            }
        });
        jobSelectBar.add(btnRecalc);

        topPanel.add(jobSelectBar, BorderLayout.CENTER);

        // Filter Bar (Skills, Experience, Education, Score Threshold)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setBorder(BorderFactory.createTitledBorder("Search & Ranking Filters"));

        txtSkillFilter.putClientProperty("JTextField.placeholderText", "Skill (e.g. Java)");
        txtSkillFilter.setPreferredSize(new Dimension(130, 28));

        filterBar.add(new JLabel("Skill:"));
        filterBar.add(txtSkillFilter);

        spnMinExp.setPreferredSize(new Dimension(65, 28));
        filterBar.add(new JLabel("Min Exp:"));
        filterBar.add(spnMinExp);

        filterBar.add(new JLabel("Min Education:"));
        filterBar.add(cmbEducation);

        filterBar.add(new JLabel("Score Threshold:"));
        filterBar.add(cmbMinScore);

        JButton btnApplyFilter = new ModernButton("Apply Filter", ModernButton.ButtonType.SECONDARY);
        JButton btnResetFilter = new ModernButton("Reset", ModernButton.ButtonType.SECONDARY);

        btnApplyFilter.addActionListener(e -> calculateAndRank());
        btnResetFilter.addActionListener(e -> {
            txtSkillFilter.setText("");
            spnMinExp.setValue(0.0);
            cmbEducation.setSelectedIndex(0);
            cmbMinScore.setSelectedIndex(0);
            calculateAndRank();
        });

        filterBar.add(btnApplyFilter);
        filterBar.add(btnResetFilter);
        topPanel.add(filterBar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        table.getColumnModel().getColumn(3).setCellRenderer(new ProgressBarCellRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(45);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setOpaque(false);

        JButton btnViewDetails = new ModernButton("View Match Breakdown", ModernButton.ButtonType.PRIMARY);
        JButton btnViewDossier = new ModernButton("View Candidate Profile", ModernButton.ButtonType.SECONDARY);
        JButton btnShortlist = new ModernButton("Shortlist Candidate", ModernButton.ButtonType.SUCCESS);
        JButton btnScheduleInterview = new ModernButton("Schedule Interview", ModernButton.ButtonType.PRIMARY);
        JButton btnHire = new ModernButton("Mark Hired", ModernButton.ButtonType.SUCCESS);
        JButton btnReject = new ModernButton("Reject Candidate", ModernButton.ButtonType.DANGER);
        JButton btnExportCSV = new ModernButton("Export Rankings (CSV)", ModernButton.ButtonType.SECONDARY);
        JButton btnExportPDF = new ModernButton("Export Match Report (PDF)", ModernButton.ButtonType.SECONDARY);

        btnViewDetails.addActionListener(e -> showSelectedMatchDetails());
        btnViewDossier.addActionListener(e -> showSelectedCandidateDossier());
        btnShortlist.addActionListener(e -> updateStatusForSelected("SHORTLISTED", "Candidate shortlisted based on high ranking score"));
        btnScheduleInterview.addActionListener(e -> updateStatusForSelected("INTERVIEW_SCHEDULED", "Technical interview scheduled with engineering team"));
        btnHire.addActionListener(e -> updateStatusForSelected("HIRED", "Offer extended and candidate hired!"));
        btnReject.addActionListener(e -> updateStatusForSelected("REJECTED", "Candidate rejected for this position"));
        btnExportCSV.addActionListener(e -> exportRankingsCSV());
        btnExportPDF.addActionListener(e -> exportSelectedMatchPDF());

        bottomBar.add(btnViewDetails);
        bottomBar.add(btnViewDossier);
        bottomBar.add(btnShortlist);
        bottomBar.add(btnScheduleInterview);
        bottomBar.add(btnHire);
        bottomBar.add(btnReject);
        bottomBar.add(btnExportCSV);
        bottomBar.add(btnExportPDF);
        add(bottomBar, BorderLayout.SOUTH);

        loadJobs();
    }

    public void loadJobs() {
        cmbJobs.removeAllItems();
        List<Job> jobs = jobService.getJobs(null, null, null, null);
        for (Job j : jobs) {
            cmbJobs.addItem(j);
        }
        if (cmbJobs.getItemCount() > 0) {
            cmbJobs.setSelectedIndex(0);
        }
    }

    public void calculateAndRank() {
        Job job = (Job) cmbJobs.getSelectedItem();
        if (job == null) {
            tableModel.setRowCount(0);
            return;
        }

        String skillKw = txtSkillFilter.getText().trim();
        Double minExp = ((Number) spnMinExp.getValue()).doubleValue();
        String edu = (String) cmbEducation.getSelectedItem();

        Double minScore = null;
        int scoreIdx = cmbMinScore.getSelectedIndex();
        if (scoreIdx == 1) minScore = 80.0;
        else if (scoreIdx == 2) minScore = 60.0;
        else if (scoreIdx == 3) minScore = 40.0;

        currentResults = rankingService.getRankedCandidates(job.getJobId(), skillKw, minExp, edu, minScore);

        tableModel.setRowCount(0);
        int rank = 1;
        for (MatchResult mr : currentResults) {
            MatchDetail d = mr.getDetails();
            String matchedReq = d != null ? MatchDetail.toCommaString(d.getMatchedRequiredSkills()) : "N/A";
            String missingReq = d != null ? MatchDetail.toCommaString(d.getMissingRequiredSkills()) : "None";

            tableModel.addRow(new Object[]{
                    mr.getCandidateId(),
                    "#" + (rank++),
                    mr.getCandidateName(),
                    mr.getOverallScore(),
                    DateUtil.formatScore(mr.getRequiredSkillScore()),
                    DateUtil.formatScore(mr.getPreferredSkillScore()),
                    DateUtil.formatScore(mr.getExperienceScore()),
                    DateUtil.formatScore(mr.getEducationScore()),
                    matchedReq,
                    missingReq
            });
        }
    }

    private MatchResult getSelectedMatch() {
        int row = table.getSelectedRow();
        if (row < 0 || currentResults == null || row >= currentResults.size()) {
            JOptionPane.showMessageDialog(this, "Please select a candidate row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentResults.get(row);
    }

    private void showSelectedMatchDetails() {
        MatchResult mr = getSelectedMatch();
        if (mr != null) {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new MatchDetailDialog(topFrame, mr, rankingService, this::calculateAndRank).setVisible(true);
        }
    }

    private void showSelectedCandidateDossier() {
        MatchResult mr = getSelectedMatch();
        if (mr != null) {
            CandidateProfile cp = candidateService.getCandidateById(mr.getCandidateId());
            if (cp != null) {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                new CandidateDetailDialog(topFrame, cp, candidateService).setVisible(true);
            }
        }
    }

    private void updateStatusForSelected(String status, String notes) {
        MatchResult mr = getSelectedMatch();
        if (mr != null) {
            boolean ok = rankingService.updateApplicationStatus(mr.getJobId(), mr.getCandidateId(), status, notes);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Candidate recruitment status updated to: " + status, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                calculateAndRank();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportRankingsCSV() {
        Job j = (Job) cmbJobs.getSelectedItem();
        String title = j != null ? j.getTitle().replaceAll("[^a-zA-Z0-9]", "_") : "Job";
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("TalentIQ_Rankings_" + title + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ExportUtil.exportTableToCSV(table, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Rankings exported to CSV successfully:\n" + fc.getSelectedFile().getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportSelectedMatchPDF() {
        MatchResult mr = getSelectedMatch();
        if (mr != null) {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Candidate_Match_Report_" + mr.getCandidateId() + ".pdf"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ExportUtil.exportMatchReportToPDF(mr, fc.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "PDF Report generated and saved successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}

