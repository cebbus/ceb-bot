package com.cebbus.analysis.strategy;

import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public interface CebStrategy {
    Strategy build();

    Map<String, Map<String, CachedIndicator<Num>>> getIndicators();
}
