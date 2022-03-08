package com.cebbus.binance.mapper;

import com.binance.api.client.domain.account.Trade;
import com.cebbus.util.DateTimeUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.*;

public class TradeMapper {

    private final BarSeries series;
    private final List<Trade> tradeList;
    private final List<Trade> cumulativeTradeList = new ArrayList<>();
    private final Map<Integer, List<Trade>> cumulativeTradeMap = new LinkedHashMap<>();

    public TradeMapper(BarSeries series, List<Trade> tradeList) {
        this.series = series;
        this.tradeList = tradeList;

        accumulate();
        prepareTradeMap();
    }

    private void accumulate() {
        if (this.tradeList.isEmpty()) {
            return;
        }

        Map<String, List<Trade>> map = new LinkedHashMap<>();
        for (Trade trade : this.tradeList) {
            map.putIfAbsent(trade.getOrderId(), new ArrayList<>());
            map.get(trade.getOrderId()).add(trade);
        }

        List<Trade> tempTradeList = new ArrayList<>();
        map.forEach((orderId, subTradeList) -> {
            if (subTradeList.size() == 1) {
                tempTradeList.add(subTradeList.get(0));
            } else {
                tempTradeList.add(calculateAvg(subTradeList));
            }
        });

        tempTradeList.sort(Comparator.comparingLong(Trade::getTime));

        List<Trade> buys = new ArrayList<>();
        List<Trade> sells = new ArrayList<>();
        for (Trade trade : tempTradeList) {
            if (trade.isBuyer()) {
                buys.add(trade);
                addToCumulativeList(sells);
            } else {
                sells.add(trade);
                addToCumulativeList(buys);
            }
        }

        addToCumulativeList(buys);
        addToCumulativeList(sells);
    }

    private void prepareTradeMap() {
        Map<Long, Integer> timeIndexMap = new HashMap<>();

        for (Trade trade : this.cumulativeTradeList) {
            timeIndexMap.computeIfAbsent(trade.getTime(), this::getSeriesIndex);
        }

        boolean first = true;
        for (Trade trade : this.cumulativeTradeList) {
            int index = timeIndexMap.get(trade.getTime());

            //positions must start with buy
            if (index == -1 || (first && !trade.isBuyer())) {
                continue;
            }

            first = false;

            this.cumulativeTradeMap.computeIfAbsent(index, integer -> new ArrayList<>());
            this.cumulativeTradeMap.get(index).add(trade);
        }
    }

    private Trade calculateAvg(List<Trade> subTradeList) {
        Trade lastTrade = subTradeList.get(subTradeList.size() - 1);

        Trade trade = new Trade();
        trade.setId(lastTrade.getId());
        trade.setSymbol(lastTrade.getSymbol());
        trade.setTime(lastTrade.getTime());
        trade.setBuyer(lastTrade.isBuyer());
        trade.setMaker(lastTrade.isMaker());
        trade.setBestMatch(lastTrade.isBestMatch());
        trade.setOrderId(lastTrade.getOrderId());
        trade.setQty("0");
        trade.setQuoteQty("0");

        subTradeList.forEach(t -> {
            trade.setQty(sum(trade.getQty(), t.getQty()));
            trade.setQuoteQty(sum(trade.getQuoteQty(), t.getQuoteQty()));
        });

        trade.setPrice(divide(trade.getQuoteQty(), trade.getQty()));

        return trade;
    }

    private String sum(String v1, String v2) {
        BigDecimal firstVal = new BigDecimal(v1);
        BigDecimal secondVal = new BigDecimal(v2);

        return firstVal.add(secondVal).toPlainString();
    }

    private String divide(String v1, String v2) {
        BigDecimal firstVal = new BigDecimal(v1);
        BigDecimal secondVal = new BigDecimal(v2);

        return firstVal.divide(secondVal, RoundingMode.DOWN).toPlainString();
    }

    private void addToCumulativeList(List<Trade> subTradeList) {
        if (subTradeList.size() > 1) {
            this.cumulativeTradeList.add(calculateAvg(subTradeList));
        } else {
            this.cumulativeTradeList.addAll(subTradeList);
        }

        subTradeList.clear();
    }

    private int getSeriesIndex(long time) {
        ZonedDateTime entryTime = DateTimeUtil.millisToZonedTime(time);

        int startIndex = Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
        int endIndex = this.series.getEndIndex();
        for (int i = startIndex; i <= endIndex; i++) {
            Bar bar = this.series.getBar(i);

            ZonedDateTime beginTime = bar.getBeginTime();
            ZonedDateTime endTime = bar.getEndTime();
            if ((beginTime.isBefore(entryTime) || beginTime.isEqual(entryTime))
                    && (endTime.isAfter(entryTime) || endTime.isEqual(entryTime))) {
                return i;
            }
        }

        return -1;
    }

    public Map<Integer, List<Trade>> getTradeMap() {
        return cumulativeTradeMap;
    }
}
