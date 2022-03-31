package com.cebbus.analysis.strategy;

import com.cebbus.analysis.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CombinedStrategyTest {

    private BarSeries series;
    private CebStrategy strategy;
    private Number[] parameters;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();

        List<CebStrategy> strategies = List.of(new AdxStrategy(series), new ScalpingStrategy(series));
        this.strategy = CombinedStrategy.combine(series, strategies, true);

        this.parameters = new Number[]{50, 14, 20, 5, 8, 13};
    }

    @Test
    void getSeries() {
        assertEquals(this.series, this.strategy.getSeries());
    }

    @Test
    void getParameters() {
        assertArrayEquals(this.parameters, this.strategy.getParameters());
    }

    @Test
    void rebuild() {
        Number[] expected = new Number[]{1, 2, 3, 4, 5, 6};
        this.strategy.rebuild(expected);

        assertEquals(expected, this.strategy.getParameters());
    }

    @Test
    void getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("ADX - SMA Bar Count", this.parameters[0]);
        map.put("ADX - ADX Bar Count", this.parameters[1]);
        map.put("ADX - ADX Threshold", this.parameters[2]);
        map.put("Scalping - Short SMA Bar Count", this.parameters[3]);
        map.put("Scalping - Middle SMA Bar Count", this.parameters[4]);
        map.put("Scalping - Long SMA Bar Count", this.parameters[5]);

        assertEquals(map, this.strategy.getParameterMap());
    }

}