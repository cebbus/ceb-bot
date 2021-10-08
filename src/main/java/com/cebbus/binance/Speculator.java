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
import com.cebbus.chart.CryptoChartPanel;
import com.cebbus.util.LimitedHashMap;
import com.cebbus.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Speculator {

    private static final String SYMBOL = PropertyReader.getSymbol();
    private static final CandlestickInterval INTERVAL = PropertyReader.getInterval();
    private static final Map<Long, Candlestick> CACHE = new LimitedHashMap<>();

    private final BinanceApiRestClient restClient;

    public Speculator() {
        this.restClient = ClientFactory.restClient();
    }

    public void loadHistory() {
        List<Candlestick> bars = this.restClient.getCandlestickBars(SYMBOL, INTERVAL);
        bars.forEach(candlestick -> CACHE.put(candlestick.getCloseTime(), candlestick));
    }

    public void startSpec(TheOracle theOracle, CryptoChartPanel chartPanel) {

        CandlestickEventListener listener = new CandlestickEventListener(List.of(
                new UpdateCacheOperation(CACHE),
                new UpdateSeriesOperation(theOracle.getSeries()),
                new TradeOperation(theOracle, this.restClient),
                response -> chartPanel.refresh()));

        try (BinanceApiWebSocketClient client = ClientFactory.webSocketClient()) {
            client.onCandlestickEvent(SYMBOL.toLowerCase(), INTERVAL, listener);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Bar> convertToBarList() {
        return CACHE.values().stream().map(BarMapper::valueOf).collect(Collectors.toList());
    }

}
