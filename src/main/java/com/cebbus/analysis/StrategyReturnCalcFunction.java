package com.cebbus.analysis;

import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.BarSeries;

import java.util.function.Function;

public class StrategyReturnCalcFunction implements Function<Class<? extends BaseCebStrategy>, Pair<String, Double>> {

    private final BarSeries series;

    StrategyReturnCalcFunction(BarSeries series) {
        this.series = series;
    }

    @Override
    public Pair<String, Double> apply(Class<? extends BaseCebStrategy> clazz) {
        CebStrategy cebStrategy = StrategyFactory.create(this.series, clazz);
        TheOracle testOracle = new TheOracle(cebStrategy);
        return Pair.of(clazz.getSimpleName(), testOracle.backtestStrategyReturn().doubleValue());
    }
}
