package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.IntegerGene;
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
        super(series, new Number[]{9, 26, 14, 18, 20, 80});
        build();
    }

    @Override
    public void build() {
        int shortEmaBarCount = this.parameters[0].intValue();
        int longEmaBarCount = this.parameters[1].intValue();
        int stoBarCount = this.parameters[2].intValue();
        int macdEmaBarCount = this.parameters[3].intValue();
        int stocBuyThreshold = this.parameters[4].intValue();
        int stocSellThreshold = this.parameters[5].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        EMAIndicator shortEma = new EMAIndicator(closePrice, shortEmaBarCount);
        EMAIndicator longEma = new EMAIndicator(closePrice, longEmaBarCount);

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(this.series, stoBarCount);

        MACDIndicator macd = new MACDIndicator(closePrice, shortEmaBarCount, longEmaBarCount);
        EMAIndicator emaMacd = new EMAIndicator(macd, macdEmaBarCount);

        Rule entryRule = new OverIndicatorRule(shortEma, longEma)
                .and(new CrossedDownIndicatorRule(stochasticOscillK, stocBuyThreshold))
                .and(new OverIndicatorRule(macd, emaMacd));

        Rule exitRule = new UnderIndicatorRule(shortEma, longEma)
                .and(new CrossedUpIndicatorRule(stochasticOscillK, stocSellThreshold))
                .and(new UnderIndicatorRule(macd, emaMacd));

        BaseStrategy strategy = new BaseStrategy("Moving Momentum", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put(String.format("EMA (%s)", shortEmaBarCount), shortEma);
        indicators.get("CPI").put(String.format("EMA (%s)", longEmaBarCount), longEma);

        indicators.put("MACD", new LinkedHashMap<>());
        indicators.get("MACD").put("MACD", macd);
        indicators.get("MACD").put(String.format("EMA (%s)", macdEmaBarCount), emaMacd);

        indicators.put("STO", new LinkedHashMap<>());
        indicators.get("STO").put("STO", stochasticOscillK);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene shortEmaBarCount = new IntegerGene(conf, 1, 10);
        IntegerGene longEmaBarCount = new IntegerGene(conf, 10, 30);
        IntegerGene stoBarCount = new IntegerGene(conf, 10, 20);
        IntegerGene macdEmaBarCount = new IntegerGene(conf, 10, 20);
        IntegerGene stocBuyThreshold = new IntegerGene(conf, 10, 30);
        IntegerGene stocSellThreshold = new IntegerGene(conf, 70, 100);

        return new Gene[]{shortEmaBarCount, longEmaBarCount, stoBarCount, macdEmaBarCount, stocBuyThreshold, stocSellThreshold};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("Short EMA Bar Count", this.parameters[0]);
        map.put("Long EMA Bar Count", this.parameters[1]);
        map.put("Stochastic Oscillator Bar Count", this.parameters[2]);
        map.put("MACD EMA Bar Count", this.parameters[3]);
        map.put("Stochastic Oscillator Buy Threshold", this.parameters[4]);
        map.put("Stochastic Oscillator Sell Threshold", this.parameters[5]);

        return map;
    }

}
