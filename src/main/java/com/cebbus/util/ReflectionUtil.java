package com.cebbus.util;

import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.exception.StrategyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class ReflectionUtil {

    private ReflectionUtil() {
    }

    public static CebStrategy initStrategy(BarSeries series, String strategy) {

        Object[] initArgs = new Object[]{series};
        Class<?>[] parameterTypes = new Class<?>[]{BarSeries.class};

        try {
            Class<?> strategyClazz = Class.forName("com.cebbus.analysis.strategy." + strategy);
            return (CebStrategy) strategyClazz.getDeclaredConstructor(parameterTypes).newInstance(initArgs);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new StrategyNotFoundException();
        }
    }
}
