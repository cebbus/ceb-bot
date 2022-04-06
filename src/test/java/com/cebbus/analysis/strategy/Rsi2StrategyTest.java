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

class Rsi2StrategyTest {

    private BarSeries series;
    private CebStrategy strategy;
    private Number[] parameters;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
        this.strategy = new Rsi2Strategy(series);
        this.parameters = new Number[]{2, 5, 95, 5, 200};
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
        Number[] expected = new Number[]{1, 2, 3, 4, 5};
        this.strategy.rebuild(expected);

        assertEquals(expected, this.strategy.getParameters());
    }

    @Test
    void getParameterMap() {
        Map<String, Number> map = new LinkedHashMap<>(this.parameters.length);
        map.put("RSI Bar Count", this.parameters[0]);
        map.put("RSI Buy Threshold", this.parameters[1]);
        map.put("RSI Sell Threshold", this.parameters[2]);
        map.put("Short SMA Bar Count", this.parameters[3]);
        map.put("Long SMA Bar Count", this.parameters[4]);

        assertEquals(map, this.strategy.getParameterMap());
    }

    @Test
    void createGene() throws InvalidConfigurationException {
        Configuration conf = new Configuration();
        IntegerGene rsiBarCount = new IntegerGene(conf, 1, 10);
        IntegerGene entryThreshold = new IntegerGene(conf, 1, 10);
        IntegerGene exitThreshold = new IntegerGene(conf, 75, 100);
        IntegerGene shortSmaBarCount = new IntegerGene(conf, 1, 10);
        IntegerGene longSmaBarCount = new IntegerGene(conf, 150, 300);

        assertArrayEquals(new Gene[]{rsiBarCount, entryThreshold, exitThreshold, shortSmaBarCount, longSmaBarCount}, this.strategy.createGene(conf));
    }

}