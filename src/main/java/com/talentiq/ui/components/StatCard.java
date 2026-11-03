package com.talentiq.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor != null ? accentColor : new Color(37, 99, 235);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setBackground(UIManager.getColor("Panel.background"));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(UIManager.getColor("Label.foreground"));

        lblSubtitle = new JLabel(subtitle != null ? subtitle : "");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(lblValue);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(lblSubtitle);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color cardBg = UIManager.getColor("statCard.background");
        if (cardBg == null) {
            cardBg = UIManager.getColor("Panel.background");
            if (cardBg != null) {
                // slightly lift the color
                cardBg = new Color(
                        Math.min(255, cardBg.getRed() + 8),
                        Math.min(255, cardBg.getGreen() + 8),
                        Math.min(255, cardBg.getBlue() + 8)
                );
            }
        }

        g2.setColor(cardBg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        // Accent top bar
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, 4, 4, 4);

        // Soft border
        g2.setColor(new Color(150, 150, 150, 40));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}

