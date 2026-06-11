package com.fragrance.util;

import javax.swing.*;
import java.awt.*;
public class RoundedPanel extends JPanel {
    private int radius;
    private Color bgColor;
    private Color borderColor;
    public RoundedPanel(int radius, Color bgColor, Color borderColor) {
        super(new BorderLayout());
        this.radius = radius;
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        setOpaque(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}