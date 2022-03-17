package com.cebbus.analysis.strategy;

import com.cebbus.analysis.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdxStrategyTest {

    private BarSeries series;
    private CebStrategy strategy;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
        this.strategy = new AdxStrategy(series);
    }

    @Test
    void getSeries() {
        assertEquals(this.series, this.strategy.getSeries());
    }

    @Test
    void getParameters() {
        Number[] expected = new Number[]{50, 14, 20};
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
        Map<String, Number> map = new LinkedHashMap<>(3);
        map.put("SMA Bar Count", 50);
        map.put("ADX Bar Count", 14);
        map.put("ADX Threshold", 20);

        assertEquals(map, this.strategy.getParameterMap());
    }

}