package com.cebbus.analysis.mapper;

import com.cebbus.dto.TradeDto;
import com.cebbus.util.DateTimeUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TradeMapper {

    private final BarSeries series;
    private final List<TradeDto> tradeList;
    private final List<TradeDto> cumulativeTradeList = new ArrayList<>();
    private final Map<Integer, List<TradeDto>> cumulativeTradeMap = new LinkedHashMap<>();

    public TradeMapper(BarSeries series, List<TradeDto> tradeList) {
        this.series = series;
        this.tradeList = tradeList;

        accumulate();
        prepareTradeMap();
    }

    private void accumulate() {
        if (this.tradeList.isEmpty()) {
            return;
        }

        Map<String, List<TradeDto>> map = new LinkedHashMap<>();
        this.tradeList.forEach(t -> map.computeIfAbsent(t.getOrderId(), id -> new ArrayList<>()).add(t));

        List<TradeDto> tempTradeList = map.values().stream()
                .map(this::calculateAvg)
                .sorted(Comparator.comparingLong(TradeDto::getTime))
                .collect(Collectors.toList());

        List<TradeDto> buys = new ArrayList<>();
        List<TradeDto> sells = new ArrayList<>();
        for (TradeDto trade : tempTradeList) {
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

        for (TradeDto trade : this.cumulativeTradeList) {
            timeIndexMap.computeIfAbsent(trade.getTime(), this::getSeriesIndex);
        }

        boolean first = true;
        for (TradeDto trade : this.cumulativeTradeList) {
            int index = timeIndexMap.get(trade.getTime());

            //positions must start with buy
            if (index == -1 || (first && !trade.isBuyer())) {
                continue;
            }

            first = false;

            this.cumulativeTradeMap.computeIfAbsent(index, integer -> new ArrayList<>()).add(trade);
        }
    }

    private TradeDto calculateAvg(List<TradeDto> subTradeList) {
        if (subTradeList.size() == 1) {
            return subTradeList.get(0);
        }

        TradeDto lastTrade = subTradeList.get(subTradeList.size() - 1);

        TradeDto trade = new TradeDto();
        trade.setId(lastTrade.getId());
        trade.setSymbol(lastTrade.getSymbol());
        trade.setTime(lastTrade.getTime());
        trade.setBuyer(lastTrade.isBuyer());
        trade.setMaker(lastTrade.isMaker());
        trade.setBestMatch(lastTrade.isBestMatch());
        trade.setOrderId(lastTrade.getOrderId());
        trade.setQty(0);
        trade.setQuoteQty(0);

        subTradeList.forEach(t -> {
            trade.setQty(sum(trade.getQty(), t.getQty()));
            trade.setQuoteQty(sum(trade.getQuoteQty(), t.getQuoteQty()));
        });

        trade.setPrice(divide(trade.getQuoteQty(), trade.getQty()));

        return trade;
    }

    private Number sum(Number v1, Number v2) {
        if (BigDecimal.class.isAssignableFrom(v1.getClass())
                && BigDecimal.class.isAssignableFrom(v2.getClass())) {
            return ((BigDecimal) v1).add((BigDecimal) v2);
        } else {
            return v1.doubleValue() + v2.doubleValue();
        }
    }

    private Number divide(Number v1, Number v2) {
        if (BigDecimal.class.isAssignableFrom(v1.getClass())
                && BigDecimal.class.isAssignableFrom(v2.getClass())) {
            return ((BigDecimal) v1).divide(((BigDecimal) v2), RoundingMode.DOWN);
        } else {
            return v1.doubleValue() / v2.doubleValue();
        }
    }

    private void addToCumulativeList(List<TradeDto> subTradeList) {
        if (subTradeList.isEmpty()) {
            return;
        }

        this.cumulativeTradeList.add(calculateAvg(subTradeList));
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

    public List<Trade> getTradeHistory() {
        List<Trade> tList = new ArrayList<>();
        this.cumulativeTradeMap.forEach((index, trades) -> trades.forEach(t -> tList.add(valueOf(index, t))));

        return tList;
    }

    private Trade valueOf(Integer index, TradeDto dto) {
        Num price = DecimalNum.valueOf(dto.getPrice());
        Num amount = DecimalNum.valueOf(dto.getQty());

        if (dto.isBuyer()) {
            return Trade.buyAt(index, price, amount);
        } else {
            return Trade.sellAt(index, price, amount);
        }
    }

    public static TradeDto tradeToDto(Trade trade) {
        TradeDto dto = new TradeDto();
        dto.setId((long) trade.getIndex());
        dto.setQty(trade.getAmount().getDelegate());
        dto.setPrice(trade.getNetPrice().getDelegate());
        dto.setBuyer(trade.isBuy());
        dto.setMaker(trade.isSell());

        return dto;
    }
}
