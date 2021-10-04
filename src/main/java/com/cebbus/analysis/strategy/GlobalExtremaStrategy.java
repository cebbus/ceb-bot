package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalExtremaStrategy implements CebStrategy {

    private final BarSeries series;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

    public GlobalExtremaStrategy(BarSeries series) {
        this.series = series;
    }

    @Override
    public Strategy build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);

        HighPriceIndicator highPrice = new HighPriceIndicator(this.series);
        HighestValueIndicator weekHighPrice = new HighestValueIndicator(highPrice, 2016);
        TransformIndicator upWeek = TransformIndicator.multiply(weekHighPrice, 0.996D);

        LowPriceIndicator lowPrice = new LowPriceIndicator(this.series);
        LowestValueIndicator weekLowPrice = new LowestValueIndicator(lowPrice, 2016);
        TransformIndicator downWeek = TransformIndicator.multiply(weekLowPrice, 1.004D);

        Rule entryRule = new UnderIndicatorRule(closePrice, downWeek);
        Rule exitRule = new OverIndicatorRule(closePrice, upWeek);

        this.indicators.put("CHL", Map.of(
                "CPI", closePrice,
                "HPI", highPrice,
                "LPI", lowPrice)
        );

        this.indicators.put("TR", Map.of(
                "CPI", closePrice,
                "Week High", weekHighPrice,
                "Up Week", upWeek,
                "Week Low", weekLowPrice,
                "Down Week", downWeek)
        );

        return new BaseStrategy("Global Extrema", entryRule, exitRule);
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return indicators;
    }
}
