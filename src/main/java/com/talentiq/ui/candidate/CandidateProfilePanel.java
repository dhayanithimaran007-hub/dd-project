package com.talentiq.ui.candidate;

import com.talentiq.model.*;
import com.talentiq.service.CandidateService;
import com.talentiq.service.SkillService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.DateUtil;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CandidateProfilePanel extends JPanel {

    private final CandidateService candidateService;
    private final SkillService skillService;

    private int activeCandidateId;
    private CandidateProfile currentProfile;

    // Profile form fields
    private final JTextField txtFullName = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtPhone = new JTextField(20);
    private final JTextField txtHeadline = new JTextField(20);
    private final JSpinner spnExperience = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 40.0, 0.5));
    private final JComboBox<String> cmbEducation = new JComboBox<>(new String[]{"Doctorate", "Master", "Bachelor", "Associate", "High School"});
    private final JTextField txtJobTitle = new JTextField(20);
    private final JTextField txtCompany = new JTextField(20);
    private final JTextField txtLocation = new JTextField(20);
    private final JTextField txtSalary = new JTextField("95000", 12);
    private final JTextField txtLinkedin = new JTextField(20);
    private final JTextField txtGithub = new JTextField(20);
    private final JTextArea txtSummary = new JTextArea(4, 20);

    // Sub-tables
    private final DefaultTableModel skillsModel = new DefaultTableModel(new String[]{"Skill ID", "Skill Name", "Category", "Proficiency", "Years of Exp"}, 0);
    private final ModernTable skillsTable = new ModernTable(skillsModel);

    private final DefaultTableModel expModel = new DefaultTableModel(new String[]{"ID", "Job Title", "Company", "Location", "Start Date", "End Date", "Technologies"}, 0);
    private final ModernTable expTable = new ModernTable(expModel);

    private final DefaultTableModel eduModel = new DefaultTableModel(new String[]{"ID", "Degree", "Level", "Institution", "Field", "Years", "Grade"}, 0);
    private final ModernTable eduTable = new ModernTable(eduModel);

    private final DefaultTableModel certModel = new DefaultTableModel(new String[]{"ID", "Certification", "Issuing Organization", "Issue Date", "Credential ID"}, 0);
    private final ModernTable certTable = new ModernTable(certModel);

    private final DefaultTableModel projModel = new DefaultTableModel(new String[]{"ID", "Project Title", "Role", "Technologies", "Description"}, 0);
    private final ModernTable projTable = new ModernTable(projModel);

    public CandidateProfilePanel(CandidateService candidateService, SkillService skillService) {
        this.candidateService = candidateService;
        this.skillService = skillService;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Comprehensive Candidate Profile & Skill Portfolio");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnSaveProfile = new ModernButton("Save Profile Changes", ModernButton.ButtonType.PRIMARY);
        btnSaveProfile.addActionListener(e -> saveProfile());
        header.add(btnSaveProfile, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        // 1. General Profile Details Tab
        JPanel profileForm = new JPanel(new GridBagLayout());
        profileForm.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(profileForm, gbc, row++, "Full Name:", txtFullName);
        addFormRow(profileForm, gbc, row++, "Email Address:", txtEmail);
        addFormRow(profileForm, gbc, row++, "Phone:", txtPhone);
        addFormRow(profileForm, gbc, row++, "Professional Headline:", txtHeadline);
        addFormRow(profileForm, gbc, row++, "Current Job Title:", txtJobTitle);
        addFormRow(profileForm, gbc, row++, "Current Company:", txtCompany);
        addFormRow(profileForm, gbc, row++, "Location:", txtLocation);
        addFormRow(profileForm, gbc, row++, "Total Experience (Years):", spnExperience);
        addFormRow(profileForm, gbc, row++, "Highest Education Level:", cmbEducation);
        addFormRow(profileForm, gbc, row++, "Expected Salary (USD):", txtSalary);
        addFormRow(profileForm, gbc, row++, "LinkedIn Profile URL:", txtLinkedin);
        addFormRow(profileForm, gbc, row++, "GitHub Profile URL:", txtGithub);

        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0;
        profileForm.add(new JLabel("Executive Summary:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        profileForm.add(new JScrollPane(txtSummary), gbc);

        tabs.addTab("Personal & Work Details", new JScrollPane(profileForm));

        // 2. Technical Skills Tab
        tabs.addTab("Technical Skills", createSubEntityTab(skillsTable, "+ Add Skill", "- Remove Skill", this::addSkillAction, this::deleteSkillAction));

        // 3. Work Experience Tab
        tabs.addTab("Work Experience", createSubEntityTab(expTable, "+ Add Experience", "- Remove Experience", this::addExpAction, this::deleteExpAction));

        // 4. Education Tab
        tabs.addTab("Education", createSubEntityTab(eduTable, "+ Add Degree", "- Remove Degree", this::addEduAction, this::deleteEduAction));

        // 5. Certifications Tab
        tabs.addTab("Certifications", createSubEntityTab(certTable, "+ Add Certification", "- Remove Certification", this::addCertAction, this::deleteCertAction));

        // 6. Projects Tab
        tabs.addTab("Projects", createSubEntityTab(projTable, "+ Add Project", "- Remove Project", this::addProjAction, this::deleteProjAction));

        add(tabs, BorderLayout.CENTER);

        loadCurrentCandidate();
    }

    public void loadCurrentCandidate() {
        CandidateProfile cp = SessionManager.getInstance().getCurrentCandidate();
        if (cp == null && SessionManager.getInstance().isAdmin()) {
            List<CandidateProfile> list = candidateService.getAllCandidates(null, null, null);
            if (!list.isEmpty()) cp = list.get(0);
        }

        if (cp != null) {
            loadCandidate(cp.getCandidateId());
        }
    }

    public void loadCandidate(int candidateId) {
        this.activeCandidateId = candidateId;
        this.currentProfile = candidateService.getCandidateById(candidateId);
        if (currentProfile == null) return;

        txtFullName.setText(currentProfile.getFullName());
        txtEmail.setText(currentProfile.getEmail());
        txtPhone.setText(currentProfile.getPhone());
        txtHeadline.setText(currentProfile.getHeadline());
        txtJobTitle.setText(currentProfile.getCurrentJobTitle());
        txtCompany.setText(currentProfile.getCurrentCompany());
        txtLocation.setText(currentProfile.getLocation());
        spnExperience.setValue(currentProfile.getTotalExperienceYears());
        cmbEducation.setSelectedItem(currentProfile.getHighestEducationLevel());
        if (currentProfile.getExpectedSalary() != null) txtSalary.setText(currentProfile.getExpectedSalary().toString());
        txtLinkedin.setText(currentProfile.getLinkedinUrl());
        txtGithub.setText(currentProfile.getGithubUrl());
        txtSummary.setText(currentProfile.getSummary());

        // Refresh sub-tables
        refreshSubTables();
    }

    private void refreshSubTables() {
        if (activeCandidateId <= 0) return;

        // Skills
        skillsModel.setRowCount(0);
        List<CandidateSkill> skills = candidateService.getSkills(activeCandidateId);
        for (CandidateSkill s : skills) {
            skillsModel.addRow(new Object[]{s.getSkillId(), s.getSkillName(), s.getCategoryName(), s.getProficiencyLevel(), s.getYearsOfExperience() + " yrs"});
        }

        // Experience
        expModel.setRowCount(0);
        List<WorkExperience> exps = candidateService.getExperience(activeCandidateId);
        for (WorkExperience w : exps) {
            expModel.addRow(new Object[]{
                    w.getExperienceId(), w.getJobTitle(), w.getCompanyName(), w.getLocation(),
                    DateUtil.formatDate(w.getStartDate()),
                    w.isCurrent() ? "Present" : DateUtil.formatDate(w.getEndDate()),
                    w.getTechnologiesUsed()
            });
        }

        // Education
        eduModel.setRowCount(0);
        List<Education> edus = candidateService.getEducation(activeCandidateId);
        for (Education e : edus) {
            eduModel.addRow(new Object[]{
                    e.getEducationId(), e.getDegree(), e.getDegreeLevel(), e.getInstitution(), e.getFieldOfStudy(),
                    (e.getStartYear() != null ? e.getStartYear() : "") + " - " + (e.getEndYear() != null ? e.getEndYear() : ""),
                    e.getGradeGpa()
            });
        }

        // Certifications
        certModel.setRowCount(0);
        List<Certification> certs = candidateService.getCertifications(activeCandidateId);
        for (Certification c : certs) {
            certModel.addRow(new Object[]{c.getCertificationId(), c.getName(), c.getIssuingOrganization(), DateUtil.formatDate(c.getIssueDate()), c.getCredentialId()});
        }

        // Projects
        projModel.setRowCount(0);
        List<Project> projs = candidateService.getProjects(activeCandidateId);
        for (Project p : projs) {
            projModel.addRow(new Object[]{p.getProjectId(), p.getTitle(), p.getRole(), p.getTechnologies(), p.getDescription()});
        }
    }

    private void saveProfile() {
        if (currentProfile == null) return;

        currentProfile.setFullName(txtFullName.getText().trim());
        currentProfile.setEmail(txtEmail.getText().trim());
        currentProfile.setPhone(txtPhone.getText().trim());
        currentProfile.setHeadline(txtHeadline.getText().trim());
        currentProfile.setCurrentJobTitle(txtJobTitle.getText().trim());
        currentProfile.setCurrentCompany(txtCompany.getText().trim());
        currentProfile.setLocation(txtLocation.getText().trim());
        currentProfile.setTotalExperienceYears(((Number) spnExperience.getValue()).doubleValue());
        currentProfile.setHighestEducationLevel((String) cmbEducation.getSelectedItem());
        try {
            currentProfile.setExpectedSalary(new BigDecimal(txtSalary.getText().trim()));
        } catch (Exception ignored) {}
        currentProfile.setLinkedinUrl(txtLinkedin.getText().trim());
        currentProfile.setGithubUrl(txtGithub.getText().trim());
        currentProfile.setSummary(txtSummary.getText().trim());

        boolean ok = candidateService.updateCandidateProfile(currentProfile);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createSubEntityTab(ModernTable table, String addLabel, String delLabel, Runnable onAdd, Runnable onDel) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new ModernButton(addLabel, ModernButton.ButtonType.PRIMARY);
        JButton btnDel = new ModernButton(delLabel, ModernButton.ButtonType.DANGER);

        btnAdd.addActionListener(e -> onAdd.run());
        btnDel.addActionListener(e -> onDel.run());

        btnBar.add(btnAdd);
        btnBar.add(btnDel);
        p.add(btnBar, BorderLayout.SOUTH);
        return p;
    }

    // --- Sub-entity Dialog Handlers ---
    private void addSkillAction() {
        List<Skill> all = skillService.getAllSkills();
        Skill sel = (Skill) JOptionPane.showInputDialog(this, "Select Skill:", "Add Skill", JOptionPane.PLAIN_MESSAGE, null, all.toArray(), all.isEmpty() ? null : all.get(0));
        if (sel != null) {
            String[] profs = {"Beginner", "Intermediate", "Advanced", "Expert"};
            String prof = (String) JOptionPane.showInputDialog(this, "Select Proficiency:", "Proficiency", JOptionPane.PLAIN_MESSAGE, null, profs, profs[1]);
            String expStr = JOptionPane.showInputDialog(this, "Years of Experience with this skill:", "1.0");
            double exp = 1.0;
            try { exp = Double.parseDouble(expStr); } catch (Exception ignored) {}

            CandidateSkill cs = new CandidateSkill(activeCandidateId, sel.getSkillId(), sel.getSkillName(), prof != null ? prof : "Intermediate", exp);
            candidateService.addOrUpdateSkill(cs);
            refreshSubTables();
        }
    }

    private void deleteSkillAction() {
        int r = skillsTable.getSelectedRow();
        if (r >= 0) {
            int sId = (int) skillsModel.getValueAt(r, 0);
            candidateService.deleteSkill(activeCandidateId, sId);
            refreshSubTables();
        }
    }

    private void addExpAction() {
        JTextField tTitle = new JTextField(15);
        JTextField tComp = new JTextField(15);
        JTextField tLoc = new JTextField(15);
        JTextField tTech = new JTextField(15);
        JCheckBox chkCur = new JCheckBox("Currently working here");

        JPanel p = new JPanel(new GridLayout(5, 2, 6, 6));
        p.add(new JLabel("Job Title:")); p.add(tTitle);
        p.add(new JLabel("Company:")); p.add(tComp);
        p.add(new JLabel("Location:")); p.add(tLoc);
        p.add(new JLabel("Tech Stack:")); p.add(tTech);
        p.add(new JLabel("")); p.add(chkCur);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Work Experience", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !tTitle.getText().trim().isEmpty()) {
            WorkExperience exp = new WorkExperience();
            exp.setCandidateId(activeCandidateId);
            exp.setJobTitle(tTitle.getText().trim());
            exp.setCompanyName(tComp.getText().trim());
            exp.setLocation(tLoc.getText().trim());
            exp.setTechnologiesUsed(tTech.getText().trim());
            exp.setStartDate(LocalDate.now().minusYears(2));
            exp.setCurrent(chkCur.isSelected());
            candidateService.addExperience(exp);
            refreshSubTables();
        }
    }

    private void deleteExpAction() {
        int r = expTable.getSelectedRow();
        if (r >= 0) {
            int id = (int) expModel.getValueAt(r, 0);
            candidateService.deleteExperience(id);
            refreshSubTables();
        }
    }

    private void addEduAction() {
        JTextField tDeg = new JTextField(15);
        JComboBox<String> cLvl = new JComboBox<>(new String[]{"Doctorate", "Master", "Bachelor", "Associate", "High School"});
        JTextField tInst = new JTextField(15);
        JTextField tField = new JTextField(15);

        JPanel p = new JPanel(new GridLayout(4, 2, 6, 6));
        p.add(new JLabel("Degree:")); p.add(tDeg);
        p.add(new JLabel("Level:")); p.add(cLvl);
        p.add(new JLabel("Institution:")); p.add(tInst);
        p.add(new JLabel("Field of Study:")); p.add(tField);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Education", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !tDeg.getText().trim().isEmpty()) {
            Education edu = new Education(activeCandidateId, tDeg.getText().trim(), (String) cLvl.getSelectedItem(), tInst.getText().trim(), tField.getText().trim(), 2018, 2022, "3.8 GPA");
            candidateService.addEducation(edu);
            refreshSubTables();
        }
    }

    private void deleteEduAction() {
        int r = eduTable.getSelectedRow();
        if (r >= 0) {
            int id = (int) eduModel.getValueAt(r, 0);
            candidateService.deleteEducation(id);
            refreshSubTables();
        }
    }

    private void addCertAction() {
        JTextField tName = new JTextField(15);
        JTextField tOrg = new JTextField(15);
        JTextField tId = new JTextField(15);

        JPanel p = new JPanel(new GridLayout(3, 2, 6, 6));
        p.add(new JLabel("Certification Name:")); p.add(tName);
        p.add(new JLabel("Issuing Organization:")); p.add(tOrg);
        p.add(new JLabel("Credential ID:")); p.add(tId);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Certification", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !tName.getText().trim().isEmpty()) {
            Certification c = new Certification(activeCandidateId, tName.getText().trim(), tOrg.getText().trim(), LocalDate.now(), null, tId.getText().trim(), "");
            candidateService.addCertification(c);
            refreshSubTables();
        }
    }

    private void deleteCertAction() {
        int r = certTable.getSelectedRow();
        if (r >= 0) {
            int id = (int) certModel.getValueAt(r, 0);
            candidateService.deleteCertification(id);
            refreshSubTables();
        }
    }

    private void addProjAction() {
        JTextField tTitle = new JTextField(15);
        JTextField tRole = new JTextField(15);
        JTextField tTech = new JTextField(15);
        JTextArea tDesc = new JTextArea(3, 15);

        JPanel p = new JPanel(new BorderLayout(6, 6));
        JPanel grid = new JPanel(new GridLayout(3, 2, 6, 6));
        grid.add(new JLabel("Project Title:")); grid.add(tTitle);
        grid.add(new JLabel("Role:")); grid.add(tRole);
        grid.add(new JLabel("Technologies:")); grid.add(tTech);
        p.add(grid, BorderLayout.NORTH);
        p.add(new JScrollPane(tDesc), BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Project", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION && !tTitle.getText().trim().isEmpty()) {
            Project proj = new Project(activeCandidateId, tTitle.getText().trim(), tRole.getText().trim(), tDesc.getText().trim(), tTech.getText().trim(), "");
            candidateService.addProject(proj);
            refreshSubTables();
        }
    }

    private void deleteProjAction() {
        int r = projTable.getSelectedRow();
        if (r >= 0) {
            int id = (int) projModel.getValueAt(r, 0);
            candidateService.deleteProject(id);
            refreshSubTables();
        }
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0;
        p.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }
}

