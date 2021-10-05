package com.cebbus.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.HashMap;
import java.util.Map;

public class LineChart extends CryptoChart {

    private JFreeChart chart;
    private int lastPositionCount;

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

        XYPlot xyPlot = this.chart.getXYPlot();
        addSignals(xyPlot, this.backtestRecord);

        return this.chart;
    }

    @Override
    void refresh() {
        if (this.chart == null) {
            return;
        }

        this.indicatorMap.forEach((name, indicator) -> {
            int endIndex = this.series.getEndIndex();
            Bar bar = this.series.getBar(endIndex);

            RegularTimePeriod period = convertToPeriod(bar.getEndTime());
            double value = indicator.getValue(endIndex).doubleValue();

            this.timeSeriesMap.get(name).add(period, value);
        });

        if (this.tradingRecord.getPositionCount() > this.lastPositionCount) {
            XYPlot xyPlot = this.chart.getXYPlot();
            Position lastPosition = this.tradingRecord.getLastPosition();

            addSignal(xyPlot, lastPosition);
            this.lastPositionCount = this.tradingRecord.getPositionCount();
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
}
