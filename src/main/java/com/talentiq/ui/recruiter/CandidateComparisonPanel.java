package com.talentiq.ui.recruiter;

import com.talentiq.model.*;
import com.talentiq.service.CandidateService;
import com.talentiq.service.JobService;
import com.talentiq.service.MatchingService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.DateUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateComparisonPanel extends JPanel {

    private final JobService jobService;
    private final MatchingService matchingService;
    private final CandidateService candidateService;

    private final JComboBox<Job> cmbJobs = new JComboBox<>();
    private final DefaultListModel<CandidateProfile> candidateListModel = new DefaultListModel<>();
    private final JList<CandidateProfile> listCandidates = new JList<>(candidateListModel);

    private final JPanel comparisonContainer = new JPanel();
    private final JPanel chartContainer = new JPanel(new BorderLayout());

    public CandidateComparisonPanel(JobService jobService, MatchingService matchingService, CandidateService candidateService) {
        this.jobService = jobService;
        this.matchingService = matchingService;
        this.candidateService = candidateService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setOpaque(false);

        JLabel lblTitle = new JLabel("Side-by-Side Candidate Comparison (Up to 4 Candidates)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topBar.add(lblTitle, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controls.setOpaque(false);

        controls.add(new JLabel("Target Job Opening:"));
        cmbJobs.setPreferredSize(new Dimension(300, 32));
        controls.add(cmbJobs);

        JButton btnCompare = new ModernButton("Compare Selected Candidates", ModernButton.ButtonType.PRIMARY);
        btnCompare.addActionListener(e -> performComparison());
        controls.add(btnCompare);

        topBar.add(controls, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // Main Split Pane: Left Candidate Picker, Right Comparison View
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(260);
        splitPane.setBorder(null);

        // Left Panel (Multi-select candidates)
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Select 2 to 4 Candidates (Ctrl+Click)"));

        listCandidates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listCandidates.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftPanel.add(new JScrollPane(listCandidates), BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // Right Panel (Comparison Matrix + Chart)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        comparisonContainer.setLayout(new GridLayout(1, 4, 12, 12));
        comparisonContainer.setBorder(BorderFactory.createTitledBorder("Candidate Evaluation Matrix"));

        chartContainer.setPreferredSize(new Dimension(650, 260));
        chartContainer.setBorder(BorderFactory.createTitledBorder("Comparative Score Breakdown (JFreeChart)"));

        rightPanel.add(comparisonContainer);
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(chartContainer);

        JScrollPane scrollRight = new JScrollPane(rightPanel);
        scrollRight.setBorder(null);
        splitPane.setRightComponent(scrollRight);

        add(splitPane, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        cmbJobs.removeAllItems();
        List<Job> jobs = jobService.getJobs(null, null, null, null);
        for (Job j : jobs) {
            cmbJobs.addItem(j);
        }

        candidateListModel.clear();
        List<CandidateProfile> candidates = candidateService.getAllCandidates(null, null, null);
        for (CandidateProfile c : candidates) {
            candidateListModel.addElement(c);
        }

        if (candidateListModel.size() >= 2) {
            listCandidates.setSelectionInterval(0, Math.min(3, candidateListModel.size() - 1));
            performComparison();
        }
    }

    private void performComparison() {
        List<CandidateProfile> selected = listCandidates.getSelectedValuesList();
        if (selected == null || selected.size() < 2) {
            JOptionPane.showMessageDialog(this, "Please select at least 2 candidates to compare (up to 4). Use Ctrl+Click to multi-select.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.size() > 4) {
            JOptionPane.showMessageDialog(this, "You can compare at most 4 candidates at a time. Truncating to first 4.", "Information", JOptionPane.INFORMATION_MESSAGE);
            selected = selected.subList(0, 4);
        }

        Job job = (Job) cmbJobs.getSelectedItem();
        if (job == null) return;

        comparisonContainer.removeAll();
        comparisonContainer.setLayout(new GridLayout(1, selected.size(), 12, 12));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CandidateProfile cp : selected) {
            MatchResult mr = matchingService.calculateMatch(cp, job);
            comparisonContainer.add(createCandidateCard(cp, mr));

            // Chart data
            String candName = cp.getFullName();
            dataset.addValue(mr.getOverallScore(), candName, "Overall Score");
            dataset.addValue(mr.getRequiredSkillScore(), candName, "Required Skills");
            dataset.addValue(mr.getPreferredSkillScore(), candName, "Preferred Skills");
            dataset.addValue(mr.getExperienceScore(), candName, "Experience");
            dataset.addValue(mr.getEducationScore(), candName, "Education");
        }

        comparisonContainer.revalidate();
        comparisonContainer.repaint();

        // Render Bar Chart
        JFreeChart chart = ChartFactory.createBarChart(
                null, "Criteria", "Score (%)",
                dataset, PlotOrientation.VERTICAL, true, true, false
        );

        chartContainer.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(650, 240));
        chartContainer.add(cp, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private JPanel createCandidateCard(CandidateProfile cp, MatchResult mr) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150, 50), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // Name
        JLabel lblName = new JLabel(cp.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblName.setForeground(ThemeManager.COLOR_PRIMARY);

        JLabel lblTitle = new JLabel(cp.getCurrentJobTitle() != null ? cp.getCurrentJobTitle() : "Candidate");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        // Match Score Badge
        JLabel lblScore = new JLabel(String.format("%.1f%%", mr.getOverallScore()));
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblScore.setForeground(mr.getOverallScore() >= 80 ? ThemeManager.COLOR_SUCCESS : ThemeManager.COLOR_PRIMARY);

        card.add(lblName);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(new JLabel("Match Score:"));
        card.add(lblScore);
        card.add(Box.createVerticalStrut(10));

        // Required skills
        MatchDetail d = mr.getDetails();
        card.add(createBoldLabel("Required Skills (60%):"));
        JLabel lblReqMatched = new JLabel("<html><b>Matched:</b> <font color='#10B981'>" + (d != null ? MatchDetail.toCommaString(d.getMatchedRequiredSkills()) : "None") + "</font></html>");
        JLabel lblReqMissed = new JLabel("<html><b>Missing:</b> <font color='#EF4444'>" + (d != null ? MatchDetail.toCommaString(d.getMissingRequiredSkills()) : "None") + "</font></html>");
        card.add(lblReqMatched);
        card.add(lblReqMissed);
        card.add(Box.createVerticalStrut(8));

        // Preferred skills
        card.add(createBoldLabel("Preferred Skills (20%):"));
        JLabel lblPrefMatched = new JLabel("<html><b>Matched:</b> " + (d != null ? MatchDetail.toCommaString(d.getMatchedPreferredSkills()) : "None") + "</html>");
        JLabel lblPrefMissed = new JLabel("<html><b>Missing:</b> <font color='#F59E0B'>" + (d != null ? MatchDetail.toCommaString(d.getMissingPreferredSkills()) : "None") + "</font></html>");
        card.add(lblPrefMatched);
        card.add(lblPrefMissed);
        card.add(Box.createVerticalStrut(8));

        // Experience
        card.add(createBoldLabel("Experience:"));
        card.add(new JLabel(cp.getTotalExperienceYears() + " Years (" + (cp.getCurrentCompany() != null ? cp.getCurrentCompany() : "N/A") + ")"));
        card.add(Box.createVerticalStrut(8));

        // Education
        card.add(createBoldLabel("Education:"));
        card.add(new JLabel(cp.getHighestEducationLevel()));
        card.add(Box.createVerticalStrut(8));

        // Certifications & Projects counts
        card.add(createBoldLabel("Credentials:"));
        int certCount = cp.getCertifications() != null ? cp.getCertifications().size() : 0;
        int projCount = cp.getProjects() != null ? cp.getProjects().size() : 0;
        card.add(new JLabel("Certifications: " + certCount));
        card.add(new JLabel("Projects: " + projCount));

        return card;
    }

    private JLabel createBoldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }
}

