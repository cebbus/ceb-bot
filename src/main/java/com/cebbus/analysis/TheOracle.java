package com.cebbus.analysis;

import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.analysis.mapper.TradeMapper;
import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import com.cebbus.dto.*;
import com.cebbus.util.DateTimeUtil;
import com.cebbus.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class TheOracle {

    private final Symbol symbol;
    private final CebStrategy cebStrategy;
    private final SeriesHelper seriesHelper;
    private final TradeDataHelper tradeDataHelper;
    private final AnalysisCriterionCalculator criterionCalculator;
    private final TradingRecord tradingRecord;

    private TradingRecord backtestRecord;

    public TheOracle(Symbol symbol, CebStrategy cebStrategy) {
        this.symbol = symbol;
        this.cebStrategy = cebStrategy;
        this.tradingRecord = new BaseTradingRecord();
        this.backtestRecord = createBacktestRecord();

        BarSeries series = this.cebStrategy.getSeries();
        this.seriesHelper = new SeriesHelper(series);
        this.tradeDataHelper = new TradeDataHelper(series, this.tradingRecord, this.backtestRecord);
        this.criterionCalculator = new AnalysisCriterionCalculator(series, this.tradingRecord, this.backtestRecord);
    }

    public TheOracle(
            Symbol symbol,
            List<TradeDto> tradeList,
            List<CandleDto> candlestickList) {
        this.symbol = symbol;

        List<Bar> barList = BarMapper.dtoToBar(candlestickList, symbol.getInterval());
        this.seriesHelper = new SeriesHelper(symbol, barList);

        BarSeries series = this.seriesHelper.getSeries();
        this.cebStrategy = StrategyFactory.create(series, symbol.getStrategy());
        this.tradingRecord = new BaseTradingRecord();
        this.backtestRecord = createBacktestRecord();

        TradeMapper tradeMapper = new TradeMapper(series, tradeList);
        List<Trade> tradeHistoryList = tradeMapper.getTradeHistory();
        this.tradeDataHelper = new TradeDataHelper(series, this.tradingRecord, this.backtestRecord, tradeHistoryList);

        this.criterionCalculator = new AnalysisCriterionCalculator(series, this.tradingRecord, this.backtestRecord);
    }

    public TheOracle changeStrategy(String strategy) {
        BarSeries series = this.cebStrategy.getSeries();
        CebStrategy newStrategy = StrategyFactory.create(series, strategy);

        Symbol copy = this.symbol.changeStrategy(strategy);
        return new TheOracle(copy, newStrategy);
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

    public void changeProphesyParameters(Number... parameters) {
        this.cebStrategy.rebuild(parameters);
        this.backtestRecord = createBacktestRecord();
        this.tradeDataHelper.setBacktestRecord(this.backtestRecord);
        this.criterionCalculator.setBacktestRecord(this.backtestRecord);
    }

    public List<Pair<String, Double>> calcStrategies() {
        BarSeries series = this.cebStrategy.getSeries();

        List<Class<? extends BaseCebStrategy>> strategies = ReflectionUtil.listStrategyClasses();
        StrategyReturnCalcFunction calcFunction = new StrategyReturnCalcFunction(this.symbol, series);

        return strategies.stream().map(calcFunction).collect(Collectors.toList());
    }

    public boolean shouldEnter(boolean isSpecActive, boolean isManual) {
        TradingRecord tr = isSpecActive ? this.tradingRecord : this.backtestRecord;
        if (!tr.getCurrentPosition().isNew()) {
            log.info("{} - you are already in a position!", this.seriesHelper.getName());
            return false;
        }

        return isManual || this.cebStrategy.shouldEnter(tr);
    }

    public boolean shouldExit(boolean isSpecActive, boolean isManual) {
        TradingRecord tr = isSpecActive ? this.tradingRecord : this.backtestRecord;
        if (notInPosition(tr)) {
            log.info("{} - you have no position!", this.seriesHelper.getName());
            return false;
        }

        return isManual || this.cebStrategy.shouldExit(tr);
    }

    public boolean notInPosition(TradingRecord tr) {
        return !Optional.ofNullable(tr).orElse(this.tradingRecord).getCurrentPosition().isOpened();
    }

    public void addBar(CandleDto dto) {
        Bar newBar = BarMapper.dtoToBar(dto, this.symbol.getInterval());
        this.seriesHelper.addBar(newBar);
    }

    public CandleDto getLastCandle() {
        Bar bar = this.seriesHelper.getLastBar();
        return BarMapper.barToDto(bar);
    }

    public List<CandleDto> getCandleDataList() {
        List<Bar> barList = this.seriesHelper.getCandleDataList();
        return BarMapper.barToDto(barList);
    }

    public IndicatorValueDto getLastIndicatorValue(CachedIndicator<Num> indicator) {
        int index = this.seriesHelper.getEndIndex();
        return createIndicatorValueDto(index, indicator);
    }

    public List<IndicatorValueDto> getIndicatorValueList(CachedIndicator<Num> indicator) {
        List<Integer> indexList = this.seriesHelper.getSeriesIndexList();
        return indexList.stream().map(i -> createIndicatorValueDto(i, indicator)).collect(Collectors.toList());
    }

    public TradeDto newTrade(boolean isSpecActive, Pair<Number, Number> priceAmount) {
        Trade trade = this.tradeDataHelper.newTrade(isSpecActive, priceAmount);
        return TradeMapper.tradeToDto(trade);
    }

    public List<TradePointDto> getTradePointList() {
        return this.tradeDataHelper.getTradePointList();
    }

    public Optional<TradePointDto> getLastTradePoint(boolean backtest) {
        return this.tradeDataHelper.getLastTradePoint(backtest);
    }

    public Optional<TradeRowDto> getLastTradeRow(boolean backtest) {
        return this.tradeDataHelper.getLastTradeRow(backtest);
    }

    public List<TradeRowDto> getTradeRowList(boolean backtest) {
        return this.tradeDataHelper.getTradeRowList(backtest);
    }

    public Number backtestBuyAndHold() {
        return this.criterionCalculator.backtestBuyAndHold().getDelegate();
    }

    public Number backtestStrategyReturn() {
        return this.criterionCalculator.backtestStrategyReturn().getDelegate();
    }

    public List<CriterionResultDto> getCriterionResultList(boolean backtest) {
        return this.criterionCalculator.getCriterionResultList(backtest);
    }

    private TradingRecord createBacktestRecord() {
        if (!GraphicsEnvironment.isHeadless()) {
            BarSeries series = this.cebStrategy.getSeries();
            Strategy strategy = this.cebStrategy.getStrategy();

            BarSeriesManager manager = new BarSeriesManager(series);
            return manager.run(strategy);
        } else {
            return new BaseTradingRecord();
        }
    }

    private IndicatorValueDto createIndicatorValueDto(int index, CachedIndicator<Num> indicator) {
        double value = indicator.getValue(index).doubleValue();
        Bar bar = this.seriesHelper.getBar(index);
        Long beginTime = DateTimeUtil.zonedTimeToMillis(bar.getBeginTime());

        return new IndicatorValueDto(index, value, beginTime);
    }
}
