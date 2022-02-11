package com.cebbus.analysis;

import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.num.Num;

import java.util.function.Function;

public class StrategyReturnCalcFunction implements Function<Class<? extends BaseCebStrategy>, Pair<String, Num>> {

    private final TheOracle theOracle;

    StrategyReturnCalcFunction(TheOracle theOracle) {
        this.theOracle = theOracle;
    }

    @Override
    public Pair<String, Num> apply(Class<? extends BaseCebStrategy> clazz) {
        CebStrategy cebStrategy = StrategyFactory.create(this.theOracle.getSeries(), clazz);
        TheOracle testOracle = new TheOracle(cebStrategy);
        AnalysisCriterionCalculator calculator = testOracle.getCriterionCalculator();

        return Pair.of(clazz.getSimpleName(), calculator.backtestStrategyReturn());
    }
}
