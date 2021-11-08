package com.cebbus.view.panel.test;

import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.ChartListPanel;

import javax.swing.*;
import java.awt.*;

public class TestChartPanel {

    private final JPanel panel;

    public TestChartPanel() {
        this.panel = new JPanel(new BorderLayout());
    }

    public void recreate(Speculator speculator) {
        this.panel.invalidate();
        this.panel.removeAll();

        ChartListPanel chartListPanel = new ChartListPanel(speculator.getTheOracle());
        JScrollPane scrollPane = new JScrollPane(chartListPanel.create());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.panel.add(scrollPane, BorderLayout.CENTER);
        this.panel.revalidate();
        this.panel.repaint();
    }

    public JPanel getPanel() {
        return panel;
    }
}
