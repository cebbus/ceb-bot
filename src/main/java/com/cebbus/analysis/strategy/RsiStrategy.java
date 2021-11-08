package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.IntegerGene;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class RsiStrategy extends BaseCebStrategy {

    public RsiStrategy(BarSeries series) {
        super(series, new Number[]{14, 30, 70});
        build();
    }

    @Override
    public void build() {
        int rsiBarCount = this.parameters[0].intValue();
        int rsiBuyThreshold = this.parameters[1].intValue();
        int rsiSellThreshold = this.parameters[2].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        RSIIndicator rsi = new RSIIndicator(closePrice, rsiBarCount);

        Rule entryRule = new CrossedDownIndicatorRule(rsi, rsiBuyThreshold);
        Rule exitRule = new CrossedUpIndicatorRule(rsi, rsiSellThreshold);

        BaseStrategy strategy = new BaseStrategy("RSI", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);

        indicators.put("RSI", Map.of(String.format("RSI (%s)", rsiBarCount), rsi));

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene rsiBarCount = new IntegerGene(conf, 10, 50);
        IntegerGene entryThreshold = new IntegerGene(conf, 1, 50);
        IntegerGene exitThreshold = new IntegerGene(conf, 51, 100);

        return new Gene[]{rsiBarCount, entryThreshold, exitThreshold};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("RSI Bar Count", this.parameters[0]);
        map.put("RSI Buy Threshold", this.parameters[1]);
        map.put("RSI Sell Threshold", this.parameters[2]);

        return map;
    }
}
