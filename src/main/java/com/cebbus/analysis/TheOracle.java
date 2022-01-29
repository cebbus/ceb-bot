package com.cebbus.analysis;

import com.cebbus.analysis.strategy.CebStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

@Slf4j
public class TheOracle {

    private final CebStrategy cebStrategy;
    private final GrossReturnCriterion grossReturnCriterion;
    private final BuyAndHoldReturnCriterion buyAndHoldReturnCriterion;

    private TradingRecord tradingRecord;
    private TradingRecord backtestRecord;

    public TheOracle(CebStrategy cebStrategy) {
        this.cebStrategy = cebStrategy;
        this.grossReturnCriterion = new GrossReturnCriterion();
        this.buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
    }

    public BarSeries getSeries() {
        return this.cebStrategy.getSeries();
    }

    public Strategy prophesy() {
        return this.cebStrategy.getStrategy();
    }

    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return this.cebStrategy.getIndicators();
    }

    public Number[] getProphesyParameters() {
        return this.cebStrategy.getParameters();
    }

    public Map<String, Number> getProphesyParameterMap() {
        return this.cebStrategy.getParameterMap();
    }

    public Num calculateProfit() {
        return this.grossReturnCriterion.calculate(getSeries(), getBacktestRecord());
    }

    public Num calculateBuyAndHold() {
        return this.buyAndHoldReturnCriterion.calculate(getSeries(), getBacktestRecord());
    }

    public Chromosome getProphesyOmen(Configuration conf) throws InvalidConfigurationException {
        return new Chromosome(conf, this.cebStrategy.createGene(conf));
    }

    public void changeProphesyParameters(Number... parameters) {
        this.cebStrategy.rebuild(parameters);
        this.backtestRecord = null;
        getBacktestRecord();
    }

    public TradingRecord getTradingRecord() {
        if (this.tradingRecord == null) {
            this.tradingRecord = new BaseTradingRecord();
        }

        return this.tradingRecord;
    }

    public TradingRecord getBacktestRecord() {
        if (this.backtestRecord == null) {
            BarSeriesManager manager = new BarSeriesManager(getSeries());
            this.backtestRecord = manager.run(prophesy());
        }

        return this.backtestRecord;
    }
}
