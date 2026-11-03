package com.talentiq.ui.components;

import com.talentiq.model.User;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SidebarPanel extends JPanel {

    public interface NavigationListener {
        void onNavigate(String viewName);
    }

    private final NavigationListener listener;
    private final JPanel buttonContainer = new JPanel();
    private final List<JButton> navButtons = new ArrayList<>();
    private String currentView = "";

    public SidebarPanel(NavigationListener listener) {
        this.listener = listener;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 700));
        setBorder(new EmptyBorder(16, 12, 16, 12));
        setBackground(new Color(24, 30, 44)); // Deep charcoal slate

        // Brand Banner
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("✦ TalentIQ");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(new Color(96, 165, 250)); // Bright sky blue
        brandPanel.add(logo);

        JLabel subtitle = new JLabel("Resume & Skill Matching");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setBorder(new EmptyBorder(0, 10, 16, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(brandPanel, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
        buttonContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(buttonContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Version footer
        JLabel footer = new JLabel("v1.0.0 Enterprise");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(100, 116, 139));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));
        add(footer, BorderLayout.SOUTH);

        rebuildMenu();
    }

    public void rebuildMenu() {
        buttonContainer.removeAll();
        navButtons.clear();

        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            buttonContainer.revalidate();
            buttonContainer.repaint();
            return;
        }

        if (user.isAdmin()) {
            addNavCategory("MAIN");
            addNavButton("Dashboard", "ADMIN_DASHBOARD");
            addNavButton("Users & Accounts", "ADMIN_USERS");
            addNavButton("Candidate Directory", "ADMIN_CANDIDATES");
            addNavButton("Job Openings", "ADMIN_JOBS");

            addNavCategory("MANAGEMENT");
            addNavButton("Skill Dictionary", "ADMIN_SKILLS");
            addNavButton("System Settings", "ADMIN_SETTINGS");
            addNavButton("Activity Logs", "ADMIN_LOGS");
            addNavButton("Analytics & Reports", "REPORTS");

        } else if (user.isRecruiter()) {
            addNavCategory("TALENT ACQUISITION");
            addNavButton("Recruiter Dashboard", "RECRUITER_DASHBOARD");
            addNavButton("Job Vacancies", "RECRUITER_JOBS");
            addNavButton("Rank Candidates", "RECRUITER_RANKING");
            addNavButton("Candidate Comparison", "RECRUITER_COMPARISON");
            addNavButton("Skill Gap Analysis", "RECRUITER_GAP_ANALYSIS");
            addNavButton("Job Applications", "RECRUITER_APPLICATIONS");

            addNavCategory("INSIGHTS");
            addNavButton("Analytics & Reports", "REPORTS");

        } else if (user.isCandidate()) {
            addNavCategory("CANDIDATE PORTAL");
            addNavButton("My Dashboard", "CANDIDATE_DASHBOARD");
            addNavButton("Profile & Skills", "CANDIDATE_PROFILE");
            addNavButton("Resume Management", "CANDIDATE_RESUME");
            addNavButton("Browse Jobs & Match", "CANDIDATE_JOBS");
            addNavButton("My Applications", "CANDIDATE_APPLICATIONS");
        }

        buttonContainer.revalidate();
        buttonContainer.repaint();

        // Select first item by default
        if (!navButtons.isEmpty()) {
            navButtons.get(0).doClick();
        }
    }

    private void addNavCategory(String title) {
        JLabel catLabel = new JLabel(title);
        catLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        catLabel.setForeground(new Color(100, 116, 139));
        catLabel.setBorder(new EmptyBorder(12, 12, 6, 12));
        buttonContainer.add(catLabel);
    }

    private void addNavButton(String label, String viewKey) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(new Color(24, 30, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(215, 38));
        btn.setPreferredSize(new Dimension(215, 38));
        btn.setBorder(new EmptyBorder(0, 14, 0, 14));

        btn.addActionListener(e -> {
            for (JButton b : navButtons) {
                b.setBackground(new Color(24, 30, 44));
                b.setForeground(new Color(226, 232, 240));
                b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            btn.setBackground(ThemeManager.COLOR_PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            currentView = viewKey;
            listener.onNavigate(viewKey);
        });

        navButtons.add(btn);
        buttonContainer.add(btn);
        buttonContainer.add(Box.createVerticalStrut(3));
    }
}

