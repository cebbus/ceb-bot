package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalExtremaStrategy extends BaseCebStrategy {

    public GlobalExtremaStrategy(BarSeries series) {
        super(series, new Number[]{7, 0.996D, 1.004D});
        build();
    }

    @Override
    public void build() {
        int priceBarCount = this.parameters[0].intValue();
        double highCoefficient = this.parameters[1].doubleValue();
        double lowCoefficient = this.parameters[2].doubleValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);

        HighPriceIndicator highPrice = new HighPriceIndicator(this.series);
        HighestValueIndicator weekHighPrice = new HighestValueIndicator(highPrice, priceBarCount);
        TransformIndicator upWeek = TransformIndicator.multiply(weekHighPrice, highCoefficient);

        LowPriceIndicator lowPrice = new LowPriceIndicator(this.series);
        LowestValueIndicator weekLowPrice = new LowestValueIndicator(lowPrice, priceBarCount);
        TransformIndicator downWeek = TransformIndicator.multiply(weekLowPrice, lowCoefficient);

        Rule entryRule = new UnderIndicatorRule(closePrice, downWeek);
        Rule exitRule = new OverIndicatorRule(closePrice, upWeek);

        BaseStrategy strategy = new BaseStrategy("Global Extrema", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put("Up Week", upWeek);
        indicators.get("CPI").put("Down Week", downWeek);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene priceBarCount = new IntegerGene(conf, 1, 30);
        DoubleGene highCoefficient = new DoubleGene(conf, 0.990, 0.999);
        DoubleGene lowCoefficient = new DoubleGene(conf, 1.001, 1.009);

        return new Gene[]{priceBarCount, highCoefficient, lowCoefficient};
    }

}
