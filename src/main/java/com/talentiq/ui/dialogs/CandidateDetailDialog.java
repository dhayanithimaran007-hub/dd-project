package com.talentiq.ui.dialogs;

import com.talentiq.model.*;
import com.talentiq.service.CandidateService;
import com.talentiq.ui.components.ModernButton;
import com.talentiq.ui.components.ModernTable;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CandidateDetailDialog extends JDialog {

    public CandidateDetailDialog(JFrame parent, CandidateProfile candidate, CandidateService candidateService) {
        super(parent, "Candidate Dossier - " + candidate.getFullName(), true);
        setSize(800, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblName = new JLabel(candidate.getFullName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(ThemeManager.COLOR_PRIMARY);

        JLabel lblHeadline = new JLabel(candidate.getHeadline() != null ? candidate.getHeadline() : "Software Professional");
        lblHeadline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblHeadline.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(lblName);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(lblHeadline);

        header.add(titleBlock, BorderLayout.WEST);
        root.add(header);
        root.add(Box.createVerticalStrut(16));

        // Quick Stats Card
        JPanel statsCard = new JPanel(new GridLayout(2, 3, 12, 10));
        statsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150, 40), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        statsCard.add(createStatItem("Email:", candidate.getEmail()));
        statsCard.add(createStatItem("Phone:", candidate.getPhone()));
        statsCard.add(createStatItem("Location:", candidate.getLocation()));
        statsCard.add(createStatItem("Total Experience:", candidate.getTotalExperienceYears() + " Years"));
        statsCard.add(createStatItem("Highest Education:", candidate.getHighestEducationLevel()));
        statsCard.add(createStatItem("Current Company:", candidate.getCurrentCompany() != null ? candidate.getCurrentCompany() : "N/A"));
        root.add(statsCard);
        root.add(Box.createVerticalStrut(16));

        // Sub-Entities Tabs
        JTabbedPane tabs = new JTabbedPane();

        // 1. Technical Skills Tab
        DefaultTableModel skillsModel = new DefaultTableModel(new String[]{"Skill Name", "Category", "Proficiency", "Years of Exp"}, 0);
        ModernTable skillsTable = new ModernTable(skillsModel);
        if (candidate.getSkills() != null) {
            for (CandidateSkill s : candidate.getSkills()) {
                skillsModel.addRow(new Object[]{s.getSkillName(), s.getCategoryName(), s.getProficiencyLevel(), s.getYearsOfExperience() + " yrs"});
            }
        }
        tabs.addTab("Skills (" + (candidate.getSkills() != null ? candidate.getSkills().size() : 0) + ")", new JScrollPane(skillsTable));

        // 2. Work Experience Tab
        DefaultTableModel expModel = new DefaultTableModel(new String[]{"Job Title", "Company", "Location", "Start", "End", "Technologies"}, 0);
        ModernTable expTable = new ModernTable(expModel);
        if (candidate.getExperienceList() != null) {
            for (WorkExperience w : candidate.getExperienceList()) {
                expModel.addRow(new Object[]{
                        w.getJobTitle(), w.getCompanyName(), w.getLocation(),
                        DateUtil.formatDate(w.getStartDate()),
                        w.isCurrent() ? "Present" : DateUtil.formatDate(w.getEndDate()),
                        w.getTechnologiesUsed()
                });
            }
        }
        tabs.addTab("Experience (" + (candidate.getExperienceList() != null ? candidate.getExperienceList().size() : 0) + ")", new JScrollPane(expTable));

        // 3. Education Tab
        DefaultTableModel eduModel = new DefaultTableModel(new String[]{"Degree", "Degree Level", "Institution", "Field", "Years", "Grade"}, 0);
        ModernTable eduTable = new ModernTable(eduModel);
        if (candidate.getEducationList() != null) {
            for (Education ed : candidate.getEducationList()) {
                eduModel.addRow(new Object[]{
                        ed.getDegree(), ed.getDegreeLevel(), ed.getInstitution(), ed.getFieldOfStudy(),
                        (ed.getStartYear() != null ? ed.getStartYear() : "") + " - " + (ed.getEndYear() != null ? ed.getEndYear() : ""),
                        ed.getGradeGpa()
                });
            }
        }
        tabs.addTab("Education (" + (candidate.getEducationList() != null ? candidate.getEducationList().size() : 0) + ")", new JScrollPane(eduTable));

        // 4. Certifications Tab
        DefaultTableModel certModel = new DefaultTableModel(new String[]{"Certification", "Issuing Organization", "Issue Date", "Credential ID"}, 0);
        ModernTable certTable = new ModernTable(certModel);
        if (candidate.getCertifications() != null) {
            for (Certification c : candidate.getCertifications()) {
                certModel.addRow(new Object[]{c.getName(), c.getIssuingOrganization(), DateUtil.formatDate(c.getIssueDate()), c.getCredentialId()});
            }
        }
        tabs.addTab("Certifications (" + (candidate.getCertifications() != null ? candidate.getCertifications().size() : 0) + ")", new JScrollPane(certTable));

        // 5. Projects Tab
        DefaultTableModel projModel = new DefaultTableModel(new String[]{"Project Title", "Role", "Technologies", "Description"}, 0);
        ModernTable projTable = new ModernTable(projModel);
        if (candidate.getProjects() != null) {
            for (Project p : candidate.getProjects()) {
                projModel.addRow(new Object[]{p.getTitle(), p.getRole(), p.getTechnologies(), p.getDescription()});
            }
        }
        tabs.addTab("Projects (" + (candidate.getProjects() != null ? candidate.getProjects().size() : 0) + ")", new JScrollPane(projTable));

        root.add(tabs);

        JScrollPane mainScroll = new JScrollPane(root);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        JButton btnClose = new ModernButton("Close", ModernButton.ButtonType.PRIMARY);
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createStatItem(String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel l1 = new JLabel(label);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel l2 = new JLabel(value != null && !value.isBlank() ? value : "N/A");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(l1);
        p.add(l2);
        return p;
    }
}

