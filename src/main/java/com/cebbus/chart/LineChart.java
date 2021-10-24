package com.cebbus.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.HashMap;
import java.util.Map;

public class LineChart extends CryptoChart {

    private JFreeChart chart;
    private Trade lastTradeBuffer;

    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;
    private final Map<String, CachedIndicator<Num>> indicatorMap;
    private final Map<String, TimeSeries> timeSeriesMap = new HashMap<>();

    public LineChart(
            BarSeries series,
            Map<String, CachedIndicator<Num>> indicatorMap,
            TradingRecord tradingRecord,
            TradingRecord backtestRecord) {
        super(series);

        this.indicatorMap = indicatorMap;
        this.tradingRecord = tradingRecord;
        this.backtestRecord = backtestRecord;
    }

    @Override
    public JFreeChart create() {
        this.indicatorMap.forEach((name, indicator) -> this.timeSeriesMap.put(name, createChartData(name, indicator)));

        TimeSeriesCollection axis = new TimeSeriesCollection();
        this.timeSeriesMap.forEach((name, timeSeries) -> axis.addSeries(timeSeries));

        this.chart = ChartFactory.createTimeSeriesChart(null, null,
                null, axis, true, true, false);

        this.chart.setBackgroundPaint(ColorPalette.SOFT_WIGHT);
        this.chart.getLegend().setBackgroundPaint(ColorPalette.SOFT_WIGHT);

        XYPlot xyPlot = this.chart.getXYPlot();
        xyPlot.setDrawingSupplier(new ChartDrawingSupplier());
        xyPlot.setBackgroundPaint(ColorPalette.LIGHT_GRAY);
        xyPlot.setFixedLegendItems(xyPlot.getLegendItems());

        addSignals(xyPlot, this.backtestRecord, true);
        addSignals(xyPlot, this.tradingRecord, false);

        return this.chart;
    }

    @Override
    public void refresh() {
        if (this.chart == null) {
            return;
        }

        this.indicatorMap.forEach((name, indicator) -> {
            int endIndex = this.series.getEndIndex();
            Bar bar = this.series.getBar(endIndex);
            RegularTimePeriod period = convertToPeriod(bar.getEndTime());

            TimeSeries timeSeries = this.timeSeriesMap.get(name);
            if (!itemExist(timeSeries, period)) {
                double value = indicator.getValue(endIndex).doubleValue();
                timeSeries.add(period, value);
            }
        });

        Trade lastTrade = this.tradingRecord.getLastTrade();
        if (lastTrade != null && !lastTrade.equals(this.lastTradeBuffer)) {
            XYPlot xyPlot = this.chart.getXYPlot();
            addSignal(xyPlot, lastTrade);

            this.lastTradeBuffer = lastTrade;
        }
    }

    private TimeSeries createChartData(String name, CachedIndicator<Num> indicator) {
        TimeSeries timeSeries = new TimeSeries(name);

        int startIndex = getStartIndex();
        int endIndex = this.series.getEndIndex();
        for (int i = startIndex; i < endIndex; i++) {
            Bar bar = this.series.getBar(i);

            RegularTimePeriod period = convertToPeriod(bar.getEndTime());
            double value = indicator.getValue(i).doubleValue();

            timeSeries.add(period, value);
        }

        return timeSeries;
    }

    private boolean itemExist(TimeSeries timeSeries, RegularTimePeriod newPeriod) {
        int count = timeSeries.getItemCount();
        if (count == 0) {
            return false;
        }

        RegularTimePeriod lastPeriod = timeSeries.getTimePeriod(count - 1);
        return lastPeriod.equals(newPeriod);
    }
}
