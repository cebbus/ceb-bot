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
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Middle - Long term strategy
 */
public class ObvStrategy extends BaseCebStrategy {

    public ObvStrategy(BarSeries series) {
        super(series, new Number[]{21});
        build();
    }

    @Override
    public void build() {
        int smaBarCount = this.parameters[0].intValue();

        ClosePriceIndicator cpi = new ClosePriceIndicator(this.series);
        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(this.series);
        SMAIndicator sma = new SMAIndicator(obv, smaBarCount);

        Rule entryRule = new OverIndicatorRule(obv, sma);
        Rule exitRule = new UnderIndicatorRule(obv, sma);

        BaseStrategy strategy = new BaseStrategy(OBV_KEY, entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put(CPI_KEY, Map.of(CPI_KEY, cpi));

        indicators.put(OBV_KEY, new LinkedHashMap<>());
        indicators.get(OBV_KEY).put(OBV_KEY, obv);
        indicators.get(OBV_KEY).put(String.format(SMA_PARAM_KEY, smaBarCount), sma);

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        IntegerGene smaBarCount = new IntegerGene(conf, 10, 50);

        return new Gene[]{smaBarCount};
    }

    @Override
    public Map<String, Number> getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("SMA Bar Count", this.parameters[0]);

        return map;
    }

}
