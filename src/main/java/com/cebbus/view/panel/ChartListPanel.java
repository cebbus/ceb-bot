package com.cebbus.view.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.view.chart.CryptoChart;
import com.cebbus.view.chart.CryptoChartFactory;
import com.cebbus.view.chart.LegendClickListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChartListPanel {

    private static final int CENTER_WIDTH = 1100;
    private static final int CHART_HEIGHT = 400;

    private final TheOracle theOracle;
    private final List<CryptoChart> chartList = new ArrayList<>();

    public ChartListPanel(TheOracle theOracle) {
        this.theOracle = theOracle;
    }

    public JPanel create() {
        fillChartList();

        JPanel jPanel = new JPanel();
        jPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        BoxLayout layout = new BoxLayout(jPanel, BoxLayout.Y_AXIS);
        jPanel.setLayout(layout);

        this.chartList.forEach(cryptoChart -> {
            JFreeChart chart = cryptoChart.create();
            List<JMenuItem> menuItemList = cryptoChart.createMenuList();

            ChartPanel panel = new ChartPanel(chart);
            panel.setFillZoomRectangle(true);
            panel.setMouseWheelEnabled(true);
            panel.addChartMouseListener(new LegendClickListener());
            panel.setPreferredSize(new Dimension(CENTER_WIDTH - 10, CHART_HEIGHT));

            JPopupMenu menu = panel.getPopupMenu();
            for (JMenuItem item : menuItemList) {
                menu.addSeparator();
                menu.add(item);
            }

            jPanel.add(panel);
            jPanel.add(Box.createVerticalStrut(8));
        });

        return jPanel;
    }

    public void refresh() {
        this.chartList.forEach(CryptoChart::refresh);
    }

    private void fillChartList() {
        CryptoChartFactory factory = new CryptoChartFactory(this.theOracle);
        this.chartList.add(factory.newCandlestickChart());
        this.theOracle.getIndicators().forEach((key, indicatorMap) -> this.chartList.add(factory.newLineChart(indicatorMap)));
    }
}
