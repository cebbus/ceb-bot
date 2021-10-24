package com.cebbus.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.market.Candlestick;
import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.binance.listener.CandlestickEventListener;
import com.cebbus.binance.listener.operation.EventOperation;
import com.cebbus.binance.listener.operation.TradeOperation;
import com.cebbus.binance.listener.operation.UpdateCacheOperation;
import com.cebbus.binance.listener.operation.UpdateSeriesOperation;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.util.LimitedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class Speculator {

    private final Symbol symbol;
    private final BinanceApiRestClient restClient;
    private final Map<Long, Candlestick> candlestickCache = new LimitedHashMap<>();

    private final CandlestickEventListener listener = new CandlestickEventListener();
    private final List<Consumer<Boolean>> manualTradeListeners = new ArrayList<>();
    private final List<Consumer<TradeStatus>> statusChangeListeners = new ArrayList<>();

    private TradeStatus status;
    private TheOracle theOracle;

    public Speculator(Symbol symbol) {
        this.symbol = symbol;
        this.status = symbol.getStatus();
        this.restClient = ClientFactory.restClient();
    }

    public void loadHistory() {
        List<Candlestick> bars = this.restClient.getCandlestickBars(this.symbol.getName(), this.symbol.getInterval());
        bars.forEach(candlestick -> this.candlestickCache.put(candlestick.getCloseTime(), candlestick));
    }

    public void checkOpenPosition() {
        Objects.requireNonNull(this.theOracle);

        Optional<Trade> optTrade = getLastTrade();
        Optional<Order> optOrder = optTrade.isEmpty() ? Optional.empty() : getOrder(optTrade.get().getOrderId());
        if (optOrder.isEmpty()) {
            return;
        }

        Trade trade = optTrade.get();
        Order order = optOrder.get();
        if (order.getSide() == OrderSide.SELL) {
            return;
        }

        int index = getSeriesIndex(order.getTime());
        if (index == -1) {
            return;
        }

        Num price = DecimalNum.valueOf(trade.getPrice());
        Num amount = DecimalNum.valueOf(order.getExecutedQty());
        this.theOracle.getTradingRecord().enter(index, price, amount);
    }

    public void startSpec() {
        Objects.requireNonNull(this.theOracle);

        this.listener.addOperation(new UpdateCacheOperation(this.candlestickCache));
        this.listener.addOperation(new UpdateSeriesOperation(this.theOracle.getSeries()));
        this.listener.addOperation(new TradeOperation(this.theOracle, this));

        try (BinanceApiWebSocketClient client = ClientFactory.webSocketClient()) {
            client.onCandlestickEvent(this.symbol.getName().toLowerCase(), this.symbol.getInterval(), this.listener);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Bar> convertToBarList() {
        return this.candlestickCache.values().stream().map(BarMapper::valueOf).collect(Collectors.toList());
    }

    public void addCandlestickEventOperation(EventOperation operation) {
        this.listener.addOperation(operation);
    }

    public void addStatusChangeListener(Consumer<TradeStatus> operation) {
        this.statusChangeListeners.add(operation);
    }

    public void addManualTradeListeners(Consumer<Boolean> operation) {
        this.manualTradeListeners.add(operation);
    }

    public boolean buy() {
        return trade(true);
    }

    public boolean sell() {
        return trade(false);
    }

    public void activate() {
        this.status = TradeStatus.ACTIVE;
        this.statusChangeListeners.forEach(o -> o.accept(this.status));
    }

    public void deactivate() {
        this.status = TradeStatus.INACTIVE;
        this.statusChangeListeners.forEach(o -> o.accept(this.status));
    }

    public boolean isActive() {
        return status == null || status == TradeStatus.ACTIVE;
    }

    public List<Pair<String, Num>> calcStrategies() {
        return this.theOracle.calcStrategies();
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public BinanceApiRestClient getRestClient() {
        return restClient;
    }

    public void setTheOracle(TheOracle theOracle) {
        this.theOracle = theOracle;
    }

    private boolean trade(boolean isBuy) {
        TradeOperation trader = new TradeOperation(this.theOracle, this);
        boolean success = isBuy ? trader.manualEnter() : trader.manualExit();

        this.manualTradeListeners.forEach(o -> o.accept(success));
        return success;
    }

    private Optional<Trade> getLastTrade() {
        List<Trade> trades = this.restClient.getMyTrades(this.symbol.getName());
        trades.sort((o1, o2) -> Long.compare(o2.getTime(), o1.getTime()));
        return trades.isEmpty() ? Optional.empty() : Optional.of(trades.get(0));
    }

    private Optional<Order> getOrder(String orderId) {
        List<Order> orders = this.restClient.getAllOrders(new AllOrdersRequest(this.symbol.getName()));
        return orders.stream().filter(o -> o.getOrderId().toString().equals(orderId)).findFirst();
    }

    private int getSeriesIndex(long time) {
        Instant instant = Instant.ofEpochMilli(time);
        ZonedDateTime entryTime = ZonedDateTime.ofInstant(instant, ZoneId.of("GMT+3"));

        BarSeries series = this.theOracle.getSeries();

        int startIndex = Math.max(series.getRemovedBarsCount(), series.getBeginIndex());
        int endIndex = series.getEndIndex();
        for (int i = startIndex; i < endIndex; i++) {
            Bar bar = series.getBar(i);

            ZonedDateTime beginTime = bar.getBeginTime();
            ZonedDateTime endTime = bar.getEndTime();
            if ((beginTime.isBefore(entryTime) || beginTime.isEqual(entryTime))
                    && (endTime.isAfter(entryTime) || endTime.isEqual(entryTime))) {
                return i;
            }
        }

        return -1;
    }
}
