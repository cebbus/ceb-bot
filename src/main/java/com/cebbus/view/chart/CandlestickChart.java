package com.cebbus.view.chart;

import com.cebbus.util.DateTimeUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class CandlestickChart extends CryptoChart {

    private JFreeChart chart;

    private final OHLCSeries ohlcSeries = new OHLCSeries("");

    public CandlestickChart(BarSeries series) {
        super(series);
    }

    @Override
    public List<JMenuItem> createMenuList() {
        return Collections.emptyList();
    }

    @Override
    public JFreeChart create() {
        OHLCDataset dataset = createChartData();
        this.chart = ChartFactory.createCandlestickChart(null, null, null, dataset, false);
        this.chart.setBackgroundPaint(ColorPalette.SOFT_WIGHT);

        XYPlot xyPlot = this.chart.getXYPlot();
        xyPlot.setBackgroundPaint(ColorPalette.LIGHT_GRAY);

        NumberAxis numberAxis = (NumberAxis) xyPlot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);

        CandlestickRenderer renderer = new CustomCandlestickRenderer();
        renderer.setUpPaint(ColorPalette.GREEN);
        renderer.setDownPaint(ColorPalette.RED);

        renderer.setAutoWidthMethod(1);
        xyPlot.setRenderer(renderer);

        return this.chart;
    }

    @Override
    public void refresh() {
        if (this.chart == null) {
            return;
        }

        int endIndex = this.series.getEndIndex();
        Bar bar = this.series.getBar(endIndex);
        RegularTimePeriod period = DateTimeUtil.getBarPeriod(bar);

        if (itemExist(period)) {
            int count = this.ohlcSeries.getItemCount();
            this.ohlcSeries.remove(count - 1);
        }

        this.ohlcSeries.add(createItem(bar));
    }

    private OHLCDataset createChartData() {
        int startIndex = getStartIndex();
        int endIndex = this.series.getEndIndex();
        for (int i = startIndex; i <= endIndex; i++) {
            Bar bar = this.series.getBar(i);
            this.ohlcSeries.add(createItem(bar));
        }

        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(this.ohlcSeries);

        return dataset;
    }

    private OHLCItem createItem(Bar bar) {
        RegularTimePeriod period = DateTimeUtil.getBarPeriod(bar);

        double open = bar.getOpenPrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();

        return new OHLCItem(period, open, high, low, close);
    }

    private boolean itemExist(RegularTimePeriod newPeriod) {
        int count = this.ohlcSeries.getItemCount();
        if (count == 0) {
            return false;
        }

        RegularTimePeriod lastPeriod = this.ohlcSeries.getPeriod(count - 1);
        return lastPeriod.equals(newPeriod);
    }
}
