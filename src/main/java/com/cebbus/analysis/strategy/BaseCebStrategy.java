package com.cebbus.analysis.strategy;

import lombok.Data;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.LinkedHashMap;
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
    public CebStrategy and(CebStrategy other) {
        Strategy strategy = this.getStrategy().and(other.getStrategy());
        return createNewStrategy(strategy, concatIndicators(other));
    }

    @Override
    public CebStrategy or(CebStrategy other) {
        Strategy strategy = this.getStrategy().or(other.getStrategy());
        return createNewStrategy(strategy, concatIndicators(other));
    }

    @Override
    public Strategy getStrategy() {
        return this.builderResult.strategy;
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return this.builderResult.indicators;
    }

    private CebStrategy createNewStrategy(Strategy strategy, Map<String, Map<String, CachedIndicator<Num>>> indicators) {
        return new BaseCebStrategy(this.series) {
            @Override
            BuilderResult build() {
                return new BuilderResult(strategy, indicators);
            }
        };
    }

    private Map<String, Map<String, CachedIndicator<Num>>> concatIndicators(CebStrategy other) {
        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>(this.getIndicators());

        this.getIndicators().forEach((k, v) -> {
            indicators.put(k, new LinkedHashMap<>());
            indicators.get(k).putAll(v);
        });

        other.getIndicators().forEach((k, v) -> {
            if (indicators.containsKey(k)) {
                indicators.get(k).putAll(v);
            } else {
                indicators.put(k, v);
            }
        });
        indicators.putAll(other.getIndicators());

        return indicators;
    }

    @Data
    static final class BuilderResult {
        private final Strategy strategy;
        private final Map<String, Map<String, CachedIndicator<Num>>> indicators;
    }

}
