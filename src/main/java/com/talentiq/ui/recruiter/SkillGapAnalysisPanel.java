package com.talentiq.ui.recruiter;

import com.talentiq.model.*;
import com.talentiq.service.CandidateService;
import com.talentiq.service.JobService;
import com.talentiq.service.MatchingService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.theme.ThemeManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SkillGapAnalysisPanel extends JPanel {

    private final JobService jobService;
    private final CandidateService candidateService;
    private final MatchingService matchingService;

    private final JComboBox<CandidateProfile> cmbCandidates = new JComboBox<>();
    private final JComboBox<Job> cmbJobs = new JComboBox<>();

    // Metrics
    private final JLabel lblCompletion = new JLabel("0.0%");
    private final JLabel lblReqRatio = new JLabel("0 / 0");
    private final JLabel lblPrefRatio = new JLabel("0 / 0");
    private final JLabel lblExpStatus = new JLabel("N/A");

    // Chart Panel
    private final JPanel chartContainer = new JPanel(new BorderLayout());

    // Tables
    private final DefaultTableModel existingSkillsModel = new DefaultTableModel(new String[]{"Skill", "Category", "Proficiency"}, 0);
    private final ModernTable existingSkillsTable = new ModernTable(existingSkillsModel);

    private final DefaultListModel<String> missingReqModel = new DefaultListModel<>();
    private final JList<String> missingReqList = new JList<>(missingReqModel);

    private final DefaultListModel<String> missingPrefModel = new DefaultListModel<>();
    private final JList<String> missingPrefList = new JList<>(missingPrefModel);

    private final JTextArea txtActionPlan = new JTextArea(3, 30);

    public SkillGapAnalysisPanel(JobService jobService, CandidateService candidateService, MatchingService matchingService) {
        this.jobService = jobService;
        this.candidateService = candidateService;
        this.matchingService = matchingService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Skill Gap Analysis & Candidate Readiness Evaluation");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(lblTitle, BorderLayout.NORTH);

        JPanel selectBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        selectBar.setOpaque(false);

        selectBar.add(new JLabel("Select Candidate:"));
        cmbCandidates.setPreferredSize(new Dimension(240, 32));
        selectBar.add(cmbCandidates);

        selectBar.add(new JLabel("Target Job Opening:"));
        cmbJobs.setPreferredSize(new Dimension(260, 32));
        selectBar.add(cmbJobs);

        JButton btnAnalyze = new ModernButton("Analyze Skill Gap", ModernButton.ButtonType.PRIMARY);
        btnAnalyze.addActionListener(e -> runAnalysis());
        selectBar.add(btnAnalyze);

        topBar.add(selectBar, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // Center Content: Split into Left Metrics/Charts and Right Skill Details
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 14, 14));

        // Left Panel (KPI Card + Donut Chart + Action Plan)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JPanel kpiCard = new JPanel(new GridLayout(2, 2, 10, 10));
        kpiCard.setBorder(BorderFactory.createTitledBorder("Readiness Summary"));

        lblCompletion.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCompletion.setForeground(ThemeManager.COLOR_PRIMARY);

        kpiCard.add(createMetric("Skill Completion Rate:", lblCompletion));
        kpiCard.add(createMetric("Required Skills Matched:", lblReqRatio));
        kpiCard.add(createMetric("Preferred Skills Matched:", lblPrefRatio));
        kpiCard.add(createMetric("Experience Qualification:", lblExpStatus));
        leftPanel.add(kpiCard);
        leftPanel.add(Box.createVerticalStrut(10));

        chartContainer.setBorder(BorderFactory.createTitledBorder("Skill Gap Distribution (JFreeChart)"));
        chartContainer.setPreferredSize(new Dimension(360, 240));
        leftPanel.add(chartContainer);
        leftPanel.add(Box.createVerticalStrut(10));

        JPanel planPanel = new JPanel(new BorderLayout());
        planPanel.setBorder(BorderFactory.createTitledBorder("Development & Training Recommendations"));
        txtActionPlan.setWrapStyleWord(true);
        txtActionPlan.setLineWrap(true);
        txtActionPlan.setEditable(false);
        txtActionPlan.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        planPanel.add(new JScrollPane(txtActionPlan), BorderLayout.CENTER);
        leftPanel.add(planPanel);

        centerPanel.add(leftPanel);

        // Right Panel (Existing Skills Table + Missing Lists)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel existingPanel = new JPanel(new BorderLayout());
        existingPanel.setBorder(BorderFactory.createTitledBorder("Candidate Existing Technical Skills"));
        existingPanel.add(new JScrollPane(existingSkillsTable), BorderLayout.CENTER);
        rightPanel.add(existingPanel);

        JPanel missingPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel reqMissPanel = new JPanel(new BorderLayout());
        reqMissPanel.setBorder(BorderFactory.createTitledBorder("Critical Missing (Required Skills)"));
        missingReqList.setFont(new Font("Segoe UI", Font.BOLD, 12));
        missingReqList.setForeground(ThemeManager.COLOR_DANGER);
        reqMissPanel.add(new JScrollPane(missingReqList), BorderLayout.CENTER);

        JPanel prefMissPanel = new JPanel(new BorderLayout());
        prefMissPanel.setBorder(BorderFactory.createTitledBorder("Secondary Missing (Preferred Skills)"));
        missingPrefList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        missingPrefList.setForeground(ThemeManager.COLOR_WARNING);
        prefMissPanel.add(new JScrollPane(missingPrefList), BorderLayout.CENTER);

        missingPanel.add(reqMissPanel);
        missingPanel.add(prefMissPanel);
        rightPanel.add(missingPanel);

        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);

        loadDropdowns();
    }

    public void loadDropdowns() {
        cmbCandidates.removeAllItems();
        List<CandidateProfile> cList = candidateService.getAllCandidates(null, null, null);
        for (CandidateProfile c : cList) {
            cmbCandidates.addItem(c);
        }

        cmbJobs.removeAllItems();
        List<Job> jList = jobService.getJobs(null, null, null, null);
        for (Job j : jList) {
            cmbJobs.addItem(j);
        }

        if (cmbCandidates.getItemCount() > 0 && cmbJobs.getItemCount() > 0) {
            runAnalysis();
        }
    }

    private void runAnalysis() {
        CandidateProfile candidate = (CandidateProfile) cmbCandidates.getSelectedItem();
        Job job = (Job) cmbJobs.getSelectedItem();
        if (candidate == null || job == null) return;

        MatchResult mr = matchingService.calculateMatch(candidate, job);
        MatchDetail d = mr.getDetails();

        // Populate Existing Skills
        existingSkillsModel.setRowCount(0);
        if (candidate.getSkills() != null) {
            for (CandidateSkill s : candidate.getSkills()) {
                existingSkillsModel.addRow(new Object[]{s.getSkillName(), s.getCategoryName(), s.getProficiencyLevel()});
            }
        }

        // Missing Required
        missingReqModel.clear();
        int missingReqCount = 0;
        int matchedReqCount = 0;
        if (d != null && d.getMissingRequiredSkills() != null) {
            for (String s : d.getMissingRequiredSkills()) {
                if (!"None".equalsIgnoreCase(s)) {
                    missingReqModel.addElement("✕ " + s);
                    missingReqCount++;
                }
            }
            if (d.getMatchedRequiredSkills() != null) {
                matchedReqCount = d.getMatchedRequiredSkills().size();
            }
        }

        // Missing Preferred
        missingPrefModel.clear();
        int missingPrefCount = 0;
        int matchedPrefCount = 0;
        if (d != null && d.getMissingPreferredSkills() != null) {
            for (String s : d.getMissingPreferredSkills()) {
                if (!"None".equalsIgnoreCase(s)) {
                    missingPrefModel.addElement("△ " + s);
                    missingPrefCount++;
                }
            }
            if (d.getMatchedPreferredSkills() != null) {
                matchedPrefCount = d.getMatchedPreferredSkills().size();
            }
        }

        int totalReq = matchedReqCount + missingReqCount;
        int totalPref = matchedPrefCount + missingPrefCount;

        lblCompletion.setText(String.format("%.1f%%", mr.getRequiredSkillScore()));
        lblReqRatio.setText(matchedReqCount + " / " + totalReq + " (" + Math.round(mr.getRequiredSkillScore()) + "%)");
        lblPrefRatio.setText(matchedPrefCount + " / " + totalPref + " (" + Math.round(mr.getPreferredSkillScore()) + "%)");
        lblExpStatus.setText(candidate.getTotalExperienceYears() + " yrs vs " + job.getMinExperienceYears() + " yrs req");

        // Action plan text
        if (d != null && d.getRecommendations() != null) {
            txtActionPlan.setText(d.getRecommendations());
        }

        // Pie Chart
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Acquired Required (" + matchedReqCount + ")", matchedReqCount);
        if (missingReqCount > 0) dataset.setValue("Missing Required (" + missingReqCount + ")", missingReqCount);
        if (matchedPrefCount > 0) dataset.setValue("Acquired Preferred (" + matchedPrefCount + ")", matchedPrefCount);
        if (missingPrefCount > 0) dataset.setValue("Missing Preferred (" + missingPrefCount + ")", missingPrefCount);

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setSectionPaint("Acquired Required (" + matchedReqCount + ")", ThemeManager.COLOR_SUCCESS);
        if (missingReqCount > 0) plot.setSectionPaint("Missing Required (" + missingReqCount + ")", ThemeManager.COLOR_DANGER);
        if (matchedPrefCount > 0) plot.setSectionPaint("Acquired Preferred (" + matchedPrefCount + ")", ThemeManager.COLOR_PRIMARY);
        if (missingPrefCount > 0) plot.setSectionPaint("Missing Preferred (" + missingPrefCount + ")", ThemeManager.COLOR_WARNING);

        chartContainer.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        chartContainer.add(cp, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private JPanel createMetric(String label, JLabel valLabel) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(UIManager.getColor("Label.disabledForeground"));
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(l, BorderLayout.NORTH);
        p.add(valLabel, BorderLayout.CENTER);
        return p;
    }
}

