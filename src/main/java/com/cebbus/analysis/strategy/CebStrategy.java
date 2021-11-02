package com.cebbus.analysis.strategy;

import lombok.Data;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

public interface CebStrategy {
    void build();

    void rebuild(Number... parameters);

    CebStrategy and(CebStrategy other);

    CebStrategy or(CebStrategy other);

    Strategy getStrategy();

    Map<String, Map<String, CachedIndicator<Num>>> getIndicators();

    Gene[] createGene(Configuration conf) throws InvalidConfigurationException;

    Number[] getParameters();

    @Data
    final class BuilderResult {
        private final Strategy strategy;
        private final Map<String, Map<String, CachedIndicator<Num>>> indicators;
    }
}
