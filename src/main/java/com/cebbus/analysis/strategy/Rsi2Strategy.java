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
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class Rsi2Strategy extends BaseCebStrategy {

    public Rsi2Strategy(BarSeries series) {
        super(series, new Number[]{2, 5, 95, 5, 200});
        build();
    }

    @Override
    public void build() {
        int rsiBarCount = this.parameters[0].intValue();
        int rsiBuyThreshold = this.parameters[1].intValue();
        int rsiSellThreshold = this.parameters[2].intValue();
        int shortSmaBarCount = this.parameters[3].intValue();
        int longSmaBarCount = this.parameters[4].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, shortSmaBarCount);
        SMAIndicator longSma = new SMAIndicator(closePrice, longSmaBarCount);

        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, rsiBarCount);

        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedDownIndicatorRule(rsi, rsiBuyThreshold)) // Signal 1
                .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2

        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedUpIndicatorRule(rsi, rsiSellThreshold)) // Signal 1
                .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2

        BaseStrategy strategy = new BaseStrategy("RSI 2", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put(String.format("SMA (%s)", shortSmaBarCount), shortSma);
        indicators.get("CPI").put(String.format("SMA (%s)", longSmaBarCount), longSma);

        indicators.put("RSI", Map.of("RSI", rsi));

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene rsiBarCount = new IntegerGene(conf, 1, 10);
        IntegerGene entryThreshold = new IntegerGene(conf, 1, 10);
        IntegerGene exitThreshold = new IntegerGene(conf, 75, 100);
        IntegerGene shortSmaBarCount = new IntegerGene(conf, 1, 10);
        IntegerGene longSmaBarCount = new IntegerGene(conf, 150, 300);

        return new Gene[]{rsiBarCount, entryThreshold, exitThreshold, shortSmaBarCount, longSmaBarCount};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("RSI Bar Count", this.parameters[0]);
        map.put("RSI Buy Threshold", this.parameters[1]);
        map.put("RSI Sell Threshold", this.parameters[2]);
        map.put("Short SMA Bar Count", this.parameters[3]);
        map.put("Long SMA Bar Count", this.parameters[4]);

        return map;
    }

}
