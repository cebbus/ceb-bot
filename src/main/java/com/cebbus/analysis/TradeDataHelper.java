package com.cebbus.analysis;

import com.cebbus.binance.mapper.TradeMapper;
import com.cebbus.util.DateTimeUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.time.RegularTimePeriod;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TradeDataHelper {

    private final BarSeries series;

    private TradingRecord tradingRecord;
    private TradingRecord backtestRecord;

    TradeDataHelper(BarSeries series, TradingRecord tradingRecord, TradingRecord backtestRecord) {
        this.series = series;
        setTradingRecord(tradingRecord);
        setBacktestRecord(backtestRecord);
    }

    public void fillTradeHistory(List<com.binance.api.client.domain.account.Trade> tradeList) {
        TradeMapper tradeMapper = new TradeMapper(this.series, tradeList);
        Map<Integer, List<com.binance.api.client.domain.account.Trade>> tradeMap = tradeMapper.getTradeMap();

        tradeMap.forEach((index, trades) -> {
            for (com.binance.api.client.domain.account.Trade trade : trades) {
                Num price = DecimalNum.valueOf(trade.getPrice());
                Num amount = DecimalNum.valueOf(trade.getQty());

                if (trade.isBuyer()) {
                    this.tradingRecord.enter(index, price, amount);
                } else {
                    this.tradingRecord.exit(index, price, amount);
                }
            }
        });
    }

    public Trade newTrade(boolean isSpecActive, Pair<Num, Num> priceAmount) {
        TradingRecord tr = isSpecActive ? this.tradingRecord : this.backtestRecord;
        int endIndex = this.series.getEndIndex();

        if (isSpecActive) {
            tr.operate(endIndex, priceAmount.getKey(), priceAmount.getValue());
        } else {
            Num closePrice = this.series.getLastBar().getClosePrice();
            tr.operate(endIndex, closePrice, DecimalNum.valueOf(1));
        }

        return tr.getLastTrade();
    }

    public List<Object[]> getTradePointList() {
        List<Object[]> pointList = new ArrayList<>();
        pointList.addAll(prepareTradePointList(false));
        pointList.addAll(prepareTradePointList(true));

        return pointList;
    }

    public Optional<Object[]> getLastTradePoint(boolean backtest) {
        Trade bufferTrade = !backtest ? this.tradingRecord.getLastTrade() : this.backtestRecord.getLastTrade();
        return bufferTrade == null ? Optional.empty() : Optional.of(createTradePoint(bufferTrade, backtest));
    }

    public Optional<Object[]> getLastTradeRow(boolean backtest) {
        Trade bufferTrade = !backtest ? this.tradingRecord.getLastTrade() : this.backtestRecord.getLastTrade();
        return bufferTrade == null ? Optional.empty() : Optional.of(createTradeRow(bufferTrade));
    }

    public List<Object[]> getTradeRowList(boolean backtest) {
        List<Trade> tradeList = new ArrayList<>();

        TradingRecord tr = !backtest ? this.tradingRecord : this.backtestRecord;
        List<Position> positionList = tr.getPositions();
        positionList.forEach(p -> tradeList.addAll(positionToTradeList(p)));

        Trade lastTrade = tr.getLastTrade();
        Position lastPosition = tr.getLastPosition();
        if (lastTrade != null && (tradeList.isEmpty() || (lastPosition != null && !lastPosition.getExit().equals(lastTrade)))) {
            tradeList.add(lastTrade);
        }

        return tradeList.stream()
                .sorted(Comparator.comparingInt(Trade::getIndex))
                .map(this::createTradeRow)
                .collect(Collectors.toList());
    }

    private List<Object[]> prepareTradePointList(boolean backtest) {
        List<Object[]> pointList = new ArrayList<>();

        TradingRecord tr = !backtest ? this.tradingRecord : this.backtestRecord;
        Trade last = tr.getLastTrade();
        List<Position> positions = tr.getPositions();

        for (Position position : positions) {
            pointList.add(createTradePoint(position.getEntry(), backtest));
            pointList.add(createTradePoint(position.getExit(), backtest));
        }

        if (last != null && last.isBuy()) {
            pointList.add(createTradePoint(last, backtest));
        }

        return pointList;
    }

    private Object[] createTradePoint(Trade trade, boolean backtest) {
        Object[] point = new Object[3];

        Bar bar = this.series.getBar(trade.getIndex());
        RegularTimePeriod period = DateTimeUtil.getBarPeriod(bar);
        double barTime = period.getFirstMillisecond();

        point[0] = barTime;
        point[1] = trade.isBuy();
        point[2] = backtest;

        return point;
    }

    private List<Trade> positionToTradeList(Position position) {
        return List.of(position.getEntry(), position.getExit());
    }

    private Object[] createTradeRow(Trade trade) {
        int index = trade.getIndex();
        ZonedDateTime dateTime = this.series.getBar(index).getEndTime();
        String formattedTime = dateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

        Object[] row = new Object[6];
        row[0] = index;
        row[1] = formattedTime;
        row[2] = trade.isBuy() ? "B" : "S";
        row[3] = trade.getAmount();
        row[4] = trade.getNetPrice();
        row[5] = trade.getAmount().multipliedBy(trade.getNetPrice());

        return row;
    }

    void setTradingRecord(TradingRecord tradingRecord) {
        this.tradingRecord = tradingRecord;
    }

    void setBacktestRecord(TradingRecord backtestRecord) {
        this.backtestRecord = backtestRecord;
    }
}
