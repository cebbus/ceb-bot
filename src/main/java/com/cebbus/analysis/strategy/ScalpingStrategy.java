package com.cebbus.analysis.strategy;

import com.cebbus.analysis.rule.ForceToWinRule;
import com.cebbus.analysis.rule.LastEntryOverIndicatorRule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.TransformIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Short - Middle term strategy
 */
public class ScalpingStrategy extends BaseCebStrategy {

    public ScalpingStrategy(BarSeries series) {
        super(series);
    }

    @Override
    public BuilderResult build() {
        OnBalanceVolumeIndicator volume = new OnBalanceVolumeIndicator(this.series);
        SMAIndicator volumeSma = new SMAIndicator(volume, 21);

        HighPriceIndicator highPrice = new HighPriceIndicator(this.series);
        HighestValueIndicator dayHighPrice = new HighestValueIndicator(highPrice, 36);
        TransformIndicator upDay = TransformIndicator.multiply(dayHighPrice, 0.996D);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(this.series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator middleSma = new SMAIndicator(closePrice, 8);
        SMAIndicator longSma = new SMAIndicator(closePrice, 13);

        Rule entryRule = new OverIndicatorRule(shortSma, middleSma)
                .and(new OverIndicatorRule(shortSma, longSma))
                .and(new OverIndicatorRule(volume, volumeSma));

        Rule exitRule = new UnderIndicatorRule(shortSma, middleSma)
                .and(new UnderIndicatorRule(shortSma, longSma))
                .and(new ForceToWinRule(closePrice, 0.0)
                        .or(new LastEntryOverIndicatorRule(upDay))
                        .or(new StopLossRule(closePrice, 5))
                );

        BaseStrategy strategy = new BaseStrategy("Scalping", entryRule, exitRule);

        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();
        indicators.put("SMA", new LinkedHashMap<>());
        indicators.get("SMA").put("Short SMA", shortSma);
        indicators.get("SMA").put("Middle SMA", middleSma);
        indicators.get("SMA").put("Long SMA", longSma);

        indicators.put("VOL", new LinkedHashMap<>());
        indicators.get("VOL").put("Volume SMA", volumeSma);
        indicators.get("VOL").put("Volume", volume);

        indicators.put("CPI", new LinkedHashMap<>());
        indicators.get("CPI").put("Highest Value", upDay);
        indicators.get("CPI").put("Close Price", closePrice);

        return new BuilderResult(strategy, indicators);
    }

}
