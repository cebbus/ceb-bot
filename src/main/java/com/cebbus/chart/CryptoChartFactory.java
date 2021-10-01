package com.cebbus.chart;

import org.jfree.chart.JFreeChart;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public class CryptoChartFactory {

    private final BarSeries series;
    private final TradingRecord tradingRecord;

    public CryptoChartFactory(BarSeries series, TradingRecord tradingRecord) {
        this.series = series;
        this.tradingRecord = tradingRecord;
    }

    public JFreeChart newLineChart(Map<String, CachedIndicator<Num>> indicatorMap) {
        LineChart lineChart = new LineChart(this.series, indicatorMap, this.tradingRecord);
        return lineChart.create();
    }

    public JFreeChart newCandlestickChart() {
        return new CandlestickChart(this.series).create();
    }
}
