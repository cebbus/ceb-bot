package com.cebbus.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public class LineChart extends CryptoChart {

    private final TradingRecord tradingRecord;
    private final Map<String, CachedIndicator<Num>> indicatorMap;

    public LineChart(
            BarSeries series,
            Map<String, CachedIndicator<Num>> indicatorMap,
            TradingRecord tradingRecord) {
        super(series);

        this.indicatorMap = indicatorMap;
        this.tradingRecord = tradingRecord;
    }

    public JFreeChart create() {
        TimeSeriesCollection axis = new TimeSeriesCollection();
        this.indicatorMap.forEach((name, indicator) -> axis.addSeries(createChartData(name, indicator)));

        JFreeChart chart = ChartFactory.createTimeSeriesChart(null, null,
                null, axis, true, true, false);

        setDateAxisFormat(chart.getXYPlot());
        addSignals(chart.getXYPlot(), this.tradingRecord);

        return chart;
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
