package com.cebbus.analysis;

import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.util.ReflectionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cebbus.util.ReflectionUtil.initStrategy;

@Data
@Slf4j
public class TheOracle {

    private final BarSeries series;
    private final CebStrategy cebStrategy;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;

    public TheOracle(BarSeries series, String strategy) {
        this.series = series;

        CebStrategy cs;
        if (strategy.contains("&")) {
            String[] strategies = strategy.split("&");
            cs = initStrategy(series, strategies[0]);
            for (int i = 1; i < strategies.length; i++) {
                cs = cs.and(initStrategy(series, strategies[i]));
            }
        } else if (strategy.contains("|")) {
            String[] strategies = strategy.split("\\|");
            cs = initStrategy(series, strategies[0]);
            for (int i = 1; i < strategies.length; i++) {
                cs = cs.or(initStrategy(series, strategies[i]));
            }
        } else {
            cs = initStrategy(series, strategy);
        }

        this.cebStrategy = cs;
        this.tradingRecord = new BaseTradingRecord();
        this.backtestRecord = backtest(prophesy());
    }

    public List<Pair<String, Num>> calcStrategies() {
        List<Class<? extends BaseCebStrategy>> strategies = ReflectionUtil.listStrategyClasses();

        return strategies.stream().map(clazz -> {
            Strategy strategy = initStrategy(this.series, clazz).getStrategy();
            return Pair.of(strategy.getName(), calculateProfit(strategy));
        }).collect(Collectors.toList());
    }

    private Num calculateProfit(Strategy strategy) {
        AnalysisCriterion criterion = new GrossReturnCriterion();
        TradingRecord rec = backtest(strategy);

        return criterion.calculate(this.series, rec);
    }

    private TradingRecord backtest(Strategy s) {
        BarSeriesManager seriesManager = new BarSeriesManager(this.series);
        return seriesManager.run(s);
    }

    public Strategy prophesy() {
        return this.cebStrategy.getStrategy();
    }

    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return this.cebStrategy.getIndicators();
    }
}
