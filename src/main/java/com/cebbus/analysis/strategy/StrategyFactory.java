package com.cebbus.analysis.strategy;

import org.ta4j.core.BarSeries;

import static com.cebbus.util.ReflectionUtil.initStrategy;

public class StrategyFactory {

    private StrategyFactory() {
    }

    public static CebStrategy create(BarSeries series, Class<? extends BaseCebStrategy> clazz) {
        return initStrategy(series, clazz);
    }

    public static CebStrategy create(BarSeries series, String clazz) {
        CebStrategy cs;
        if (clazz.contains("&")) {
            return createCombinedStrategy(clazz, series, true);
        } else if (clazz.contains("|")) {
            return createCombinedStrategy(clazz, series, false);
        } else {
            cs = initStrategy(series, clazz);
        }

        return cs;
    }

    private static CebStrategy createCombinedStrategy(String clazz, BarSeries series, boolean and) {
        String[] strategies = clazz.split(and ? "&" : "\\|");
        CebStrategy strategy = initStrategy(series, strategies[0]);

        for (int i = 1; i < strategies.length; i++) {
            CebStrategy tempStrategy = initStrategy(series, strategies[i]);
            strategy = and ? strategy.and(tempStrategy) : strategy.or(tempStrategy);
        }

        return strategy;
    }

}
