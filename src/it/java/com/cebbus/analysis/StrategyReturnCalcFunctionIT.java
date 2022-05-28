package com.cebbus.analysis;

import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.analysis.strategy.MacdStrategy;
import com.cebbus.binance.Speculator;
import com.cebbus.dto.CandleDto;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.util.PropertyReader;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StrategyReturnCalcFunctionIT {

    private StrategyReturnCalcFunction func;

    @BeforeEach
    void setUp() {
        Symbol symbol = PropertyReader.getSymbols().get(0);
        Speculator speculator = new Speculator(symbol);
        List<CandleDto> stickList = speculator.loadBarHistory();

        CsIntervalAdapter interval = symbol.getInterval();
        List<Bar> barList = BarMapper.dtoToBar(stickList, interval);

        BarSeries series = new BaseBarSeriesBuilder().withBars(barList).build();
        this.func = new StrategyReturnCalcFunction(symbol, series);
    }

    @Test
    void apply() {
        Pair<String, Double> actual = this.func.apply(MacdStrategy.class);

        assertNotNull(actual);
        assertEquals("MacdStrategy", actual.getKey());
    }

}
