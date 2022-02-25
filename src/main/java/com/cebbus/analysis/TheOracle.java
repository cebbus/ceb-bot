package com.cebbus.analysis;

import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import com.cebbus.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class TheOracle {

    private final CebStrategy cebStrategy;
    private final SeriesHelper seriesHelper;
    private final TradeDataHelper tradeDataHelper;
    private final AnalysisCriterionCalculator criterionCalculator;
    private final TradingRecord tradingRecord;

    private TradingRecord backtestRecord;

    public TheOracle(CebStrategy cebStrategy) {
        this.cebStrategy = cebStrategy;
        this.tradingRecord = new BaseTradingRecord();
        this.backtestRecord = createBacktestRecord();

        BarSeries series = this.cebStrategy.getSeries();
        this.seriesHelper = new SeriesHelper(series);
        this.tradeDataHelper = new TradeDataHelper(series, this.tradingRecord, this.backtestRecord);
        this.criterionCalculator = new AnalysisCriterionCalculator(series, this.tradingRecord, this.backtestRecord);
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

    public Chromosome getProphesyOmen(Configuration conf) throws InvalidConfigurationException {
        return new Chromosome(conf, this.cebStrategy.createGene(conf));
    }

    public TheOracle changeStrategy(String strategy) {
        BarSeries series = this.cebStrategy.getSeries();
        CebStrategy newStrategy = StrategyFactory.create(series, strategy);

        return new TheOracle(newStrategy);
    }

    public void changeProphesyParameters(Number... parameters) {
        this.cebStrategy.rebuild(parameters);
        this.backtestRecord = createBacktestRecord();
        this.tradeDataHelper.setBacktestRecord(this.backtestRecord);
        this.criterionCalculator.setBacktestRecord(this.backtestRecord);
    }

    public List<Pair<String, Double>> calcStrategies() {
        BarSeries series = this.cebStrategy.getSeries();

        List<Class<? extends BaseCebStrategy>> strategies = ReflectionUtil.listStrategyClasses();
        StrategyReturnCalcFunction calcFunction = new StrategyReturnCalcFunction(series);
        return strategies.stream().map(calcFunction).collect(Collectors.toList());
    }

    public boolean shouldEnter(boolean isSpecActive, boolean isManual) {
        TradingRecord tr = isSpecActive ? this.tradingRecord : this.backtestRecord;
        if (!tr.getCurrentPosition().isNew()) {
            log.info("you are already in a position!");
            return false;
        }

        return isManual || this.cebStrategy.shouldEnter(tr);
    }

    public boolean shouldExit(boolean isSpecActive, boolean isManual) {
        TradingRecord tr = isSpecActive ? this.tradingRecord : this.backtestRecord;
        if (notInPosition(tr)) {
            log.info("you have no position!");
            return false;
        }

        return isManual || this.cebStrategy.shouldExit(tr);
    }

    public boolean notInPosition(TradingRecord tr) {
        return !Optional.ofNullable(tr).orElse(this.tradingRecord).getCurrentPosition().isOpened();
    }

    public void addBar(Bar newBar) {
        this.seriesHelper.addBar(newBar);
    }

    public boolean isNewCandle() {
        return this.seriesHelper.isNewCandle();
    }

    public OHLCItem getLastCandle() {
        return this.seriesHelper.getLastCandle();
    }

    public List<OHLCItem> getCandleDataList() {
        return this.seriesHelper.getCandleDataList();
    }

    public TimeSeriesDataItem getLastSeriesItem(CachedIndicator<Num> indicator) {
        return this.seriesHelper.getLastSeriesItem(indicator);
    }

    public List<TimeSeriesDataItem> getSeriesDataList(CachedIndicator<Num> indicator) {
        return this.seriesHelper.getSeriesDataList(indicator);
    }

    public void fillTradeHistory(List<com.binance.api.client.domain.account.Trade> tradeList) {
        this.tradeDataHelper.fillTradeHistory(tradeList);
    }

    public Trade newTrade(boolean isSpecActive, Pair<Num, Num> priceAmount) {
        return this.tradeDataHelper.newTrade(isSpecActive, priceAmount);
    }

    public List<Object[]> getTradePointList() {
        return this.tradeDataHelper.getTradePointList();
    }

    public boolean hasNewTrade(boolean backtest) {
        return this.tradeDataHelper.hasNewTrade(backtest);
    }

    public Object[] getLastTradePoint(boolean backtest) {
        return this.tradeDataHelper.getLastTradePoint(backtest);
    }

    public boolean hasNewPosition(boolean backtest) {
        return this.tradeDataHelper.hasNewPosition(backtest);
    }

    public Object[] getLastTradeRow(boolean backtest) {
        return this.tradeDataHelper.getLastTradeRow(backtest);
    }

    public List<Object[]> getTradeRowList(boolean backtest) {
        return this.tradeDataHelper.getTradeRowList(backtest);
    }

    public Num backtestBuyAndHold() {
        return this.criterionCalculator.backtestBuyAndHold();
    }

    public Num backtestStrategyReturn() {
        return this.criterionCalculator.backtestStrategyReturn();
    }

    public List<CriterionResult> getCriterionResultList(boolean backtest) {
        return this.criterionCalculator.getCriterionResultList(backtest);
    }

    private TradingRecord createBacktestRecord() {
        BarSeries series = this.cebStrategy.getSeries();
        Strategy strategy = this.cebStrategy.getStrategy();

        BarSeriesManager manager = new BarSeriesManager(series);
        return manager.run(strategy);
    }
}
