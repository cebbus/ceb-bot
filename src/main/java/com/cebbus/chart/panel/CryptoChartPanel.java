package com.cebbus.chart.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.chart.ColorPalette;
import com.cebbus.util.PropertyReader;
import org.jfree.chart.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;

public class CryptoChartPanel {

    private static final int PANEL_WIDTH = 1400;
    private static final int PANEL_HEIGHT = 800;

    private final ApplicationFrame frame;

    private final TradeTable tradeTable;
    private final ChartListPanel chartListPanel;
    private final PerformancePanel performancePanel;

    public CryptoChartPanel(TheOracle theOracle) {
        this.frame = new ApplicationFrame("CebBot!");
        this.frame.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.frame.pack();

        this.tradeTable = new TradeTable(theOracle);
        this.chartListPanel = new ChartListPanel(theOracle);
        this.performancePanel = new PerformancePanel(theOracle);

        addTradePerformance();
        addTradeHistory();
        addChartList();
    }

    private void addTradePerformance() {
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(ColorPalette.DARK_RED);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.setMaximumSize(new Dimension(250, 25));
        titlePanel.setPreferredSize(new Dimension(250, 25));

        JLabel title = new JLabel(PropertyReader.getSymbol() + " - " + PropertyReader.getInterval().name());
        title.setForeground(ColorPalette.SOFT_WIGHT);
        titlePanel.add(title);

        JPanel perPanel = this.performancePanel.create();
        perPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        perPanel.setPreferredSize(new Dimension(250, 250));
        perPanel.setMaximumSize(new Dimension(250, 250));
        perPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel panel = new JPanel();
        panel.add(titlePanel);
        panel.add(perPanel);

        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        this.frame.add(panel, BorderLayout.WEST);
    }

    private void addTradeHistory() {
        JScrollPane scrollPane = new JScrollPane(this.tradeTable.create());
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorPalette.DARK_GRAY));
        scrollPane.setPreferredSize(new Dimension(250, 200));

        this.frame.add(scrollPane, BorderLayout.SOUTH);
    }

    private void addChartList() {
        JScrollPane scrollPane = new JScrollPane(this.chartListPanel.create());
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorPalette.DARK_GRAY));
        this.frame.add(scrollPane, BorderLayout.CENTER);
    }

    public void show() {
        this.frame.setVisible(true);
    }

    public void refresh() {
        this.tradeTable.refresh();
        this.chartListPanel.refresh();
        this.performancePanel.refresh();
    }

}
