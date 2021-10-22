package com.cebbus.analysis.strategy;

import com.cebbus.analysis.rule.BackwardUnderIndicatorRule;
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

public class DummyStrategy extends BaseCebStrategy {

    public DummyStrategy(BarSeries series) {
        super(series);
    }

    @Override
    public BuilderResult build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(closePrice, 50);

        Rule entryRule = new OverIndicatorRule(closePrice, sma)
                .and(new BackwardUnderIndicatorRule(closePrice, sma, 21));

        Rule exitRule = new UnderIndicatorRule(closePrice, sma);

        BaseStrategy strategy = new BaseStrategy("Dummy", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put("SMA (50)", sma);

        return new BuilderResult(strategy, indicators);
    }

}
