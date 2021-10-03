package com.cebbus.analysis;

import com.cebbus.analysis.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.Map;

@Slf4j
public class TheOracle {

    private final BarSeries series;
    private final CebStrategy strategy;
    private final TradingRecord tradingRecord = new BaseTradingRecord();

    public TheOracle(BarSeries series) {
        this.series = series;
        this.strategy = new Rsi2Strategy(series);
        //this.strategy = new AdxStrategy(series);
        //this.strategy = new GlobalExtremaStrategy(series);
        //this.strategy = new MovingMomentumStrategy(series);
        //this.strategy = new DummyStrategy(series);
    }

    public Strategy prophesy() {
        return this.strategy.build();
    }

    public TradingRecord backtest() {
        BarSeriesManager seriesManager = new BarSeriesManager(this.series);
        return seriesManager.run(prophesy());
    }

    public void walkForward() {

    }

    public BarSeries getSeries() {
        return series;
    }

    public TradingRecord getTradingRecord() {
        return tradingRecord;
    }

    public Map<String, Map<String, CachedIndicator<Num>>> getIndicators() {
        return this.strategy.getIndicators();
    }
}
