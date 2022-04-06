package com.cebbus.analysis.strategy;

import com.cebbus.analysis.DataGenerator;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.IntegerGene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RsiStrategyTest {

    private BarSeries series;
    private CebStrategy strategy;
    private Number[] parameters;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
        this.strategy = new RsiStrategy(series);
        this.parameters = new Number[]{14, 30, 70};
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
        map.put("RSI Bar Count", this.parameters[0]);
        map.put("RSI Buy Threshold", this.parameters[1]);
        map.put("RSI Sell Threshold", this.parameters[2]);

        assertEquals(map, this.strategy.getParameterMap());
    }

    @Test
    void createGene() throws InvalidConfigurationException {
        Configuration conf = new Configuration();
        IntegerGene rsiBarCount = new IntegerGene(conf, 10, 50);
        IntegerGene entryThreshold = new IntegerGene(conf, 1, 50);
        IntegerGene exitThreshold = new IntegerGene(conf, 51, 100);

        assertArrayEquals(new Gene[]{rsiBarCount, entryThreshold, exitThreshold}, this.strategy.createGene(conf));
    }

}