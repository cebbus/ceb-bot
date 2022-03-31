package com.cebbus.analysis.strategy;

import com.cebbus.analysis.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MovingMomentumStrategyTest {

    private BarSeries series;
    private CebStrategy strategy;
    private Number[] parameters;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
        this.strategy = new MovingMomentumStrategy(series);
        this.parameters = new Number[]{9, 26, 14, 18, 20, 80};
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
        map.put("Short EMA Bar Count", this.parameters[0]);
        map.put("Long EMA Bar Count", this.parameters[1]);
        map.put("Stochastic Oscillator Bar Count", this.parameters[2]);
        map.put("MACD EMA Bar Count", this.parameters[3]);
        map.put("Stochastic Oscillator Buy Threshold", this.parameters[4]);
        map.put("Stochastic Oscillator Sell Threshold", this.parameters[5]);

        assertEquals(map, this.strategy.getParameterMap());
    }

}