package com.cebbus.analysis;

import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.BarSeries;

import java.util.function.Function;

public class StrategyReturnCalcFunction implements Function<Class<? extends BaseCebStrategy>, Pair<String, Double>> {

    private final Symbol symbol;
    private final BarSeries series;

    StrategyReturnCalcFunction(Symbol symbol, BarSeries series) {
        this.symbol = symbol;
        this.series = series;
    }

    @Override
    public Pair<String, Double> apply(Class<? extends BaseCebStrategy> clazz) {
        Symbol copy = this.symbol.changeStrategy(clazz.getSimpleName());
        CebStrategy cebStrategy = StrategyFactory.create(this.series, clazz);

        TheOracle testOracle = new TheOracle(copy, cebStrategy);
        return Pair.of(clazz.getSimpleName(), testOracle.backtestStrategyReturn().doubleValue());
    }
}
