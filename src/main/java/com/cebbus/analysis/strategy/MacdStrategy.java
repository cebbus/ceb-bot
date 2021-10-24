package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class MacdStrategy extends BaseCebStrategy {

    public MacdStrategy(BarSeries series) {
        super(series);
    }

    @Override
    BuilderResult build() {
        ClosePriceIndicator cpi = new ClosePriceIndicator(this.series);

        MACDIndicator macd = new MACDIndicator(cpi);
        EMAIndicator signal = new EMAIndicator(macd, 9);

        Rule entryRule = new OverIndicatorRule(macd, signal)
                .and(new OverIndicatorRule(macd, 0));

        Rule exitRule = new UnderIndicatorRule(macd, signal);

        BaseStrategy strategy = new BaseStrategy("MACD", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("MACD", new LinkedHashMap<>());
        indicators.get("MACD").put("MACD", macd);
        indicators.get("MACD").put("EMA (9)", signal);

        return new BuilderResult(strategy, indicators);
    }
}