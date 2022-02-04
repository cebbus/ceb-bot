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
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Short - Middle term strategy
 */
public class ScalpingStrategy extends BaseCebStrategy {

    public ScalpingStrategy(BarSeries series) {
        super(series, new Number[]{5, 8, 13});
        build();
    }

    @Override
    public void build() {
        int shortBarCount = this.parameters[0].intValue();
        int middleBarCount = this.parameters[1].intValue();
        int longBarCount = this.parameters[2].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, shortBarCount);
        SMAIndicator middleSma = new SMAIndicator(closePrice, middleBarCount);
        SMAIndicator longSma = new SMAIndicator(closePrice, longBarCount);

        Rule entryRule = new OverIndicatorRule(shortSma, middleSma)
                .and(new OverIndicatorRule(shortSma, longSma));

        Rule exitRule = new UnderIndicatorRule(shortSma, middleSma)
                .and(new UnderIndicatorRule(shortSma, longSma));

        BaseStrategy strategy = new BaseStrategy("Scalping", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put(CPI_KEY, new LinkedHashMap<>());
        indicators.get(CPI_KEY).put(CPI_KEY, closePrice);
        indicators.get(CPI_KEY).put(String.format(SMA_PARAM_KEY, shortBarCount), shortSma);
        indicators.get(CPI_KEY).put(String.format(SMA_PARAM_KEY, middleBarCount), middleSma);
        indicators.get(CPI_KEY).put(String.format(SMA_PARAM_KEY, longBarCount), longSma);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene shortBarCount = new IntegerGene(conf, 1, 5);
        IntegerGene middleBarCount = new IntegerGene(conf, 6, 10);
        IntegerGene longBarCount = new IntegerGene(conf, 11, 20);

        return new Gene[]{shortBarCount, middleBarCount, longBarCount};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("Short SMA Bar Count", this.parameters[0]);
        map.put("Middle SMA Bar Count", this.parameters[1]);
        map.put("Long SMA Bar Count", this.parameters[2]);

        return map;
    }

}
