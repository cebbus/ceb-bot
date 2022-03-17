package com.cebbus.analysis.strategy;

import com.cebbus.analysis.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CciCorrectionStrategyTest {

    private BarSeries series;
    private CebStrategy strategy;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
        this.strategy = new CciCorrectionStrategy(series);
    }

    @Test
    void getSeries() {
        assertEquals(this.series, this.strategy.getSeries());
    }

    @Test
    void getParameters() {
        Number[] expected = new Number[]{200, 5};
        assertArrayEquals(expected, this.strategy.getParameters());
    }

    @Test
    void rebuild() {
        Number[] expected = new Number[]{1, 2, 3};
        this.strategy.rebuild(expected);

        assertEquals(expected, this.strategy.getParameters());
    }

    @Test
    void getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(2);
        map.put("Long CCI Bar Count", 200);
        map.put("Short CCI Bar Count", 5);

        assertEquals(map, this.strategy.getParameterMap());
    }

}