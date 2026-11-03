package com.talentiq.ui.dialogs;

import com.talentiq.model.Skill;
import com.talentiq.service.ResumeService;
import com.talentiq.service.SkillService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.ResumeParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResumeCorrectionDialog extends JDialog {

    private final JTextField txtName = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtHeadline = new JTextField(20);
    private final JComboBox<String> cmbEducation = new JComboBox<>(new String[]{"Doctorate", "Master", "Bachelor", "Associate", "High School"});
    private final JSpinner spnExperience = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 50.0, 0.5));
    private final JTextArea txtSummary = new JTextArea(4, 25);
    private final DefaultListModel<Skill> skillsListModel = new DefaultListModel<>();
    private final JList<Skill> listSkills = new JList<>(skillsListModel);
    private final JTextArea txtRawText = new JTextArea(8, 30);

    private boolean applied = false;

    public ResumeCorrectionDialog(JFrame parent, int candidateId, ResumeParser.ParsedResumeResult parsed,
                                  ResumeService resumeService, SkillService skillService) {
        super(parent, "Review & Correct Extracted Resume Information", true);
        setSize(750, 720);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Prepopulate parsed data
        txtName.setText(parsed.getCandidateName());
        txtEmail.setText(parsed.getEmail());
        txtPhone.setText(parsed.getPhone());
        txtHeadline.setText(parsed.getHeadline() != null && !parsed.getHeadline().isBlank() ? parsed.getHeadline() : "Software Professional");
        cmbEducation.setSelectedItem(parsed.getHighestEducationLevel());
        spnExperience.setValue(parsed.getTotalExperienceYears());
        txtSummary.setText(parsed.getSummary());
        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);

        if (parsed.getMatchedSkills() != null) {
            for (Skill s : parsed.getMatchedSkills()) {
                skillsListModel.addElement(s);
            }
        }

        txtRawText.setText(parsed.getRawText());
        txtRawText.setEditable(false);
        txtRawText.setLineWrap(true);
        txtRawText.setWrapStyleWord(true);

        // Header info banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(37, 99, 235, 25));
        banner.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblNotice = new JLabel("<html><b>Conventional Text Extraction Completed.</b><br/>" +
                "Review the regular-expression matched fields below. You can edit any field before saving to candidate profile.</html>");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        banner.add(lblNotice, BorderLayout.CENTER);
        add(banner, BorderLayout.NORTH);

        // Main Tabbed or Split form
        JTabbedPane tabs = new JTabbedPane();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormField(formPanel, gbc, row++, "Full Name:", txtName);
        addFormField(formPanel, gbc, row++, "Email Address:", txtEmail);
        addFormField(formPanel, gbc, row++, "Phone Number:", txtPhone);
        addFormField(formPanel, gbc, row++, "Professional Headline:", txtHeadline);
        addFormField(formPanel, gbc, row++, "Highest Education:", cmbEducation);
        addFormField(formPanel, gbc, row++, "Total Experience (Years):", spnExperience);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Profile Summary:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(new JScrollPane(txtSummary), gbc);

        tabs.addTab("Basic Information", formPanel);

        // Skills panel
        JPanel skillsPanel = new JPanel(new BorderLayout(10, 10));
        skillsPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel skillsHeader = new JPanel(new BorderLayout());
        skillsHeader.add(new JLabel("Extracted Skills from Dictionary (" + skillsListModel.size() + " detected):"), BorderLayout.WEST);

        JPanel skillBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnAddSkill = new ModernButton("+ Add Skill", ModernButton.ButtonType.SECONDARY);
        JButton btnRemoveSkill = new ModernButton("- Remove", ModernButton.ButtonType.DANGER);

        btnAddSkill.addActionListener(e -> {
            List<Skill> all = skillService.getAllSkills();
            Skill selected = (Skill) JOptionPane.showInputDialog(this,
                    "Select skill to add to candidate profile:",
                    "Add Skill", JOptionPane.PLAIN_MESSAGE, null, all.toArray(), all.isEmpty() ? null : all.get(0));
            if (selected != null && !skillsListModel.contains(selected)) {
                skillsListModel.addElement(selected);
            }
        });

        btnRemoveSkill.addActionListener(e -> {
            int idx = listSkills.getSelectedIndex();
            if (idx >= 0) {
                skillsListModel.remove(idx);
            }
        });

        skillBtnBar.add(btnAddSkill);
        skillBtnBar.add(btnRemoveSkill);
        skillsHeader.add(skillBtnBar, BorderLayout.EAST);

        skillsPanel.add(skillsHeader, BorderLayout.NORTH);
        skillsPanel.add(new JScrollPane(listSkills), BorderLayout.CENTER);

        tabs.addTab("Extracted Skills (" + skillsListModel.size() + ")", skillsPanel);

        // Raw Text Tab
        JPanel rawPanel = new JPanel(new BorderLayout());
        rawPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        rawPanel.add(new JLabel("Full Plain Text Extracted by Apache PDFBox / Apache POI:"), BorderLayout.NORTH);
        rawPanel.add(new JScrollPane(txtRawText), BorderLayout.CENTER);
        tabs.addTab("Raw Extracted Text", rawPanel);

        add(tabs, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        JButton btnApply = new ModernButton("Apply to Profile", ModernButton.ButtonType.SUCCESS);
        JButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);

        btnApply.addActionListener(e -> {
            parsed.setCandidateName(txtName.getText().trim());
            parsed.setEmail(txtEmail.getText().trim());
            parsed.setPhone(txtPhone.getText().trim());
            parsed.setHeadline(txtHeadline.getText().trim());
            parsed.setHighestEducationLevel((String) cmbEducation.getSelectedItem());
            parsed.setTotalExperienceYears(((Number) spnExperience.getValue()).doubleValue());
            parsed.setSummary(txtSummary.getText().trim());

            List<Skill> updatedSkills = new ArrayList<>();
            for (int i = 0; i < skillsListModel.size(); i++) {
                updatedSkills.add(skillsListModel.get(i));
            }
            parsed.setMatchedSkills(updatedSkills);

            boolean success = resumeService.applyParsedInfoToCandidate(candidateId, parsed);
            if (success) {
                applied = true;
                JOptionPane.showMessageDialog(this, "Profile updated with extracted and verified resume information!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to apply changes to database profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dispose());

        footer.add(btnCancel);
        footer.add(btnApply);
        add(footer, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    public boolean isApplied() {
        return applied;
    }
}

