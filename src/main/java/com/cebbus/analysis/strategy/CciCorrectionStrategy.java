package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.IntegerGene;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class CciCorrectionStrategy extends BaseCebStrategy {

    public CciCorrectionStrategy(BarSeries series) {
        super(series, new Number[]{200, 5});
        build();
    }

    @Override
    public void build() {
        int longCciBarCount = this.parameters[0].intValue();
        int shortCciBarCount = this.parameters[1].intValue();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);

        CCIIndicator longCci = new CCIIndicator(this.series, longCciBarCount);
        CCIIndicator shortCci = new CCIIndicator(this.series, shortCciBarCount);
        Num plus100 = this.series.numOf(100);
        Num minus100 = this.series.numOf(-100);

        Rule entryRule = new OverIndicatorRule(longCci, plus100)
                .and(new UnderIndicatorRule(shortCci, minus100));

        Rule exitRule = new UnderIndicatorRule(longCci, minus100)
                .and(new OverIndicatorRule(shortCci, plus100));

        Strategy strategy = new BaseStrategy("CCI", entryRule, exitRule, 5);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("CPI", closePrice);

        indicators.put("CCI", new LinkedHashMap<>());
        indicators.get("CCI").put(String.format("CCI (%s)", shortCciBarCount), shortCci);
        indicators.get("CCI").put(String.format("CCI (%s)", longCciBarCount), longCci);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene longCciBarCount = new IntegerGene(conf, 100, 300);
        IntegerGene shortCciBarCount = new IntegerGene(conf, 1, 50);

        return new Gene[]{longCciBarCount, shortCciBarCount};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("Long CCI Bar Count", this.parameters[0]);
        map.put("Short CCI Bar Count", this.parameters[1]);

        return map;
    }

}
