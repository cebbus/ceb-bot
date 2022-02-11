package com.cebbus.analysis;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.num.Num;

public class AnalysisCriterionCalculator {

    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;

    private final NumberOfBarsCriterion numberOfBarsCriterion = new NumberOfBarsCriterion();
    private final GrossReturnCriterion returnCriterion = new GrossReturnCriterion();
    private final BuyAndHoldReturnCriterion buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
    private final VersusBuyAndHoldCriterion versusBuyAndHoldCriterion = new VersusBuyAndHoldCriterion(new GrossReturnCriterion());
    private final WinningPositionsRatioCriterion winningRatioCriterion = new WinningPositionsRatioCriterion();

    AnalysisCriterionCalculator(TheOracle theOracle) {
        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.getBacktestRecord();
    }

    public int posCount() {
        return this.tradingRecord.getPositionCount();
    }

    public int barCount() {
        return this.numberOfBarsCriterion.calculate(this.series, this.tradingRecord).intValue();
    }

    public Num strategyReturn() {
        return this.returnCriterion.calculate(this.series, this.tradingRecord);
    }

    public Num buyAndHold() {
        return this.buyAndHoldReturnCriterion.calculate(this.series, this.tradingRecord);
    }

    public Num winnigRatio() {
        return this.winningRatioCriterion.calculate(this.series, this.tradingRecord);
    }

    public Num versus() {
        return this.versusBuyAndHoldCriterion.calculate(this.series, this.tradingRecord);
    }

    public int backtestPosCount() {
        return this.backtestRecord.getPositionCount();
    }

    public int backtestBarCount() {
        return this.numberOfBarsCriterion.calculate(this.series, this.backtestRecord).intValue();
    }

    public Num backtestStrategyReturn() {
        return this.returnCriterion.calculate(this.series, this.backtestRecord);
    }

    public Num backtestBuyAndHold() {
        return this.buyAndHoldReturnCriterion.calculate(this.series, this.backtestRecord);
    }

    public Num backtestWinnigRatio() {
        return this.winningRatioCriterion.calculate(this.series, this.backtestRecord);
    }

    public Num backtestVersus() {
        return this.versusBuyAndHoldCriterion.calculate(this.series, this.backtestRecord);
    }
}
