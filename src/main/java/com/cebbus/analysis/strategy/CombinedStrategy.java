package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CombinedStrategy extends BaseCebStrategy {

    private final boolean and;
    private final List<CebStrategy> strategies;

    private CombinedStrategy(
            BarSeries series,
            Number[] parameters,
            List<CebStrategy> strategies,
            boolean and) {
        super(series, parameters);
        this.and = and;
        this.strategies = strategies;

        build();
    }

    @Override
    public void build() {
        int count = 0;
        Strategy strategy = null;
        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>();

        for (CebStrategy cebStrategy : this.strategies) {
            int length = cebStrategy.getParameters().length;

            Number[] parameters = new Number[length];
            for (int j = 0; j < length; j++) {
                parameters[j] = this.parameters[count++];
            }

            cebStrategy.rebuild(parameters);

            mergeIndicators(indicators, cebStrategy.getIndicators());

            if (strategy == null) {
                strategy = cebStrategy.getStrategy();
            } else if (this.and) {
                strategy = strategy.and(cebStrategy.getStrategy());
            } else {
                strategy = strategy.or(cebStrategy.getStrategy());
            }
        }

        this.builderResult = new BuilderResult(strategy, indicators);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        List<Gene[]> geneList = new ArrayList<>();
        for (CebStrategy strategy : this.strategies) {
            geneList.add(strategy.createGene(conf));
        }

        return geneList.stream()
                .flatMap(Stream::of)
                .toArray(Gene[]::new);
    }

    public static CombinedStrategy combine(BarSeries series, List<CebStrategy> strategies, boolean and) {
        Number[] parameters = concatParameters(strategies);
        return new CombinedStrategy(series, parameters, strategies, and);
    }

    private static Number[] concatParameters(List<CebStrategy> strategies) {
        return strategies.stream()
                .map(CebStrategy::getParameters)
                .flatMap(Stream::of)
                .toArray(Number[]::new);
    }

    private void mergeIndicators(
            Map<String, Map<String, CachedIndicator<Num>>> i1,
            Map<String, Map<String, CachedIndicator<Num>>> i2) {

        i2.forEach((k, v) -> {
            if (!i1.containsKey(k)) {
                i1.put(k, new LinkedHashMap<>());
            }

            v.forEach((sk, sv) -> i1.get(k).putIfAbsent(sk, sv));
        });
    }
}