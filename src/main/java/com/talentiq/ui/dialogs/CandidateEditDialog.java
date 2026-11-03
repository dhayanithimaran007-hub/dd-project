package com.talentiq.ui.dialogs;

import com.talentiq.model.CandidateProfile;
import com.talentiq.service.AuthService;
import com.talentiq.service.CandidateService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class CandidateEditDialog extends JDialog {

    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JTextField txtFullName = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtHeadline = new JTextField(20);
    private final JSpinner spnExperience = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 40.0, 0.5));
    private final JComboBox<String> cmbEducation = new JComboBox<>(new String[]{"Doctorate", "Master", "Bachelor", "Associate", "High School"});
    private final JTextField txtCompany = new JTextField(20);
    private final JTextField txtLocation = new JTextField(20);
    private final JTextField txtSalary = new JTextField("90000", 12);
    private final JTextArea txtSummary = new JTextArea(3, 20);

    private boolean saved = false;

    public CandidateEditDialog(JFrame parent, CandidateProfile profile, boolean isCreate,
                               CandidateService candidateService, AuthService authService) {
        super(parent, isCreate ? "Add New Candidate" : "Edit Candidate Profile", true);
        setSize(550, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        if (isCreate) {
            addFormRow(formPanel, gbc, row++, "Username:", txtUsername);
            addFormRow(formPanel, gbc, row++, "Initial Password:", txtPassword);
        }

        addFormRow(formPanel, gbc, row++, "Full Name:", txtFullName);
        addFormRow(formPanel, gbc, row++, "Email Address:", txtEmail);
        addFormRow(formPanel, gbc, row++, "Phone:", txtPhone);
        addFormRow(formPanel, gbc, row++, "Headline:", txtHeadline);
        addFormRow(formPanel, gbc, row++, "Total Experience (Years):", spnExperience);
        addFormRow(formPanel, gbc, row++, "Highest Education:", cmbEducation);
        addFormRow(formPanel, gbc, row++, "Current Company:", txtCompany);
        addFormRow(formPanel, gbc, row++, "Location:", txtLocation);
        addFormRow(formPanel, gbc, row++, "Expected Salary (USD):", txtSalary);

        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Summary:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(new JScrollPane(txtSummary), gbc);

        // Prepopulate if editing
        if (!isCreate && profile != null) {
            txtFullName.setText(profile.getFullName());
            txtEmail.setText(profile.getEmail());
            txtPhone.setText(profile.getPhone());
            txtHeadline.setText(profile.getHeadline());
            spnExperience.setValue(profile.getTotalExperienceYears());
            cmbEducation.setSelectedItem(profile.getHighestEducationLevel());
            txtCompany.setText(profile.getCurrentCompany());
            txtLocation.setText(profile.getLocation());
            if (profile.getExpectedSalary() != null) txtSalary.setText(profile.getExpectedSalary().toString());
            txtSummary.setText(profile.getSummary());
        }

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        JButton btnSave = new ModernButton(isCreate ? "Create Candidate" : "Update Candidate", ModernButton.ButtonType.PRIMARY);
        JButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);

        btnSave.addActionListener(e -> {
            String name = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();

            if (!ValidationUtil.isNotEmpty(name)) {
                JOptionPane.showMessageDialog(this, "Full name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "A valid email address is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isCreate) {
                    String username = txtUsername.getText().trim();
                    String pwd = new String(txtPassword.getPassword());
                    if (!ValidationUtil.isNotEmpty(username)) {
                        JOptionPane.showMessageDialog(this, "Username is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!ValidationUtil.isValidPassword(pwd)) {
                        JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    authService.registerCandidate(username, pwd, email, name, txtPhone.getText().trim(), txtHeadline.getText().trim());
                } else {
                    profile.setFullName(name);
                    profile.setEmail(email);
                    profile.setPhone(txtPhone.getText().trim());
                    profile.setHeadline(txtHeadline.getText().trim());
                    profile.setTotalExperienceYears(((Number) spnExperience.getValue()).doubleValue());
                    profile.setHighestEducationLevel((String) cmbEducation.getSelectedItem());
                    profile.setCurrentCompany(txtCompany.getText().trim());
                    profile.setLocation(txtLocation.getText().trim());
                    try {
                        profile.setExpectedSalary(new BigDecimal(txtSalary.getText().trim()));
                    } catch (Exception ignored) {}
                    profile.setSummary(txtSummary.getText().trim());
                    candidateService.updateCandidateProfile(profile);
                }
                saved = true;
                JOptionPane.showMessageDialog(this, "Candidate successfully saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving candidate: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dispose());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0;
        p.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    public boolean isSaved() {
        return saved;
    }
}

