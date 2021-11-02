package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.List;
import java.util.Map;

public abstract class BaseCebStrategy implements CebStrategy {

    final BarSeries series;

    Number[] parameters;
    BuilderResult builderResult;

    BaseCebStrategy(BarSeries series, Number[] parameters) {
        this.series = series;
        this.parameters = parameters;
        build();
    }

    @Override
    public void rebuild(Number... parameters) {
        this.parameters = parameters;
        build();
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
