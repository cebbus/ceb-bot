package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Middle - Long term strategy
 */
public class ObvStrategy extends BaseCebStrategy {

    public ObvStrategy(BarSeries series) {
        super(series);
    }

    @Override
    public BuilderResult build() {
        ClosePriceIndicator cpi = new ClosePriceIndicator(this.series);
        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(obv, 21);

        Rule entryRule = new CrossedUpIndicatorRule(obv, sma);
        Rule exitRule = new CrossedDownIndicatorRule(obv, sma);

        BaseStrategy strategy = new BaseStrategy("OBV", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", Map.of("CPI", cpi));

        indicators.put("OBV", new LinkedHashMap<>());
        indicators.get("OBV").put("OBV", obv);
        indicators.get("OBV").put("SMA (21)", sma);

        return new BuilderResult(strategy, indicators);
    }

}
