package com.cebbus.analysis;

import com.cebbus.analysis.strategy.MacdStrategy;
import com.cebbus.binance.Speculator;
import com.cebbus.util.PropertyReader;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StrategyReturnCalcFunctionIT {

    private StrategyReturnCalcFunction func;

    @BeforeEach
    void setUp() {
        Symbol symbol = PropertyReader.getSymbols().get(0);
        Speculator speculator = new Speculator(symbol);
        speculator.loadHistory();

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(symbol.getName())
                .withBars(speculator.convertToBarList())
                .withMaxBarCount(PropertyReader.getCacheSize())
                .build();

        this.func = new StrategyReturnCalcFunction(series);
    }

    @Test
    void apply() {
        Pair<String, Double> actual = this.func.apply(MacdStrategy.class);

        assertNotNull(actual);
        assertEquals("MacdStrategy", actual.getKey());
    }

}
