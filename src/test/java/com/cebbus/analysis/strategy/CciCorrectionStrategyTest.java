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
    private Number[] parameters;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
        this.strategy = new CciCorrectionStrategy(series);
        this.parameters = new Number[]{200, 5};
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
        Number[] expected = new Number[]{1, 2, 3};
        this.strategy.rebuild(expected);

        assertEquals(expected, this.strategy.getParameters());
    }

    @Test
    void getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("Long CCI Bar Count", this.parameters[0]);
        map.put("Short CCI Bar Count", this.parameters[1]);

        assertEquals(map, this.strategy.getParameterMap());
    }

}