package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class MacdStrategy extends BaseCebStrategy {

    public MacdStrategy(BarSeries series) {
        super(series);
    }

    @Override
    BuilderResult build() {
        ClosePriceIndicator cpi = new ClosePriceIndicator(this.series);
        SMAIndicator cpiShortSma = new SMAIndicator(cpi, 50);
        SMAIndicator cpiLongSma = new SMAIndicator(cpi, 200);

        OnBalanceVolumeIndicator obv = new OnBalanceVolumeIndicator(this.series);
        SMAIndicator obvSma = new SMAIndicator(obv, 21);

        MACDIndicator macd = new MACDIndicator(cpi);
        EMAIndicator signal = new EMAIndicator(macd, 9);

        Rule entryRule = new OverIndicatorRule(macd, signal)
                .and(new OverIndicatorRule(obv, obvSma))
                .and(new OverIndicatorRule(macd, 0));

        Rule exitRule = new UnderIndicatorRule(macd, signal)
                .or(new StopLossRule(cpi, 5));

        BaseStrategy strategy = new BaseStrategy("MACD", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("MACD", new LinkedHashMap<>());
        indicators.get("MACD").put("MACD", macd);
        indicators.get("MACD").put("EMA (9)", signal);

        indicators.put("OBV", new LinkedHashMap<>());
        indicators.get("OBV").put("OBV", obv);
        indicators.get("OBV").put("SMA (50)", obvSma);

        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("Close Price", cpi);
        indicators.get("CPI").put("SMA (50)", cpiShortSma);
        indicators.get("CPI").put("SMA (200)", cpiLongSma);

        return new BuilderResult(strategy, indicators);
    }
}
