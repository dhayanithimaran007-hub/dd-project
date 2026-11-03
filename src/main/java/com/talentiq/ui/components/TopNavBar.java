package com.talentiq.ui.components;

import com.talentiq.model.User;
import com.talentiq.service.AuthService;
import com.talentiq.ui.theme.ThemeManager;
import com.talentiq.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TopNavBar extends JPanel {

    private final JLabel lblTitle = new JLabel("TalentIQ");
    private final JLabel lblUserInfo = new JLabel();
    private final JLabel lblRoleBadge = new JLabel();
    private final JButton btnTheme = new ModernButton(ThemeManager.isDarkMode() ? "☀ Light" : "🌙 Dark", ModernButton.ButtonType.SECONDARY);
    private final JButton btnLogout = new ModernButton("Logout", ModernButton.ButtonType.DANGER);

    private final JFrame parentFrame;
    private final AuthService authService;

    public TopNavBar(JFrame parentFrame, AuthService authService) {
        this.parentFrame = parentFrame;
        this.authService = authService;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(parentFrame.getWidth(), 55));
        setBorder(new EmptyBorder(8, 20, 8, 20));
        setBackground(UIManager.getColor("Panel.background"));

        // Left Branding / Title
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ThemeManager.COLOR_PRIMARY);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        leftPanel.setOpaque(false);
        leftPanel.add(lblTitle);

        // Right Info & Actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2));
        rightPanel.setOpaque(false);

        lblUserInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblRoleBadge.setOpaque(true);
        lblRoleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRoleBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        btnTheme.setPreferredSize(new Dimension(85, 32));
        btnLogout.setPreferredSize(new Dimension(80, 32));

        btnTheme.addActionListener(e -> {
            ThemeManager.toggleTheme(parentFrame);
            btnTheme.setText(ThemeManager.isDarkMode() ? "☀ Light" : "🌙 Dark");
        });

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(parentFrame,
                    "Are you sure you want to log out of TalentIQ?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                authService.logout();
            }
        });

        rightPanel.add(lblUserInfo);
        rightPanel.add(lblRoleBadge);
        rightPanel.add(btnTheme);
        rightPanel.add(btnLogout);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        updateUserStatus();
    }

    public void updateUserStatus() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblUserInfo.setText("Signed in as: " + user.getFullName());
            lblRoleBadge.setText(user.getRoleName());

            Color roleBg;
            if (user.isAdmin()) {
                roleBg = new Color(220, 38, 38); // Red
            } else if (user.isRecruiter()) {
                roleBg = new Color(37, 99, 235); // Blue
            } else {
                roleBg = new Color(16, 185, 129); // Green
            }
            lblRoleBadge.setBackground(roleBg);
            lblRoleBadge.setForeground(Color.WHITE);
        } else {
            lblUserInfo.setText("Not signed in");
            lblRoleBadge.setText("GUEST");
            lblRoleBadge.setBackground(Color.GRAY);
            lblRoleBadge.setForeground(Color.WHITE);
        }
    }

    public void setViewTitle(String title) {
        lblTitle.setText("TalentIQ  /  " + title);
    }
}

