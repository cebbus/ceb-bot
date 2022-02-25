package com.cebbus.analysis;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.num.Num;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.cebbus.view.chart.ColorPalette.*;

public class AnalysisCriterionCalculator {

    private static final DecimalFormat RESULT_FORMAT = new DecimalFormat("#,###.0000");

    private final BarSeries series;
    private final NumberOfBarsCriterion numberOfBarsCriterion = new NumberOfBarsCriterion();
    private final GrossReturnCriterion returnCriterion = new GrossReturnCriterion();
    private final BuyAndHoldReturnCriterion buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
    private final VersusBuyAndHoldCriterion versusBuyAndHoldCriterion = new VersusBuyAndHoldCriterion(new GrossReturnCriterion());
    private final WinningPositionsRatioCriterion winningRatioCriterion = new WinningPositionsRatioCriterion();

    private TradingRecord tradingRecord;
    private TradingRecord backtestRecord;

    AnalysisCriterionCalculator(BarSeries series, TradingRecord tradingRecord, TradingRecord backtestRecord) {
        this.series = series;
        setTradingRecord(tradingRecord);
        setBacktestRecord(backtestRecord);
    }

    private int posCount() {
        return this.tradingRecord.getPositionCount();
    }

    private int barCount() {
        return this.numberOfBarsCriterion.calculate(this.series, this.tradingRecord).intValue();
    }

    private Num strategyReturn() {
        return this.returnCriterion.calculate(this.series, this.tradingRecord);
    }

    private double strategyReturnAsDouble() {
        return strategyReturn().doubleValue();
    }

    private Num buyAndHold() {
        return this.buyAndHoldReturnCriterion.calculate(this.series, this.tradingRecord);
    }

    private double buyAndHoldAsDouble() {
        return buyAndHold().doubleValue();
    }

    private Num winnigRatio() {
        return this.winningRatioCriterion.calculate(this.series, this.tradingRecord);
    }

    private double winnigRatioAsDouble() {
        return winnigRatio().doubleValue();
    }

    private Num versus() {
        return this.versusBuyAndHoldCriterion.calculate(this.series, this.tradingRecord);
    }

    private double versusAsDouble() {
        return versus().doubleValue();
    }

    private int backtestPosCount() {
        return this.backtestRecord.getPositionCount();
    }

    private int backtestBarCount() {
        return this.numberOfBarsCriterion.calculate(this.series, this.backtestRecord).intValue();
    }

    protected Num backtestStrategyReturn() {
        return this.returnCriterion.calculate(this.series, this.backtestRecord);
    }

    private double backtestStrategyReturnAsDouble() {
        return backtestStrategyReturn().doubleValue();
    }

    protected Num backtestBuyAndHold() {
        return this.buyAndHoldReturnCriterion.calculate(this.series, this.backtestRecord);
    }

    private double backtestBuyAndHoldAsDouble() {
        return backtestBuyAndHold().doubleValue();
    }

    private Num backtestWinnigRatio() {
        return this.winningRatioCriterion.calculate(this.series, this.backtestRecord);
    }

    private double backtestWinnigRatioAsDouble() {
        return backtestWinnigRatio().doubleValue();
    }

    private Num backtestVersus() {
        return this.versusBuyAndHoldCriterion.calculate(this.series, this.backtestRecord);
    }

    private double backtestVersusAsDouble() {
        return backtestVersus().doubleValue();
    }

    public List<CriterionResult> getCriterionResultList(boolean backtest) {
        List<CriterionResult> resultList = new ArrayList<>();

        int positionCount = backtest ? backtestPosCount() : posCount();
        resultList.add(new CriterionResult("Number of Pos", positionCount, Integer.toString(positionCount), DARK_GRAY));

        int numOfBars = backtest ? backtestBarCount() : barCount();
        resultList.add(new CriterionResult("Number of Bars", numOfBars, Integer.toString(numOfBars), DARK_GRAY));

        double totalReturn = backtest ? backtestStrategyReturnAsDouble() : strategyReturnAsDouble();
        resultList.add(new CriterionResult("Strategy Return", totalReturn, RESULT_FORMAT.format(totalReturn), totalReturn > 0 ? GREEN : RED));

        double buyAndHold = backtest ? backtestBuyAndHoldAsDouble() : buyAndHoldAsDouble();
        resultList.add(new CriterionResult("Buy and Hold Return", buyAndHold, RESULT_FORMAT.format(buyAndHold), buyAndHold > 0 ? GREEN : RED));

        double versus = backtest ? backtestVersusAsDouble() : versusAsDouble();
        resultList.add(new CriterionResult("Strategy vs Hold (%)", versus, RESULT_FORMAT.format(versus * 100), versus > 1 ? GREEN : RED));

        double winningRatio = backtest ? backtestWinnigRatioAsDouble() : winnigRatioAsDouble();
        resultList.add(new CriterionResult("Strategy Winning Ratio (%)", winningRatio, RESULT_FORMAT.format(winningRatio * 100), winningRatio > 0.75 ? GREEN : RED));

        return resultList;
    }

    void setTradingRecord(TradingRecord tradingRecord) {
        this.tradingRecord = tradingRecord;
    }

    void setBacktestRecord(TradingRecord backtestRecord) {
        this.backtestRecord = backtestRecord;
    }
}
