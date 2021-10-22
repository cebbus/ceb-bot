package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalExtremaStrategy extends BaseCebStrategy {

    public GlobalExtremaStrategy(BarSeries series) {
        super(series);
    }

    @Override
    public BuilderResult build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);

        HighPriceIndicator highPrice = new HighPriceIndicator(this.series);
        HighestValueIndicator weekHighPrice = new HighestValueIndicator(highPrice, 7);
        TransformIndicator upWeek = TransformIndicator.multiply(weekHighPrice, 0.996D);

        LowPriceIndicator lowPrice = new LowPriceIndicator(this.series);
        LowestValueIndicator weekLowPrice = new LowestValueIndicator(lowPrice, 7);
        TransformIndicator downWeek = TransformIndicator.multiply(weekLowPrice, 1.004D);

        Rule entryRule = new UnderIndicatorRule(closePrice, downWeek);
        Rule exitRule = new OverIndicatorRule(closePrice, upWeek);

        BaseStrategy strategy = new BaseStrategy("Global Extrema", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put("Up Week", upWeek);
        indicators.get("CPI").put("Down Week", downWeek);

        return new BuilderResult(strategy, indicators);
    }

}
