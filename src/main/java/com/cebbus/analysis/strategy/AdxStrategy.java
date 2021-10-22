package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdxStrategy extends BaseCebStrategy {

    public AdxStrategy(BarSeries series) {
        super(series);
    }

    @Override
    public BuilderResult build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(closePrice, 50);

        ADXIndicator adxIndicator = new ADXIndicator(this.series, 14);
        PlusDIIndicator plusDIIndicator = new PlusDIIndicator(this.series, 14);
        MinusDIIndicator minusDIIndicator = new MinusDIIndicator(this.series, 14);

        Rule entryRule = new OverIndicatorRule(adxIndicator, 20)
                .and(new OverIndicatorRule(plusDIIndicator, minusDIIndicator))
                .and(new OverIndicatorRule(closePrice, sma));

        Rule exitRule = new OverIndicatorRule(adxIndicator, 20)
                .and(new UnderIndicatorRule(plusDIIndicator, minusDIIndicator))
                .and(new UnderIndicatorRule(closePrice, sma));

        BaseStrategy strategy = new BaseStrategy("ADX", entryRule, exitRule, 14);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("ADX", new LinkedHashMap<>());
        indicators.get("ADX").put("ADX (14)", adxIndicator);
        indicators.get("ADX").put("Minus DI (14)", minusDIIndicator);
        indicators.get("ADX").put("Plus DI (14)", plusDIIndicator);

        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put("SMA (50)", sma);

        return new BuilderResult(strategy, indicators);
    }
}
