package com.cebbus.analysis;

import com.cebbus.dto.TradePointDto;
import com.cebbus.dto.TradeRowDto;
import com.cebbus.util.DateTimeUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    TradeDataHelper(
            BarSeries series,
            TradingRecord tradingRecord,
            TradingRecord backtestRecord,
            List<Trade> tradeHistoryList) {
        this(series, tradingRecord, backtestRecord);
        tradeHistoryList.forEach(t -> tradingRecord.operate(t.getIndex(), t.getNetPrice(), t.getAmount()));
    }

    public Trade newTrade(boolean isSpecActive, Pair<Number, Number> priceAmount) {
        TradingRecord tr = isSpecActive ? this.tradingRecord : this.backtestRecord;
        int endIndex = this.series.getEndIndex();

        if (isSpecActive) {
            tr.operate(endIndex,
                    DecimalNum.valueOf(priceAmount.getKey()),
                    DecimalNum.valueOf(priceAmount.getValue()));
        } else {
            Num closePrice = this.series.getLastBar().getClosePrice();
            tr.operate(endIndex, closePrice, DecimalNum.valueOf(1));
        }

        return tr.getLastTrade();
    }

    public List<TradePointDto> getTradePointList() {
        List<TradePointDto> pointList = new ArrayList<>();
        pointList.addAll(prepareTradePointList(false));
        pointList.addAll(prepareTradePointList(true));

        return pointList;
    }

    public Optional<TradePointDto> getLastTradePoint(boolean backtest) {
        Trade bufferTrade = !backtest ? this.tradingRecord.getLastTrade() : this.backtestRecord.getLastTrade();
        return bufferTrade == null ? Optional.empty() : Optional.of(createTradePoint(bufferTrade, backtest));
    }

    public Optional<TradeRowDto> getLastTradeRow(boolean backtest) {
        Trade bufferTrade = !backtest ? this.tradingRecord.getLastTrade() : this.backtestRecord.getLastTrade();
        return bufferTrade == null ? Optional.empty() : Optional.of(createTradeRow(bufferTrade));
    }

    public List<TradeRowDto> getTradeRowList(boolean backtest) {
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

    private List<TradePointDto> prepareTradePointList(boolean backtest) {
        List<TradePointDto> pointList = new ArrayList<>();

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

    private TradePointDto createTradePoint(Trade trade, boolean backtest) {
        Bar bar = this.series.getBar(trade.getIndex());
        Long tradeTime = DateTimeUtil.zonedTimeToMillis(bar.getBeginTime());
        return new TradePointDto(trade.isBuy(), backtest, tradeTime);
    }

    private List<Trade> positionToTradeList(Position position) {
        return List.of(position.getEntry(), position.getExit());
    }

    private TradeRowDto createTradeRow(Trade trade) {
        int index = trade.getIndex();
        Number price = trade.getNetPrice().getDelegate();
        Number amount = trade.getAmount().getDelegate();
        ZonedDateTime dateTime = this.series.getBar(index).getEndTime();
        Long tradeTime = DateTimeUtil.zonedTimeToMillis(dateTime);

        return new TradeRowDto(index, trade.isBuy(), amount, price, tradeTime);
    }

    void setTradingRecord(TradingRecord tradingRecord) {
        this.tradingRecord = tradingRecord;
    }

    void setBacktestRecord(TradingRecord backtestRecord) {
        this.backtestRecord = backtestRecord;
    }
}
