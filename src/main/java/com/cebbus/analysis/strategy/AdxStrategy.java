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
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdxStrategy extends BaseCebStrategy {

    public AdxStrategy(BarSeries series) {
        super(series, new Number[]{50, 14, 20});
        build();
    }

    @Override
    public void build() {
        int smaBarCount = this.parameters[0].intValue();
        int adxBarCount = this.parameters[1].intValue();
        int adxThreshold = this.parameters[2].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(closePrice, smaBarCount);

        ADXIndicator adxIndicator = new ADXIndicator(this.series, adxBarCount);
        PlusDIIndicator plusDIIndicator = new PlusDIIndicator(this.series, adxBarCount);
        MinusDIIndicator minusDIIndicator = new MinusDIIndicator(this.series, adxBarCount);

        Rule entryRule = new OverIndicatorRule(adxIndicator, adxThreshold)
                .and(new OverIndicatorRule(plusDIIndicator, minusDIIndicator))
                .and(new OverIndicatorRule(closePrice, sma));

        Rule exitRule = new OverIndicatorRule(adxIndicator, adxThreshold)
                .and(new UnderIndicatorRule(plusDIIndicator, minusDIIndicator))
                .and(new UnderIndicatorRule(closePrice, sma));

        BaseStrategy strategy = new BaseStrategy("ADX", entryRule, exitRule, adxBarCount);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("ADX", new LinkedHashMap<>());
        indicators.get("ADX").put(String.format("ADX (%s)", adxBarCount), adxIndicator);
        indicators.get("ADX").put(String.format("Minus DI (%s)", adxBarCount), minusDIIndicator);
        indicators.get("ADX").put(String.format("Plus DI (%s)", adxBarCount), plusDIIndicator);

        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put(String.format("SMA (%s)", smaBarCount), sma);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene smaBarCount = new IntegerGene(conf, 20, 75);
        IntegerGene adxBarCount = new IntegerGene(conf, 10, 50);
        IntegerGene adxThreshold = new IntegerGene(conf, 10, 30);

        return new Gene[]{smaBarCount, adxBarCount, adxThreshold};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("SMA Bar Count", this.parameters[0]);
        map.put("ADX Bar Count", this.parameters[1]);
        map.put("ADX Threshold", this.parameters[2]);

        return map;
    }
}
