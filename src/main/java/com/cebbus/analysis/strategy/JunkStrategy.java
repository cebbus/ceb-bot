package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.BooleanRule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class JunkStrategy extends BaseCebStrategy {

    public JunkStrategy(BarSeries series) {
        super(series, new Number[0]);
        build();
    }

    @Override
    public void build() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);

        Rule entryRule = new BooleanRule(false);
        Rule exitRule = new BooleanRule(false);

        BaseStrategy strategy = new BaseStrategy("JUNK", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        return new Gene[0];
    }

    @Override
    public Map<String, Number> getParameterMap() {
        return Collections.emptyMap();
    }
}
