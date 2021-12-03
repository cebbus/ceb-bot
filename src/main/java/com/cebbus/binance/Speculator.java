package com.cebbus.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
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
import com.cebbus.util.PropertyReader;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
@Slf4j
public class Speculator {

    private final int limit;
    private final Symbol symbol;
    private final BinanceApiRestClient restClient;

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final Map<Long, Candlestick> candlestickCache;

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final CandlestickEventListener listener = new CandlestickEventListener();

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final List<Consumer<Boolean>> manualTradeListeners = new CopyOnWriteArrayList<>();

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final List<Consumer<TradeStatus>> statusChangeListeners = new CopyOnWriteArrayList<>();

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private final List<Consumer<Speculator>> parameterChangeListeners = new CopyOnWriteArrayList<>();

    private TradeStatus status;
    private TheOracle theOracle;

    public Speculator(Symbol symbol) {
        this.symbol = symbol;
        this.status = symbol.getStatus();
        this.limit = PropertyReader.getCacheSize();
        this.restClient = ClientFactory.getRestClient();
        this.candlestickCache = LimitedHashMap.create(this.limit);
    }

    public Speculator(Symbol symbol, int limit) {
        this.limit = limit;
        this.symbol = symbol;
        this.status = symbol.getStatus();
        this.restClient = ClientFactory.getRestClient();
        this.candlestickCache = LimitedHashMap.create(this.limit);
    }

    public void loadHistory() {
        String symName = this.symbol.getName();
        CandlestickInterval interval = this.symbol.getInterval();
        List<Candlestick> bars = this.restClient.getCandlestickBars(symName, interval, this.limit, null, null);

        bars.forEach(candlestick -> this.candlestickCache.put(candlestick.getCloseTime(), candlestick));
    }

    public void recordOpenPosition() {
        Pair<Optional<Trade>, Optional<Order>> lastTradeAndOrder = getLastTradeAndOrder();
        if (lastTradeAndOrder.getKey().isEmpty() || lastTradeAndOrder.getValue().isEmpty()) {
            return;
        }

        Order order = lastTradeAndOrder.getValue().get();
        Trade trade = lastTradeAndOrder.getKey().get();
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
        this.listener.addOperation(new TradeOperation(this));

        try (BinanceApiWebSocketClient client = ClientFactory.newWebSocketClient()) {
            client.onCandlestickEvent(this.symbol.getName().toLowerCase(), this.symbol.getInterval(), this.listener);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Bar> convertToBarList() {
        return this.candlestickCache.values().stream().map(BarMapper::valueOf).collect(Collectors.toList());
    }

    public int addCandlestickEventOperation(EventOperation operation) {
        return this.listener.addOperation(operation);
    }

    public void removeCandlestickEventOperation(int index) {
        this.listener.removeOperation(index);
    }

    public int addStatusChangeListener(Consumer<TradeStatus> operation) {
        this.statusChangeListeners.add(operation);
        return this.statusChangeListeners.size() - 1;
    }

    public void removeStatusChangeListener(int index) {
        this.statusChangeListeners.remove(index);
    }

    public void addParameterChangeListener(Consumer<Speculator> operation) {
        this.parameterChangeListeners.add(operation);
    }

    public void clearParameterChangeListener() {
        this.parameterChangeListeners.clear();
    }

    public int addManualTradeListeners(Consumer<Boolean> operation) {
        this.manualTradeListeners.add(operation);
        return this.manualTradeListeners.size() - 1;
    }

    public void removeManualTradeListeners(int index) {
        this.manualTradeListeners.remove(index);
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
        Objects.requireNonNull(this.theOracle);

        return this.theOracle.calcStrategies();
    }

    public void changeParameters(Number... parameters) {
        this.theOracle.changeProphesyParameters(parameters);
        this.theOracle.backtest();

        this.parameterChangeListeners.forEach(o -> o.accept(this));
    }

    private boolean trade(boolean isBuy) {
        TradeOperation trader = new TradeOperation(this);
        boolean success = isBuy ? trader.manualEnter() : trader.manualExit();

        this.manualTradeListeners.forEach(o -> o.accept(success));
        return success;
    }

    private Pair<Optional<Trade>, Optional<Order>> getLastTradeAndOrder() {
        Objects.requireNonNull(this.theOracle);
        if (!PropertyReader.isCredentialsExist()) {
            log.warn("needs credentials!");
            return Pair.of(Optional.empty(), Optional.empty());
        }

        Optional<Trade> optTrade = getLastTrade();
        Optional<Order> optOrder = optTrade.isEmpty() ? Optional.empty() : getOrder(optTrade.get().getOrderId());
        return Pair.of(optTrade, optOrder);
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
        for (int i = startIndex; i <= endIndex; i++) {
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
