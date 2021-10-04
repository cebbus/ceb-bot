package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class MovingMomentumStrategy implements CebStrategy {

    private final BarSeries series;
    private final Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

    public MovingMomentumStrategy(BarSeries series) {
        this.series = series;
    }

    @Override
    public Strategy build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
        EMAIndicator longEma = new EMAIndicator(closePrice, 26);

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(this.series, 14);
        MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 18);

        Rule entryRule = new OverIndicatorRule(shortEma, longEma)
                .and(new CrossedDownIndicatorRule(stochasticOscillK, 20))
                .and(new OverIndicatorRule(macd, emaMacd));

        Rule exitRule = new UnderIndicatorRule(shortEma, longEma)
                .and(new CrossedUpIndicatorRule(stochasticOscillK, 80))
                .and(new UnderIndicatorRule(macd, emaMacd));

        this.indicators.put("CPI-SMA", Map.of(
                "CPI", closePrice,
                "Short EMA", shortEma,
                "Long EMA", longEma)
        );

        this.indicators.put("STO", Map.of("STO", stochasticOscillK));

        this.indicators.put("MACD", Map.of(
                "MACD", macd,
                "EMA MACD", emaMacd)
        );

        return new BaseStrategy("Moving Momentum", entryRule, exitRule);
    }

    @Override
    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return indicators;
    }
}
