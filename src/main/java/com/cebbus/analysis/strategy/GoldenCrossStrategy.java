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
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put(String.format("SMA (%s)", shortBarCount), shortSma);
        indicators.get("CPI").put(String.format("SMA (%s)", longBarCount), longSma);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene shortBarCount = new IntegerGene(conf, 25, 75);
        IntegerGene longBarCount = new IntegerGene(conf, 100, 300);

        return new Gene[]{shortBarCount, longBarCount};
    }
}
