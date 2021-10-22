package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Short - Middle term strategy
 */
public class ScalpingStrategy extends BaseCebStrategy {

    public ScalpingStrategy(BarSeries series) {
        super(series);
    }

    @Override
    public BuilderResult build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator middleSma = new SMAIndicator(closePrice, 8);
        SMAIndicator longSma = new SMAIndicator(closePrice, 13);

        Rule entryRule = new OverIndicatorRule(shortSma, middleSma)
                .and(new OverIndicatorRule(shortSma, longSma));

        Rule exitRule = new UnderIndicatorRule(shortSma, middleSma)
                .and(new UnderIndicatorRule(shortSma, longSma));

        BaseStrategy strategy = new BaseStrategy("Scalping", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put("SMA (5)", shortSma);
        indicators.get("CPI").put("SMA (8)", middleSma);
        indicators.get("CPI").put("SMA (13)", longSma);

        return new BuilderResult(strategy, indicators);
    }

}
