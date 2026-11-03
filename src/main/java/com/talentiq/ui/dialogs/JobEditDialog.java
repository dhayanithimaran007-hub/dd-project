package com.talentiq.ui.dialogs;

import com.talentiq.model.Job;
import com.talentiq.model.JobSkill;
import com.talentiq.model.Skill;
import com.talentiq.service.JobService;
import com.talentiq.service.SkillService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobEditDialog extends JDialog {

    private final JTextField txtTitle = new JTextField(24);
    private final JTextField txtDepartment = new JTextField(18);
    private final JTextArea txtDescription = new JTextArea(4, 25);
    private final JSpinner spnMinExp = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 30.0, 0.5));
    private final JComboBox<String> cmbMinEdu = new JComboBox<>(new String[]{"Doctorate", "Master", "Bachelor", "Associate", "High School"});
    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"Full-time", "Part-time", "Contract", "Remote", "Hybrid", "On-site"});
    private final JTextField txtLocation = new JTextField("Remote", 18);
    private final JTextField txtSalaryMin = new JTextField("80000", 10);
    private final JTextField txtSalaryMax = new JTextField("120000", 10);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{Job.STATUS_ACTIVE, Job.STATUS_CLOSED, Job.STATUS_DRAFT});
    private final JTextField txtDeadline = new JTextField(LocalDate.now().plusMonths(2).format(DateUtil.ISO_DATE), 10);

    // Skills tables
    private final DefaultTableModel reqSkillsModel = new DefaultTableModel(new String[]{"Skill ID", "Required Skill", "Min Proficiency"}, 0);
    private final ModernTable reqSkillsTable = new ModernTable(reqSkillsModel);

    private final DefaultTableModel prefSkillsModel = new DefaultTableModel(new String[]{"Skill ID", "Preferred Skill", "Min Proficiency"}, 0);
    private final ModernTable prefSkillsTable = new ModernTable(prefSkillsModel);

    private boolean saved = false;

    public JobEditDialog(JFrame parent, Job job, int recruiterId, JobService jobService, SkillService skillService) {
        super(parent, job.getJobId() > 0 ? "Edit Job Vacancy" : "Post New Job Vacancy", true);
        setSize(850, 780);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Prepopulate if editing
        if (job.getJobId() > 0) {
            txtTitle.setText(job.getTitle());
            txtDepartment.setText(job.getDepartment());
            txtDescription.setText(job.getDescription());
            spnMinExp.setValue(job.getMinExperienceYears());
            cmbMinEdu.setSelectedItem(job.getMinEducationLevel());
            cmbType.setSelectedItem(job.getEmploymentType());
            txtLocation.setText(job.getLocation());
            if (job.getSalaryMin() != null) txtSalaryMin.setText(job.getSalaryMin().toString());
            if (job.getSalaryMax() != null) txtSalaryMax.setText(job.getSalaryMax().toString());
            cmbStatus.setSelectedItem(job.getStatus());
            if (job.getDeadline() != null) txtDeadline.setText(job.getDeadline().format(DateUtil.ISO_DATE));

            if (job.getRequiredSkills() != null) {
                for (JobSkill js : job.getRequiredSkills()) {
                    reqSkillsModel.addRow(new Object[]{js.getSkillId(), js.getSkillName(), js.getMinProficiency()});
                }
            }
            if (job.getPreferredSkills() != null) {
                for (JobSkill js : job.getPreferredSkills()) {
                    prefSkillsModel.addRow(new Object[]{js.getSkillId(), js.getSkillName(), js.getMinProficiency()});
                }
            }
        }

        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        JTabbedPane tabs = new JTabbedPane();

        // 1. General Info Tab
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Job Title:", txtTitle);
        addFormRow(formPanel, gbc, row++, "Department:", txtDepartment);
        addFormRow(formPanel, gbc, row++, "Employment Type:", cmbType);
        addFormRow(formPanel, gbc, row++, "Office Location:", txtLocation);
        addFormRow(formPanel, gbc, row++, "Min Experience (Years):", spnMinExp);
        addFormRow(formPanel, gbc, row++, "Min Education Qualification:", cmbMinEdu);

        JPanel salaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        salaryPanel.add(new JLabel("Min:"));
        salaryPanel.add(txtSalaryMin);
        salaryPanel.add(Box.createHorizontalStrut(10));
        salaryPanel.add(new JLabel("Max:"));
        salaryPanel.add(txtSalaryMax);
        addFormRow(formPanel, gbc, row++, "Salary Range (USD):", salaryPanel);

        addFormRow(formPanel, gbc, row++, "Application Deadline (YYYY-MM-DD):", txtDeadline);
        addFormRow(formPanel, gbc, row++, "Listing Status:", cmbStatus);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Job Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        formPanel.add(new JScrollPane(txtDescription), gbc);

        tabs.addTab("General Details", formPanel);

        // 2. Required Skills Tab (60% weight)
        JPanel reqPanel = createSkillSubPanel("Required Skills (60% Match Weight)", reqSkillsModel, reqSkillsTable, skillService);
        tabs.addTab("Required Skills", reqPanel);

        // 3. Preferred Skills Tab (20% weight)
        JPanel prefPanel = createSkillSubPanel("Preferred Skills (20% Match Weight)", prefSkillsModel, prefSkillsTable, skillService);
        tabs.addTab("Preferred Skills", prefPanel);

        add(tabs, BorderLayout.CENTER);

        // Action Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        JButton btnSave = new ModernButton("Save Job Vacancy", ModernButton.ButtonType.PRIMARY);
        JButton btnCancel = new ModernButton("Cancel", ModernButton.ButtonType.SECONDARY);

        btnSave.addActionListener(e -> {
            if (txtTitle.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Job title is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            job.setRecruiterId(recruiterId > 0 ? recruiterId : 1);
            job.setTitle(txtTitle.getText().trim());
            job.setDepartment(txtDepartment.getText().trim());
            job.setDescription(txtDescription.getText().trim());
            job.setMinExperienceYears(((Number) spnMinExp.getValue()).doubleValue());
            job.setMinEducationLevel((String) cmbMinEdu.getSelectedItem());
            job.setEmploymentType((String) cmbType.getSelectedItem());
            job.setLocation(txtLocation.getText().trim());
            job.setStatus((String) cmbStatus.getSelectedItem());

            try {
                job.setSalaryMin(new BigDecimal(txtSalaryMin.getText().trim()));
                job.setSalaryMax(new BigDecimal(txtSalaryMax.getText().trim()));
            } catch (Exception ignored) {}

            try {
                job.setDeadline(LocalDate.parse(txtDeadline.getText().trim()));
            } catch (Exception ignored) {}

            // Populate skills
            List<JobSkill> reqList = new ArrayList<>();
            for (int i = 0; i < reqSkillsModel.getRowCount(); i++) {
                int sId = (int) reqSkillsModel.getValueAt(i, 0);
                String sName = (String) reqSkillsModel.getValueAt(i, 1);
                String prof = (String) reqSkillsModel.getValueAt(i, 2);
                reqList.add(new JobSkill(job.getJobId(), sId, sName, prof, true));
            }
            job.setRequiredSkills(reqList);

            List<JobSkill> prefList = new ArrayList<>();
            for (int i = 0; i < prefSkillsModel.getRowCount(); i++) {
                int sId = (int) prefSkillsModel.getValueAt(i, 0);
                String sName = (String) prefSkillsModel.getValueAt(i, 1);
                String prof = (String) prefSkillsModel.getValueAt(i, 2);
                prefList.add(new JobSkill(job.getJobId(), sId, sName, prof, false));
            }
            job.setPreferredSkills(prefList);

            try {
                if (job.getJobId() > 0) {
                    jobService.updateJob(job);
                } else {
                    jobService.createJob(job);
                }
                saved = true;
                JOptionPane.showMessageDialog(this, "Job vacancy saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save job: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dispose());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createSkillSubPanel(String title, DefaultTableModel model, ModernTable table, SkillService skillService) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(new JLabel("<html><b>" + title + "</b> - Define skills for matching candidate resumes.</html>"), BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnAdd = new ModernButton("+ Add Skill", ModernButton.ButtonType.SECONDARY);
        JButton btnDel = new ModernButton("- Remove Skill", ModernButton.ButtonType.DANGER);

        btnAdd.addActionListener(e -> {
            List<Skill> all = skillService.getAllSkills();
            Skill selected = (Skill) JOptionPane.showInputDialog(this,
                    "Select a skill from centralized dictionary:",
                    "Add Skill", JOptionPane.PLAIN_MESSAGE, null, all.toArray(), all.isEmpty() ? null : all.get(0));
            if (selected != null) {
                // Check if already added
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((int) model.getValueAt(i, 0) == selected.getSkillId()) {
                        JOptionPane.showMessageDialog(this, "This skill is already in the list.");
                        return;
                    }
                }
                String[] profs = {"Beginner", "Intermediate", "Advanced", "Expert"};
                String prof = (String) JOptionPane.showInputDialog(this, "Select minimum required proficiency:", "Proficiency Level", JOptionPane.PLAIN_MESSAGE, null, profs, profs[1]);
                model.addRow(new Object[]{selected.getSkillId(), selected.getSkillName(), prof != null ? prof : "Intermediate"});
            }
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.removeRow(row);
            }
        });

        btnBar.add(btnAdd);
        btnBar.add(btnDel);
        topBar.add(btnBar, BorderLayout.EAST);

        p.add(topBar, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
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

