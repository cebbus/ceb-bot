package com.cebbus.analysis.strategy;

import com.cebbus.analysis.DataGenerator;
import org.jgap.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JunkStrategyTest {

    private BarSeries series;
    private CebStrategy strategy;
    private Number[] parameters;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
        this.strategy = new JunkStrategy(series);
        this.parameters = new Number[0];
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
        Number[] expected = new Number[]{};
        this.strategy.rebuild(expected);

        assertEquals(expected, this.strategy.getParameters());
    }

    @Test
    void getParameterMap() {
        assertEquals(Collections.emptyMap(), this.strategy.getParameterMap());
    }

    @Test
    void createGene() throws InvalidConfigurationException {
        assertEquals(0, this.strategy.createGene(null).length);
    }

}