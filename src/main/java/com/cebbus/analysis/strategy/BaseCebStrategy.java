package com.cebbus.analysis.strategy;

import lombok.Data;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public abstract class BaseCebStrategy implements CebStrategy {

    final BarSeries series;
    final BuilderResult builderResult;

    BaseCebStrategy(BarSeries series) {
        this.series = series;
        this.builderResult = build();
    }

    abstract BuilderResult build();

    @Override
    public Strategy getStrategy() {
        return this.builderResult.strategy;
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return this.builderResult.indicators;
    }

    @Data
    static final class BuilderResult {
        private final Strategy strategy;
        private final Map<String, Map<String, CachedIndicator<Num>>> indicators;
    }

}
