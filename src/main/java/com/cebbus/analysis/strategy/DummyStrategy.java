package com.cebbus.analysis.strategy;

import com.cebbus.analysis.rule.BackwardUnderIndicatorRule;
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

public class DummyStrategy extends BaseCebStrategy {

    public DummyStrategy(BarSeries series) {
        super(series, new Number[]{50, 21});
        build();
    }

    @Override
    public void build() {
        int smaBarCount = this.parameters[0].intValue();
        int backwardUnderThreshold = this.parameters[1].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(closePrice, smaBarCount);

        Rule entryRule = new OverIndicatorRule(closePrice, sma)
                .and(new BackwardUnderIndicatorRule(closePrice, sma, backwardUnderThreshold));

        Rule exitRule = new UnderIndicatorRule(closePrice, sma);

        BaseStrategy strategy = new BaseStrategy("Dummy", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);
        indicators.get("CPI").put(String.format("SMA (%s)", smaBarCount), sma);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene smaBarCount = new IntegerGene(conf, 10, 100);
        IntegerGene backwardUnderThreshold = new IntegerGene(conf, 10, 50);

        return new Gene[]{smaBarCount, backwardUnderThreshold};
    }
}
