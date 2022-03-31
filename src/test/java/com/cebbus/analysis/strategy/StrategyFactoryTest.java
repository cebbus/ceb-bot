package com.cebbus.analysis.strategy;

import com.cebbus.analysis.DataGenerator;
import com.cebbus.util.ReflectionUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ta4j.core.BarSeries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class StrategyFactoryTest {

    @Test
    void create() {
        BarSeries series = DataGenerator.generateSeries();
        CebStrategy expected = new AdxStrategy(series);

        CebStrategy actual;
        try (MockedStatic<ReflectionUtil> reflectionUtilMock = mockStatic(ReflectionUtil.class)) {
            reflectionUtilMock.when(() -> ReflectionUtil.initStrategy(series, AdxStrategy.class)).thenReturn(expected);
            actual = StrategyFactory.create(series, AdxStrategy.class);
        }

        assertEquals(expected, actual);
    }

    @Test
    void createCombinedStrategy() {
        BarSeries series = DataGenerator.generateSeries();
        CebStrategy expected = new AdxStrategy(series);

        CebStrategy actual;
        try (MockedStatic<ReflectionUtil> reflectionUtilMock = mockStatic(ReflectionUtil.class)) {
            reflectionUtilMock.when(() -> ReflectionUtil.initStrategy(series, "AdxStrategy")).thenReturn(expected);
            actual = StrategyFactory.create(series, "AdxStrategy");
        }

        assertEquals(expected, actual);
    }

    @Test
    void createCombinedStrategyOr() {
        BarSeries series = DataGenerator.generateSeries();
        CebStrategy adxStrategy = new AdxStrategy(series);
        CebStrategy macdStrategy = new MacdStrategy(series);

        CebStrategy expected = adxStrategy.or(macdStrategy);

        CebStrategy actual;
        try (MockedStatic<ReflectionUtil> reflectionUtilMock = mockStatic(ReflectionUtil.class)) {
            reflectionUtilMock.when(() -> ReflectionUtil.initStrategy(series, "AdxStrategy")).thenReturn(adxStrategy);
            reflectionUtilMock.when(() -> ReflectionUtil.initStrategy(series, "MacdStrategy")).thenReturn(macdStrategy);

            actual = StrategyFactory.create(series, "AdxStrategy|MacdStrategy");
        }

        assertEquals(expected.getStrategy().getName(), actual.getStrategy().getName());
    }

    @Test
    void createCombinedStrategyAnd() {
        BarSeries series = DataGenerator.generateSeries();
        CebStrategy adxStrategy = new AdxStrategy(series);
        CebStrategy macdStrategy = new MacdStrategy(series);

        CebStrategy expected = adxStrategy.and(macdStrategy);

        CebStrategy actual;
        try (MockedStatic<ReflectionUtil> reflectionUtilMock = mockStatic(ReflectionUtil.class)) {
            reflectionUtilMock.when(() -> ReflectionUtil.initStrategy(series, "AdxStrategy")).thenReturn(adxStrategy);
            reflectionUtilMock.when(() -> ReflectionUtil.initStrategy(series, "MacdStrategy")).thenReturn(macdStrategy);

            actual = StrategyFactory.create(series, "AdxStrategy&MacdStrategy");
        }

        assertEquals(expected.getStrategy().getName(), actual.getStrategy().getName());
    }
}