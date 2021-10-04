package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class CciCorrectionStrategy implements CebStrategy {

    private final BarSeries series;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

    public CciCorrectionStrategy(BarSeries series) {
        this.series = series;
    }

    @Override
    public Strategy build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);

        CCIIndicator longCci = new CCIIndicator(this.series, 200);
        CCIIndicator shortCci = new CCIIndicator(this.series, 5);
        Num plus100 = this.series.numOf(100);
        Num minus100 = this.series.numOf(-100);

        Rule entryRule = new OverIndicatorRule(longCci, plus100)
                .and(new UnderIndicatorRule(shortCci, minus100));

        Rule exitRule = new UnderIndicatorRule(longCci, minus100)
                .and(new OverIndicatorRule(shortCci, plus100));

        Strategy strategy = new BaseStrategy("CCI", entryRule, exitRule);
        strategy.setUnstablePeriod(5);

        this.indicators.put("CCI", Map.of(
                "CPI", closePrice,
                "Short CCI", shortCci,
                "Long CCI", longCci)
        );

        return strategy;
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return indicators;
    }
}
