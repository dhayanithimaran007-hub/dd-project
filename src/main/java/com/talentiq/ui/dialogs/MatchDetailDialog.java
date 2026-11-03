package com.talentiq.ui.dialogs;

import com.talentiq.model.MatchDetail;
import com.talentiq.model.MatchResult;
import com.talentiq.service.RankingService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.DateUtil;
import com.talentiq.util.ExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class MatchDetailDialog extends JDialog {

    public MatchDetailDialog(JFrame parent, MatchResult match, RankingService rankingService, Runnable onStatusChange) {
        super(parent, "Match Score Analysis & Explanation", true);
        setSize(650, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel(match.getCandidateName());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeManager.COLOR_PRIMARY);

        JLabel lblSub = new JLabel("Target Position: " + match.getJobTitle());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(lblSub);

        // Overall Score Pill
        JLabel lblScore = new JLabel(DateUtil.formatScore(match.getOverallScore()));
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblScore.setOpaque(true);
        lblScore.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        lblScore.setBackground(match.getOverallScore() >= 80 ? ThemeManager.COLOR_SUCCESS : (match.getOverallScore() >= 60 ? ThemeManager.COLOR_PRIMARY : ThemeManager.COLOR_WARNING));
        lblScore.setForeground(Color.WHITE);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(lblScore, BorderLayout.EAST);
        root.add(header);
        root.add(Box.createVerticalStrut(18));

        // Formula Callout Card
        JPanel formulaCard = new JPanel(new GridLayout(2, 2, 10, 10));
        formulaCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150, 40), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        formulaCard.add(createMetricLabel("Required Skills (60% weight)", DateUtil.formatScore(match.getRequiredSkillScore())));
        formulaCard.add(createMetricLabel("Preferred Skills (20% weight)", DateUtil.formatScore(match.getPreferredSkillScore())));
        formulaCard.add(createMetricLabel("Experience Match (10% weight)", DateUtil.formatScore(match.getExperienceScore())));
        formulaCard.add(createMetricLabel("Education Match (10% weight)", DateUtil.formatScore(match.getEducationScore())));
        root.add(formulaCard);
        root.add(Box.createVerticalStrut(16));

        // Details Breakdown
        MatchDetail d = match.getDetails();
        if (d != null) {
            root.add(createSectionHeader("Required Skills Compatibility:"));
            root.add(createDetailRow("Matched Skills: ", MatchDetail.toCommaString(d.getMatchedRequiredSkills()), ThemeManager.COLOR_SUCCESS));
            root.add(createDetailRow("Missing Skills: ", MatchDetail.toCommaString(d.getMissingRequiredSkills()), ThemeManager.COLOR_DANGER));
            root.add(Box.createVerticalStrut(12));

            root.add(createSectionHeader("Preferred Skills Compatibility:"));
            root.add(createDetailRow("Matched Skills: ", MatchDetail.toCommaString(d.getMatchedPreferredSkills()), ThemeManager.COLOR_SUCCESS));
            root.add(createDetailRow("Missing Skills: ", MatchDetail.toCommaString(d.getMissingPreferredSkills()), ThemeManager.COLOR_WARNING));
            root.add(Box.createVerticalStrut(12));

            root.add(createSectionHeader("Qualifications Compatibility:"));
            root.add(createDetailRow("Experience: ", d.getExperienceCompatibility(), null));
            root.add(createDetailRow("Education: ", d.getEducationCompatibility(), null));
            root.add(Box.createVerticalStrut(12));

            if (d.getRecommendations() != null && !d.getRecommendations().isBlank()) {
                root.add(createSectionHeader("Recruiter Recommendations:"));
                JTextArea txtRec = new JTextArea(d.getRecommendations());
                txtRec.setWrapStyleWord(true);
                txtRec.setLineWrap(true);
                txtRec.setEditable(false);
                txtRec.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                txtRec.setBackground(new Color(150, 150, 150, 20));
                txtRec.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                root.add(txtRec);
            }
        }

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Action Buttons Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnExport = new ModernButton("Export PDF Report", ModernButton.ButtonType.SECONDARY);
        JButton btnShortlist = new ModernButton("Shortlist Candidate", ModernButton.ButtonType.SUCCESS);
        JButton btnClose = new ModernButton("Close", ModernButton.ButtonType.PRIMARY);

        btnExport.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Candidate_Match_Report_" + match.getCandidateId() + ".pdf"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ExportUtil.exportMatchReportToPDF(match, fc.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Match evaluation report exported successfully to:\n" + fc.getSelectedFile().getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to export PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnShortlist.addActionListener(e -> {
            boolean ok = rankingService.updateApplicationStatus(match.getJobId(), match.getCandidateId(), "SHORTLISTED", "Shortlisted via Match Details dialog (Score: " + match.getOverallScore() + "%)");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Candidate successfully shortlisted!", "Shortlisted", JOptionPane.INFORMATION_MESSAGE);
                if (onStatusChange != null) onStatusChange.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Could not update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnClose.addActionListener(e -> dispose());

        footer.add(btnExport);
        footer.add(btnShortlist);
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createMetricLabel(String title, String val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lTitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel lVal = new JLabel(val);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lVal.setForeground(ThemeManager.COLOR_PRIMARY);

        p.add(lTitle, BorderLayout.NORTH);
        p.add(lVal, BorderLayout.CENTER);
        return p;
    }

    private JLabel createSectionHeader(String title) {
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(UIManager.getColor("Label.foreground"));
        l.setBorder(new EmptyBorder(6, 0, 4, 0));
        return l;
    }

    private JPanel createDetailRow(String label, String value, Color highlightColor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setOpaque(false);
        JLabel l1 = new JLabel(label);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel l2 = new JLabel(value);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (highlightColor != null) {
            l2.setForeground(highlightColor);
            l2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        }
        p.add(l1);
        p.add(l2);
        return p;
    }
}

