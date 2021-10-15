package com.cebbus.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.binance.listener.CandlestickEventListener;
import com.cebbus.binance.listener.operation.TradeOperation;
import com.cebbus.binance.listener.operation.UpdateCacheOperation;
import com.cebbus.binance.listener.operation.UpdateSeriesOperation;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.chart.panel.CryptoChartPanel;
import com.cebbus.util.LimitedHashMap;
import com.cebbus.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Speculator {

    private static final String SYMBOL = PropertyReader.getSymbol();
    private static final CandlestickInterval INTERVAL = PropertyReader.getInterval();
    private static final Map<Long, Candlestick> CACHE = new LimitedHashMap<>();

    private final BinanceApiRestClient restClient;

    private TradeStatus status;
    private TheOracle theOracle;
    private CryptoChartPanel chartPanel;

    public Speculator() {
        this.restClient = ClientFactory.restClient();
    }

    public void loadHistory() {
        List<Candlestick> bars = this.restClient.getCandlestickBars(SYMBOL, INTERVAL);
        bars.forEach(candlestick -> CACHE.put(candlestick.getCloseTime(), candlestick));
    }

    public void startSpec() {
        Objects.requireNonNull(this.theOracle);
        Objects.requireNonNull(this.chartPanel);

        CandlestickEventListener listener = new CandlestickEventListener(List.of(
                new UpdateCacheOperation(CACHE),
                new UpdateSeriesOperation(this.theOracle.getSeries()),
                new TradeOperation(this.theOracle, this),
                response -> this.chartPanel.refresh()));

        try (BinanceApiWebSocketClient client = ClientFactory.webSocketClient()) {
            client.onCandlestickEvent(SYMBOL.toLowerCase(), INTERVAL, listener);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Bar> convertToBarList() {
        return CACHE.values().stream().map(BarMapper::valueOf).collect(Collectors.toList());
    }

    public boolean buy() {
        return trade(true);
    }

    public boolean sell() {
        return trade(false);
    }

    public void activate() {
        this.status = TradeStatus.ACTIVE;
        this.chartPanel.changeStatus(TradeStatus.ACTIVE);
    }

    public void deactivate() {
        this.status = TradeStatus.INACTIVE;
        this.chartPanel.changeStatus(TradeStatus.INACTIVE);
    }

    public boolean isActive() {
        return status == null || status == TradeStatus.ACTIVE;
    }

    public BinanceApiRestClient getRestClient() {
        return restClient;
    }

    public void setTheOracle(TheOracle theOracle) {
        this.theOracle = theOracle;
    }

    public void setChartPanel(CryptoChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    private boolean trade(boolean isBuy) {
        TradeOperation trader = new TradeOperation(this.theOracle, this);
        boolean success = isBuy ? trader.manualEnter() : trader.manualExit();
        if (success) {
            this.chartPanel.refresh();
        }

        return success;
    }
}
