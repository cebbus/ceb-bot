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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CryptoChartPanel {

    static final int PANEL_WIDTH = 1400;
    static final int PANEL_HEIGHT = 800;
    static final int CHART_HEIGHT = 400;

    private final ApplicationFrame frame;
    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators;

    public CryptoChartPanel(TheOracle theOracle) {
        this.frame = new ApplicationFrame("CebBot!");
        this.frame.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.frame.pack();

        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.backtest();
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
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.setBackground(Color.LIGHT_GRAY);

        infoPanel.add(new JLabel("Number of positions: "));
        infoPanel.add(new JLabel(this.tradingRecord.getPositionCount() + ""));

        infoPanel.add(new JLabel("Total return: "));

        GrossReturnCriterion criterion = new GrossReturnCriterion();
        Num totalReturn = criterion.calculate(this.series, this.tradingRecord);
        JLabel totalReturnLabel = new JLabel(totalReturn + "");
        if (totalReturn.doubleValue() > 1) {
            totalReturnLabel.setForeground(new Color(0, 120, 62));
        } else {
            totalReturnLabel.setForeground(Color.RED);
        }

        infoPanel.add(totalReturnLabel);

        this.frame.add(infoPanel, BorderLayout.SOUTH);

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
        chartListPanel.setPreferredSize(new Dimension(PANEL_WIDTH - 40, (charts.length * CHART_HEIGHT) + 25));
        Arrays.stream(charts).forEach(c -> {
            ChartPanel panel = new ChartPanel(c);
            panel.setFillZoomRectangle(true);
            panel.setMouseWheelEnabled(true);
            panel.setPreferredSize(new Dimension(PANEL_WIDTH - 50, CHART_HEIGHT));

            chartListPanel.add(panel);
        });

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scrollPane.add(chartListPanel);

        this.frame.add(scrollPane, BorderLayout.CENTER);
    }

    private JFreeChart[] createChartList() {
        List<JFreeChart> charts = new ArrayList<>();

        CryptoChartFactory factory = new CryptoChartFactory(this.series, this.tradingRecord);
        charts.add(factory.newCandlestickChart());

        this.indicators.forEach((key, indicatorMap) -> charts.add(factory.newLineChart(indicatorMap)));

        return charts.toArray(new JFreeChart[0]);
    }

    public void show() {
        this.frame.setVisible(true);
    }

}
