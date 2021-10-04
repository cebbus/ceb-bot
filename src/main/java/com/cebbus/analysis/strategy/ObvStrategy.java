package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObvStrategy implements CebStrategy {

    private final BarSeries series;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

    public ObvStrategy(BarSeries series) {
        this.series = series;
    }

    @Override
    public Strategy build() {
        ClosePriceIndicator cpi = new ClosePriceIndicator(this.series);
        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(obv, 21);

        Rule entryRule = new CrossedUpIndicatorRule(obv, sma);
        //Rule entryRule = new OverIndicatorRule(sma, obv);
        Rule exitRule = new CrossedDownIndicatorRule(obv, sma);
        //Rule exitRule = new UnderIndicatorRule(sma, obv);

        this.indicators.put("OBV", Map.of(
                "OBV", obv,
                "SMA", sma)
        );

        this.indicators.put("CPI", Map.of("CPI", cpi));

        return new BaseStrategy("OBV", entryRule, exitRule);
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return indicators;
    }
}
