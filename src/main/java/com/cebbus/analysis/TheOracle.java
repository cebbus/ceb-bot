package com.cebbus.analysis;

import com.binance.api.client.domain.account.Trade;
import com.cebbus.analysis.strategy.BaseCebStrategy;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import com.cebbus.binance.mapper.TradeMapper;
import com.cebbus.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.InvalidConfigurationException;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<Pair<String, Num>> calcStrategies() {
        BarSeries series = getSeries();
        List<Class<? extends BaseCebStrategy>> strategies = ReflectionUtil.listStrategyClasses();

        return strategies.stream().map(clazz -> {
            CebStrategy cs = StrategyFactory.create(series, clazz);
            TheOracle testOracle = new TheOracle(cs);
            return Pair.of(clazz.getSimpleName(), testOracle.calculateProfit());
        }).collect(Collectors.toList());
    }

    public void fillTradeHistory(List<Trade> tradeList) {
        BarSeries series = getSeries();

        TradeMapper tradeMapper = new TradeMapper(series, tradeList);
        Map<Integer, List<Trade>> tradeMap = tradeMapper.getTradeMap();

        TradingRecord tradingRecord = getTradingRecord();
        tradeMap.forEach((index, trades) -> {
            for (Trade trade : trades) {
                Num price = DecimalNum.valueOf(trade.getPrice());
                Num amount = DecimalNum.valueOf(trade.getQty());

                if (trade.isBuyer()) {
                    tradingRecord.enter(index, price, amount);
                } else {
                    tradingRecord.exit(index, price, amount);
                }
            }
        });
    }

    public boolean shouldEnter(boolean isSpecActive, boolean isManual) {
        TradingRecord tradingRecord = isSpecActive ? getTradingRecord() : getBacktestRecord();
        if (!tradingRecord.getCurrentPosition().isNew()) {
            log.info("you are already in a position!");
            return false;
        }

        return isManual || this.cebStrategy.shouldEnter(tradingRecord);
    }

    public boolean shouldExit(boolean isSpecActive, boolean isManual) {
        TradingRecord tradingRecord = isSpecActive ? getTradingRecord() : getBacktestRecord();
        if (notInPosition(tradingRecord)) {
            log.info("you have no position!");
            return false;
        }

        return isManual || this.cebStrategy.shouldExit(tradingRecord);
    }

    public org.ta4j.core.Trade newTrade(boolean isSpecActive, Pair<Num, Num> priceAmount) {
        TradingRecord tradingRecord = isSpecActive ? getTradingRecord() : getBacktestRecord();
        int endIndex = getSeries().getEndIndex();

        if (isSpecActive) {
            tradingRecord.operate(endIndex, priceAmount.getKey(), priceAmount.getValue());
        } else {
            Num closePrice = getSeries().getLastBar().getClosePrice();
            tradingRecord.operate(endIndex, closePrice, DecimalNum.valueOf(1));
        }

        return tradingRecord.getLastTrade();
    }

    public boolean notInPosition(TradingRecord tradingRecord) {
        return !Optional.ofNullable(tradingRecord).orElseGet(this::getTradingRecord).getCurrentPosition().isOpened();
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
            this.backtestRecord = manager.run(this.cebStrategy.getStrategy());
        }

        return this.backtestRecord;
    }
}
