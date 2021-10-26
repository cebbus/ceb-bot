package com.cebbus.chart.panel;

import com.cebbus.binance.Speculator;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.chart.ColorPalette;

import javax.swing.*;
import java.awt.*;

public class CryptoTabPanel {

    private final JPanel panel;
    private final PanelMenu panelMenu;
    private final StatusPanel statusPanel;
    private final TradeTable tradeTable;
    private final ChartListPanel chartListPanel;
    private final PerformancePanel performancePanel;

    public CryptoTabPanel(Speculator speculator) {
        this.panel = new JPanel(new BorderLayout());

        this.panelMenu = new PanelMenu(speculator);
        this.statusPanel = new StatusPanel(speculator);
        this.tradeTable = new TradeTable(speculator.getTheOracle());
        this.chartListPanel = new ChartListPanel(speculator.getTheOracle());
        this.performancePanel = new PerformancePanel(speculator);

        addMenu();
        addTradePerformance();
        addTradeHistory();
        addChartList();
    }

    private void addMenu() {
        this.panel.add(this.panelMenu.create(), BorderLayout.NORTH);
    }

    private void addTradePerformance() {
        JPanel statePanel = this.statusPanel.getPanel();
        statePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statePanel.setMaximumSize(new Dimension(250, 25));
        statePanel.setPreferredSize(new Dimension(250, 25));

        JPanel perPanel = this.performancePanel.create();
        perPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        perPanel.setPreferredSize(new Dimension(250, 350));
        perPanel.setMaximumSize(new Dimension(250, 350));
        perPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel panel = new JPanel();
        panel.add(statePanel);
        panel.add(perPanel);

        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        this.panel.add(panel, BorderLayout.WEST);
    }

    private void addTradeHistory() {
        JScrollPane scrollPane = new JScrollPane(this.tradeTable.create());
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorPalette.DARK_GRAY));
        scrollPane.setPreferredSize(new Dimension(250, 200));

        this.panel.add(scrollPane, BorderLayout.SOUTH);
    }

    private void addChartList() {
        JScrollPane scrollPane = new JScrollPane(this.chartListPanel.create());
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorPalette.DARK_GRAY));
        this.panel.add(scrollPane, BorderLayout.CENTER);
    }

    public void refresh() {
        this.tradeTable.refresh();
        this.chartListPanel.refresh();
        this.performancePanel.refresh();
    }

    public void changeStatus(TradeStatus status) {
        this.statusPanel.changeStatus(status);
    }

    public JPanel getPanel() {
        return panel;
    }
}
