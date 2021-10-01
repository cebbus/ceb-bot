package com.cebbus.chart;

import com.cebbus.analysis.TheOracle;
import com.cebbus.util.PropertyReader;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CryptoChartPanel {

    static final int PANEL_WIDTH = 1400;
    static final int WEST_WIDTH = 250;
    static final int CENTER_WIDTH = 1100;
    static final int PANEL_HEIGHT = 800;
    static final int CHART_HEIGHT = 400;
    static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.00");

    private final ApplicationFrame frame;
    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators;
    private final List<CryptoChart> chartList = new ArrayList<>();

    public CryptoChartPanel(TheOracle theOracle) {
        this.frame = new ApplicationFrame("CebBot!");
        this.frame.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.frame.pack();

        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.backtest();
        this.indicators = theOracle.getIndicators();

        addCurrInfo();
        addTradeInfo();
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

    public void addTradeInfo() {
        JPanel infoPanel = new JPanel();

        GrossReturnCriterion criterion = new GrossReturnCriterion();
        Num totalReturn = criterion.calculate(this.series, this.backtestRecord);

        infoPanel.add(new JLabel("Number of Pos: " + this.backtestRecord.getPositionCount()));
        infoPanel.add(new JLabel("Total Return: " + DECIMAL_FORMAT.format(totalReturn.doubleValue())));
        /*
        JLabel totalReturnLabel = new JLabel(DECIMAL_FORMAT.format(totalReturn.doubleValue()));
        if (totalReturn.doubleValue() > 1) {
            totalReturnLabel.setForeground(new Color(0, 120, 62));
        } else {
            totalReturnLabel.setForeground(Color.RED);
        }

        infoPanel.add(totalReturnLabel);
         */

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(WEST_WIDTH, 250));
        scrollPane.add(infoPanel);

        this.frame.add(scrollPane, BorderLayout.WEST);

//        GrossReturnCriterion totalReturn = new GrossReturnCriterion();
//        System.out.println("Total return: " + totalReturn.calculate(series, tradingRecord));
//        System.out.println("Number of bars: " + (new NumberOfBarsCriterion()).calculate(series, tradingRecord));
//        System.out.println("Average return (per bar): " + (new AverageReturnPerBarCriterion()).calculate(series, tradingRecord));
//        System.out.println("Number of positions: " + (new NumberOfPositionsCriterion()).calculate(series, tradingRecord));
//        System.out.println("Winning positions ratio: " + (new WinningPositionsRatioCriterion()).calculate(series, tradingRecord));
//        System.out.println("Maximum drawdown: " + (new MaximumDrawdownCriterion()).calculate(series, tradingRecord));
//        System.out.println("Return over maximum drawdown: " + (new ReturnOverMaxDrawdownCriterion()).calculate(series, tradingRecord));
//        System.out.println("Total transaction cost (from $1000): " + (new LinearTransactionCostCriterion(1000.0D, 0.005D)).calculate(series, tradingRecord));
//        System.out.println("Buy-and-hold return: " + (new BuyAndHoldReturnCriterion()).calculate(series, tradingRecord));
//        System.out.println("Custom strategy return vs buy-and-hold strategy return: " + (new VersusBuyAndHoldCriterion(totalReturn)).calculate(series, tradingRecord));
    }

    public void addChartList() {
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

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scrollPane.add(chartListPanel);

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

    public void show() {
        this.frame.setVisible(true);
    }

    public void refreshCharts() {
        this.chartList.forEach(CryptoChart::refresh);
    }

}
