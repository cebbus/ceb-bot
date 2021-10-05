package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Short - Middle term strategy
 */
public class ScalpingStrategy implements CebStrategy {

    private final BarSeries series;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

    public ScalpingStrategy(BarSeries series) {
        this.series = series;
    }

    @Override
    public Strategy build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);

        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator middleSma = new SMAIndicator(closePrice, 8);
        SMAIndicator longSma = new SMAIndicator(closePrice, 13);

        Rule entryRule = new OverIndicatorRule(shortSma, middleSma)
                .and(new CrossedUpIndicatorRule(shortSma, longSma));

        Rule exitRule = new UnderIndicatorRule(shortSma, middleSma)
                .and(new CrossedDownIndicatorRule(shortSma, longSma));

        this.indicators.put("SMA", Map.of(
                "Short SMA", shortSma,
                "Middle SMA", middleSma,
                "Long SMA", longSma)
        );

        this.indicators.put("CPI", Map.of("CPI", closePrice));

        return new BaseStrategy("Scalping", entryRule, exitRule);
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return indicators;
    }
}
