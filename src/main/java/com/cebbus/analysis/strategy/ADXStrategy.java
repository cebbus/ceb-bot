package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
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

public class ADXStrategy implements CebStrategy {

    private final BarSeries series;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

    public ADXStrategy(BarSeries series) {
        this.series = series;
    }

    @Override
    public Strategy build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(closePrice, 50);

        ADXIndicator adxIndicator = new ADXIndicator(this.series, 14);
        PlusDIIndicator plusDIIndicator = new PlusDIIndicator(this.series, 14);
        MinusDIIndicator minusDIIndicator = new MinusDIIndicator(this.series, 14);

        Rule entryRule = new OverIndicatorRule(adxIndicator, 20)
                .and(new CrossedUpIndicatorRule(plusDIIndicator, minusDIIndicator))
                .and(new OverIndicatorRule(closePrice, sma));

        Rule exitRule = new OverIndicatorRule(adxIndicator, 20)
                .and(new CrossedDownIndicatorRule(plusDIIndicator, minusDIIndicator))
                .and(new UnderIndicatorRule(closePrice, sma));

        this.indicators.put("CPI-SMA", Map.of(
                "CPI", closePrice,
                "SMA", sma)
        );

        this.indicators.put("ADX - DI", Map.of(
                "ADX", adxIndicator,
                "Plus DI", plusDIIndicator,
                "Minus DI", minusDIIndicator)
        );

        return new BaseStrategy("ADX", entryRule, exitRule, 14);
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return indicators;
    }
}
