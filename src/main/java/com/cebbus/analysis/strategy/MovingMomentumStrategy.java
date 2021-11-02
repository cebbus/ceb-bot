package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
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

public class MovingMomentumStrategy extends BaseCebStrategy {

    public MovingMomentumStrategy(BarSeries series) {
        super(series, new Number[0]);
    }

    @Override
    public void build() {
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

        BaseStrategy strategy = new BaseStrategy("Moving Momentum", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put("EMA (9)", shortEma);
        indicators.get("CPI").put("EMA (26)", longEma);

        indicators.put("MACD", new LinkedHashMap<>());
        indicators.get("MACD").put("MACD", macd);
        indicators.get("MACD").put("EMA (18)", emaMacd);

        indicators.put("STO", new LinkedHashMap<>());
        indicators.get("STO").put("STO", stochasticOscillK);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    //TODO optimization not implemented
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        return new Gene[0];
    }

}
