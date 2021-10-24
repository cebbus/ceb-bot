package com.cebbus.chart.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.chart.CryptoChart;
import com.cebbus.chart.CryptoChartFactory;
import com.cebbus.chart.LegendClickListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChartListPanel {

    private static final int CENTER_WIDTH = 1100;
    private static final int CHART_HEIGHT = 400;

    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators;
    private final List<CryptoChart> chartList = new ArrayList<>();

    public ChartListPanel(TheOracle theOracle) {
        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.backtest();
        this.indicators = theOracle.getIndicators();
    }

    public JPanel create() {
        JFreeChart[] charts = createChartList();

        JPanel chartListPanel = new JPanel(new FlowLayout());
        chartListPanel.setPreferredSize(new Dimension(CENTER_WIDTH, (charts.length * CHART_HEIGHT) + 25));
        Arrays.stream(charts).forEach(c -> {
            ChartPanel panel = new ChartPanel(c);
            panel.setFillZoomRectangle(true);
            panel.setMouseWheelEnabled(true);
            panel.addChartMouseListener(new LegendClickListener());
            panel.setPreferredSize(new Dimension(CENTER_WIDTH - 10, CHART_HEIGHT));

            chartListPanel.add(panel);
        });

        return chartListPanel;
    }

    public void refresh() {
        this.chartList.forEach(CryptoChart::refresh);
    }

    private JFreeChart[] createChartList() {
        CryptoChartFactory factory = new CryptoChartFactory(this.series, this.tradingRecord, this.backtestRecord);
        this.chartList.add(factory.newCandlestickChart());

        this.indicators.forEach((key, indicatorMap) -> this.chartList.add(factory.newLineChart(indicatorMap)));

        return this.chartList.stream()
                .map(CryptoChart::create)
                .toArray(JFreeChart[]::new);
    }
}
