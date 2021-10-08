package com.cebbus.analysis.strategy;

import com.cebbus.analysis.rule.ForceToWinRule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
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
                .and(new CrossedUpIndicatorRule(shortSma, longSma));

        Rule exitRule = new UnderIndicatorRule(shortSma, middleSma)
                .and(new CrossedDownIndicatorRule(shortSma, longSma))
                .and(new ForceToWinRule(closePrice, 0.1));

        BaseStrategy strategy = new BaseStrategy("Scalping", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("SMA", Map.of(
                "Short SMA", shortSma,
                "Middle SMA", middleSma,
                "Long SMA", longSma)
        );

        indicators.put("CPI", Map.of("CPI", closePrice));

        return new BuilderResult(strategy, indicators);
    }

}
