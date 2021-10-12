package com.cebbus.chart;

import com.cebbus.analysis.TheOracle;
import com.cebbus.util.PropertyReader;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import javax.swing.*;
import javax.swing.plaf.basic.BasicIconFactory;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

public class CryptoChartPanel {

    private static final int PANEL_WIDTH = 1400;
    private static final int CENTER_WIDTH = 1100;
    private static final int PANEL_HEIGHT = 800;
    private static final int CHART_HEIGHT = 400;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.00");

    private final ApplicationFrame frame;
    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators;
    private final List<TradeTable> tableList = new ArrayList<>();
    private final List<CryptoChart> chartList = new ArrayList<>();
    private final Map<String, JLabel> infoLabelMap = new LinkedHashMap<>();

    private int lastPositionCount;

    public CryptoChartPanel(TheOracle theOracle) {
        this.frame = new ApplicationFrame("CebBot!");
        this.frame.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.frame.pack();

        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.backtest();
        this.indicators = theOracle.getIndicators();

        addCurrInfo();
        addTradePerformance();
        addTradeHistory();
        addChartList();
    }

    private void addCurrInfo() {
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.setBackground(Color.LIGHT_GRAY);

        infoPanel.add(new JLabel(PropertyReader.getSymbol().toUpperCase()));
        infoPanel.add(new JLabel(" - "));
        infoPanel.add(new JLabel(PropertyReader.getInterval().name()));

        this.frame.add(infoPanel, BorderLayout.NORTH);
    }

    private void addTradePerformance() {
        JPanel infoPanel = new JPanel();
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        BoxLayout infoPanelLayout = new BoxLayout(infoPanel, BoxLayout.Y_AXIS);
        infoPanel.setLayout(infoPanelLayout);

        infoPanel.add(createTitleLabel("Backtest Results"));
        Map<String, Object> backtestMap = createCriterionMap(this.backtestRecord);
        backtestMap.forEach((s, o) -> infoPanel.add(createInfoLabel(s, o)));

        infoPanel.add(createTitleLabel("Current Results"));
        Map<String, Object> currentMap = createCriterionMap(this.tradingRecord);
        currentMap.forEach((s, o) -> this.infoLabelMap.put(s, createInfoLabel(s, o)));
        this.infoLabelMap.forEach((s, l) -> infoPanel.add(l));

        this.frame.add(infoPanel, BorderLayout.WEST);
    }

    private void addTradeHistory() {
        TradeTable table = new TradeTable(this.series, this.tradingRecord, this.backtestRecord);
        this.tableList.add(table);

        JScrollPane tablePanel = new JScrollPane(table.create());
        tablePanel.setPreferredSize(new Dimension(250, 200));

        this.frame.add(tablePanel, BorderLayout.SOUTH);
    }

    private void addChartList() {
        JFreeChart[] charts = createChartList();

        JPanel chartListPanel = new JPanel(new FlowLayout());
        chartListPanel.setPreferredSize(new Dimension(CENTER_WIDTH, (charts.length * CHART_HEIGHT) + 25));
        Arrays.stream(charts).forEach(c -> {
            ChartPanel panel = new ChartPanel(c);
            panel.setFillZoomRectangle(true);
            panel.setMouseWheelEnabled(true);
            panel.setPreferredSize(new Dimension(CENTER_WIDTH - 10, CHART_HEIGHT));

            chartListPanel.add(panel);
        });

        JScrollPane scrollPane = new JScrollPane(chartListPanel);
        this.frame.add(scrollPane, BorderLayout.CENTER);
    }

    private JFreeChart[] createChartList() {
        CryptoChartFactory factory = new CryptoChartFactory(this.series, this.tradingRecord, this.backtestRecord);
        this.chartList.add(factory.newCandlestickChart());

        this.indicators.forEach((key, indicatorMap) -> this.chartList.add(factory.newLineChart(indicatorMap)));

        return this.chartList.stream()
                .map(CryptoChart::create)
                .toArray(JFreeChart[]::new);
    }

    private Map<String, Object> createCriterionMap(TradingRecord tradingRecord) {
        GrossReturnCriterion returnCriterion = new GrossReturnCriterion();
        Num totalReturn = returnCriterion.calculate(this.series, tradingRecord);

        NumberOfBarsCriterion numberOfBarsCriterion = new NumberOfBarsCriterion();
        Num numOfBars = numberOfBarsCriterion.calculate(this.series, tradingRecord);

        WinningPositionsRatioCriterion winningRatioCriterion = new WinningPositionsRatioCriterion();
        Num winningRatio = winningRatioCriterion.calculate(this.series, tradingRecord);

        BuyAndHoldReturnCriterion buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
        Num buyAndHold = buyAndHoldReturnCriterion.calculate(this.series, tradingRecord);

        VersusBuyAndHoldCriterion versusBuyAndHoldCriterion = new VersusBuyAndHoldCriterion(returnCriterion);
        Num versus = versusBuyAndHoldCriterion.calculate(this.series, tradingRecord);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Number of pos", tradingRecord.getPositionCount());
        map.put("Number of bars", numOfBars.intValue());
        map.put("Total Return", DECIMAL_FORMAT.format(totalReturn.doubleValue()));
        map.put("Buy-and-hold return", DECIMAL_FORMAT.format(buyAndHold.doubleValue()));
        map.put("Custom vs Buy-and-hold", DECIMAL_FORMAT.format(versus.doubleValue()));
        map.put("Winning positions ratio", DECIMAL_FORMAT.format(winningRatio.doubleValue()));

        return map;
    }

    private JLabel createInfoLabel(String text, Object value) {
        return new JLabel(text + ":    " + value);
    }

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.RED);
        label.setIcon(BasicIconFactory.getMenuArrowIcon());
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        return label;
    }

    public void show() {
        this.frame.setVisible(true);
    }

    public void refresh() {
        if (this.tradingRecord.getPositionCount() > this.lastPositionCount) {
            Map<String, Object> currentMap = createCriterionMap(this.tradingRecord);
            currentMap.forEach((s, o) -> this.infoLabelMap.get(s).setText(createInfoLabel(s, o).getText()));

            this.lastPositionCount = this.tradingRecord.getPositionCount();
        }

        this.chartList.forEach(CryptoChart::refresh);
        this.tableList.forEach(TradeTable::refresh);
    }

}
