package com.cebbus.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
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
import org.ta4j.core.Bar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        this.restClient = ClientFactory.restClient();
    }

    public void loadHistory() {
        List<Candlestick> bars = this.restClient.getCandlestickBars(this.symbol.getName(), this.symbol.getInterval());
        bars.forEach(candlestick -> this.candlestickCache.put(candlestick.getCloseTime(), candlestick));
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
}
