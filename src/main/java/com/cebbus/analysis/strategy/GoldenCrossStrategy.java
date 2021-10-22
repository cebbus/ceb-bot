package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class GoldenCrossStrategy extends BaseCebStrategy {

    public GoldenCrossStrategy(BarSeries series) {
        super(series);
    }

    @Override
    BuilderResult build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 50);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule = new CrossedDownIndicatorRule(shortSma, longSma);

        BaseStrategy strategy = new BaseStrategy("Golden Cross", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put("SMA (50)", shortSma);
        indicators.get("CPI").put("SMA (200)", longSma);

        return new BuilderResult(strategy, indicators);
    }
}
