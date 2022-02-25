package com.cebbus.view.chart;

import com.cebbus.analysis.TheOracle;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public class CryptoChartFactory {

    private final TheOracle theOracle;

    public CryptoChartFactory(TheOracle theOracle) {
        this.theOracle = theOracle;
    }

    public CryptoChart newLineChart(Map<String, CachedIndicator<Num>> indicatorMap) {
        return new LineChart(this.theOracle, indicatorMap);
    }

    public CryptoChart newCandlestickChart() {
        return new CandlestickChart(this.theOracle);
    }
}
