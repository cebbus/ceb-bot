package com.cebbus.view.panel;

import com.cebbus.view.chart.ColorPalette;

import javax.swing.*;
import java.awt.*;

public class BoxTitlePanel {
    private final JPanel panel = new JPanel();

    public BoxTitlePanel(String titleText) {
        this(titleText, true);
    }

    public BoxTitlePanel(String titleText, boolean active) {
        JLabel title = new JLabel();
        title.setText(titleText);
        title.setForeground(ColorPalette.SOFT_WIGHT);

        this.panel.add(title);
        this.panel.setBackground(getColor(active));
        this.panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.panel.setMaximumSize(new Dimension(250, 25));
        this.panel.setPreferredSize(new Dimension(250, 25));
    }

    public void changeStatus(boolean active) {
        changeColor(getColor(active));
    }

    public void changeColor(Color color) {
        this.panel.setBackground(color);
    }

    public JPanel getPanel() {
        return panel;
    }

    private Color getColor(boolean active) {
        return active ? ColorPalette.DARK_GREEN : ColorPalette.DARK_RED;
    }
}
