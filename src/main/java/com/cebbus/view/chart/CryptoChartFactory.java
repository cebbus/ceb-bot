package com.cebbus.view.chart;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public class CryptoChartFactory {

    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;

    public CryptoChartFactory(
            BarSeries series,
            TradingRecord tradingRecord,
            TradingRecord backtestRecord) {
        this.series = series;
        this.tradingRecord = tradingRecord;
        this.backtestRecord = backtestRecord;
    }

    public CryptoChart newLineChart(Map<String, CachedIndicator<Num>> indicatorMap) {
        return new LineChart(this.series, indicatorMap, this.tradingRecord, this.backtestRecord);
    }

    public CryptoChart newCandlestickChart() {
        return new CandlestickChart(this.series);
    }
}
