package com.cebbus.analysis;

import com.cebbus.analysis.strategy.*;
import com.cebbus.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class TheOracle {

    private final BarSeries series;
    private final CebStrategy cebStrategy;
    private final TradingRecord tradingRecord = new BaseTradingRecord();

    public TheOracle(BarSeries series, String strategy) {
        this.series = series;
        this.cebStrategy = ReflectionUtil.initStrategy(series, strategy);
    }

    public TradingRecord backtest() {
        return backtest(prophesy());
    }

    public void chooseBest() {
        AnalysisCriterion rc = new GrossReturnCriterion();
        List<Strategy> strategies = new ArrayList<>();

        strategies.add(calculateProfit(rc, new Rsi2Strategy(this.series)));
        strategies.add(calculateProfit(rc, new AdxStrategy(this.series)));
        strategies.add(calculateProfit(rc, new GlobalExtremaStrategy(this.series)));
        strategies.add(calculateProfit(rc, new MovingMomentumStrategy(this.series)));
        strategies.add(calculateProfit(rc, new ObvStrategy(this.series)));
        strategies.add(calculateProfit(rc, new ScalpingStrategy(this.series)));
        strategies.add(calculateProfit(rc, new CciCorrectionStrategy(this.series)));
        strategies.add(calculateProfit(rc, new GoldenCrossStrategy(this.series)));

        BarSeriesManager seriesManager = new BarSeriesManager(this.series);
        Strategy bestStrategy = rc.chooseBest(seriesManager, strategies);
        log.info("--> Best strategy: " + bestStrategy.getName() + "\n");
    }

    private Strategy calculateProfit(AnalysisCriterion criterion, CebStrategy cs) {
        Strategy s = cs.getStrategy();
        TradingRecord r = backtest(s);
        log.info(s.getName() + ":\t" + criterion.calculate(this.series, r));

        return s;
    }

    private TradingRecord backtest(Strategy s) {
        BarSeriesManager seriesManager = new BarSeriesManager(this.series);
        return seriesManager.run(s);
    }

    public BarSeries getSeries() {
        return series;
    }

    public TradingRecord getTradingRecord() {
        return tradingRecord;
    }

    public Strategy prophesy() {
        return this.cebStrategy.getStrategy();
    }

    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return this.cebStrategy.getIndicators();
    }
}
