package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.List;
import java.util.Map;

public abstract class BaseCebStrategy implements CebStrategy {

    static final String CPI_KEY = "CPI";
    static final String CCI_KEY = "CCI";
    static final String ADX_KEY = "ADX";
    static final String MACD_KEY = "MACD";
    static final String STO_KEY = "STO";
    static final String OBV_KEY = "OBV";
    static final String RSI_KEY = "RSI";
    static final String ADX_PARAM_KEY = "ADX (%s)";
    static final String EMA_PARAM_KEY = "EMA (%s)";
    static final String SMA_PARAM_KEY = "SMA (%s)";
    static final String CCI_PARAM_KEY = "CCI (%s)";
    static final String RSI_PARAM_KEY = "RSI (%s)";

    final BarSeries series;

    Number[] parameters;
    BuilderResult builderResult;

    BaseCebStrategy(BarSeries series, Number[] parameters) {
        this.series = series;
        this.parameters = parameters;
    }

    @Override
    public void rebuild(Number... parameters) {
        this.parameters = parameters;
        build();
    }

    @Override
    public boolean shouldEnter(TradingRecord tradingRecord) {
        return getStrategy().shouldEnter(this.series.getEndIndex(), tradingRecord);
    }

    @Override
    public boolean shouldExit(TradingRecord tradingRecord) {
        return getStrategy().shouldExit(this.series.getEndIndex(), tradingRecord);
    }

    @Override
    public CebStrategy and(CebStrategy other) {
        return CombinedStrategy.combine(this.series, List.of(this, other), true);
    }

    @Override
    public CebStrategy or(CebStrategy other) {
        return CombinedStrategy.combine(this.series, List.of(this, other), false);
    }

    @Override
    public BarSeries getSeries() {
        return this.series;
    }

    @Override
    public Strategy getStrategy() {
        return this.builderResult.getStrategy();
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return this.builderResult.getIndicators();
    }

    @Override
    public Number[] getParameters() {
        return parameters;
    }

}
