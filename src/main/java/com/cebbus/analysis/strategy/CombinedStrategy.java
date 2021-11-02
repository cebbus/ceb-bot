package com.cebbus.analysis.strategy;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.util.List;

public class CombinedStrategy extends BaseCebStrategy {

    private boolean and;
    private List<CebStrategy> strategies;

    private CombinedStrategy(BarSeries series, Number[] parameters) {
        super(series, parameters);
    }

    @Override
    public void build() {
        int count = 0;
        Strategy strategy = null;

        for (CebStrategy cebStrategy : this.strategies) {
            int length = cebStrategy.getParameters().length;

            Number[] parameters = new Number[length];
            for (int j = 0; j < length; j++) {
                parameters[j] = this.parameters[count++];
            }

            cebStrategy.rebuild(parameters);
            if (strategy == null) {
                strategy = cebStrategy.getStrategy();
            } else if (this.and) {
                strategy = strategy.and(cebStrategy.getStrategy());
            } else {
                strategy = strategy.or(cebStrategy.getStrategy());
            }
        }

        //FIXME
        this.builderResult = new BuilderResult(strategy, null);
    }

    @Override
    public Gene[] createGene(Configuration conf) throws InvalidConfigurationException {
        return new Gene[0];
    }

    public static CombinedStrategy combine(BarSeries series, List<CebStrategy> strategies, boolean and) {
        int paramSize = strategies.stream().mapToInt(s -> s.getParameters().length).sum();

        int count = 0;
        Number[] parameters = new Number[paramSize];

        for (CebStrategy strategy : strategies) {
            for (Number parameter : strategy.getParameters()) {
                parameters[count++] = parameter;
            }
        }

        CombinedStrategy strategy = new CombinedStrategy(series, parameters);
        strategy.strategies = strategies;
        strategy.and = and;

        return strategy;
    }

//    private Map<String, Map<String, CachedIndicator<Num>>> concatIndicators(CebStrategy other) {
//        Map<String, Map<String, CachedIndicator<Num>>> indicators = new LinkedHashMap<>(this.getIndicators());
//
//        this.getIndicators().forEach((k, v) -> {
//            indicators.put(k, new LinkedHashMap<>());
//            indicators.get(k).putAll(v);
//        });
//
//        other.getIndicators().forEach((k, v) -> {
//            if (indicators.containsKey(k)) {
//                indicators.get(k).putAll(v);
//            } else {
//                indicators.put(k, v);
//            }
//        });
//
//        return indicators;
//    }
}