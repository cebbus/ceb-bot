package com.cebbus.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.listener.CandlestickEventListener;
import com.cebbus.binance.listener.operation.EventOperation;
import com.cebbus.binance.listener.operation.TradeOperation;
import com.cebbus.binance.listener.operation.UpdateSeriesOperation;
import com.cebbus.binance.mapper.CandlestickMapper;
import com.cebbus.binance.mapper.TradeMapper;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.dto.CandleDto;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.dto.TradeDto;
import com.cebbus.properties.Symbol;
import com.cebbus.util.PropertyReader;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        this(symbol, false);
    }

    public Speculator(Symbol symbol, int limit) {
        this(symbol, limit, false);
    }

    public Speculator(Symbol symbol, boolean initOracle) {
        this(symbol, PropertyReader.getCacheSize(), initOracle);
    }

    public Speculator(Symbol symbol, int limit, boolean initOracle) {
        this.limit = limit;
        this.symbol = symbol;
        this.status = symbol.getStatus();
        this.restClient = ClientFactory.getRestClient();

        if (initOracle) {
            this.theOracle = new TheOracle(symbol, loadTradeHistory(), loadBarHistory());

            this.listener.addOperation(new UpdateSeriesOperation(this));
            this.listener.addOperation(new TradeOperation(this));
        }
    }

    public List<CandleDto> loadBarHistory() {
        return CandlestickMapper.candleToDto(getCandleHistory());
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

    public List<Pair<String, String>> calcStrategies() {
        Objects.requireNonNull(this.theOracle);
        DecimalFormat format = new DecimalFormat("#,###.0000");

        List<Pair<String, Double>> pairs = theOracle.calcStrategies();
        return pairs.stream().map(p -> Pair.of(p.getKey(), format.format(p.getValue()))).collect(Collectors.toList());
    }

    public void changeParameters(Number... parameters) {
        Objects.requireNonNull(this.theOracle);
        this.theOracle.changeProphesyParameters(parameters);
        this.parameterChangeListeners.forEach(o -> o.accept(this));
    }

    public void changeStrategy(String strategy) {
        Objects.requireNonNull(this.theOracle);
        this.theOracle = this.theOracle.changeStrategy(strategy);
    }

    Candlestick getLastCandle() {
        String symName = this.symbol.getName();
        CsIntervalAdapter interval = this.symbol.getInterval();
        CandlestickInterval csInterval = CandlestickInterval.valueOf(interval.name());

        List<Candlestick> bars = this.restClient.getCandlestickBars(symName, csInterval, 2, null, null);
        return bars.get(0);
    }

    void triggerListener(CandlestickEvent event) {
        this.listener.onResponse(event);
    }

    private boolean trade(boolean isBuy) {
        TradeOperation trader = new TradeOperation(this);
        boolean success = isBuy ? trader.manualEnter() : trader.manualExit();

        this.manualTradeListeners.forEach(o -> o.accept(success));
        return success;
    }

    private List<Candlestick> getCandleHistory() {
        String symName = this.symbol.getName();
        CsIntervalAdapter interval = this.symbol.getInterval();
        CandlestickInterval csInterval = CandlestickInterval.valueOf(interval.name());

        List<Candlestick> bars = this.restClient.getCandlestickBars(symName, csInterval, this.limit, null, null);
        bars.remove(bars.size() - 1);

        return bars;
    }

    private List<TradeDto> loadTradeHistory() {
        return TradeMapper.tradeToDto(getTradeHistory());
    }

    private List<Trade> getTradeHistory() {
        if (!PropertyReader.isCredentialsExist()) {
            log.warn("needs credentials!");
            return Collections.emptyList();
        }

        return this.restClient.getMyTrades(this.symbol.getName());
    }
}
