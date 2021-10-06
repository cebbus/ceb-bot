package com.cebbus.analysis.strategy;

import com.cebbus.analysis.rule.BackwardUnderIndicatorRule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class DummyStrategy implements CebStrategy {

    private final BarSeries series;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

    public DummyStrategy(BarSeries series) {
        this.series = series;
    }

    @Override
    public Strategy build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(closePrice, 50);

        Rule entryRule = new OverIndicatorRule(closePrice, sma)
                .and(new BackwardUnderIndicatorRule(closePrice, sma, 21));

        Rule exitRule = new UnderIndicatorRule(closePrice, sma);

        this.indicators.put("CPI-SMA", Map.of(
                "CPI", closePrice,
                "SMA", sma)
        );

        return new BaseStrategy(entryRule, exitRule);
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return indicators;
    }
}
