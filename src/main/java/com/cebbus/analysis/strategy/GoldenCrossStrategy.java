package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.IntegerGene;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class GoldenCrossStrategy extends BaseCebStrategy {

    public GoldenCrossStrategy(BarSeries series) {
        super(series, new Number[]{50, 200});
        build();
    }

    @Override
    public void build() {
        int shortBarCount = this.parameters[0].intValue();
        int longBarCount = this.parameters[1].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, shortBarCount);
        SMAIndicator longSma = new SMAIndicator(closePrice, longBarCount);

        Rule entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        Rule exitRule = new CrossedDownIndicatorRule(shortSma, longSma);

        BaseStrategy strategy = new BaseStrategy("Golden Cross", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put(CPI_KEY, new LinkedHashMap<>());
        indicators.get(CPI_KEY).put(CPI_KEY, closePrice);
        indicators.get(CPI_KEY).put(String.format(SMA_PARAM_KEY, shortBarCount), shortSma);
        indicators.get(CPI_KEY).put(String.format(SMA_PARAM_KEY, longBarCount), longSma);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene shortBarCount = new IntegerGene(conf, 25, 75);
        IntegerGene longBarCount = new IntegerGene(conf, 100, 300);

        return new Gene[]{shortBarCount, longBarCount};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("Short SMA Bar Count", this.parameters[0]);
        map.put("Long SMA Bar Count", this.parameters[1]);

        return map;
    }
}
