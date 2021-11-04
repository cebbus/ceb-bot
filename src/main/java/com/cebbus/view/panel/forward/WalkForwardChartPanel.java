package com.cebbus.view.panel.forward;

import com.cebbus.binance.Speculator;
import com.cebbus.view.chart.ColorPalette;
import com.cebbus.view.panel.ChartListPanel;

import javax.swing.*;
import java.awt.*;

public class WalkForwardChartPanel {

    private final JPanel panel;

    public WalkForwardChartPanel() {
        this.panel = new JPanel(new BorderLayout());
        this.panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorPalette.DARK_GRAY));
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
