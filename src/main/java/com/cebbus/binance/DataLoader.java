package com.cebbus.binance;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.binance.mapper.CandlestickMapper;
import com.cebbus.util.LimitedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DataLoader {

    private static final Map<Long, Candlestick> CACHE = new LimitedHashMap<>();

    private final String symbol;
    private final CandlestickInterval interval;

    public DataLoader(String symbol, CandlestickInterval interval) {
        this.symbol = symbol;
        this.interval = interval;
    }

    public void loadHistory() {
        BinanceApiRestClient client = ClientFactory.restClient();

        List<Candlestick> bars = client.getCandlestickBars(this.symbol.toUpperCase(), this.interval);
        bars.forEach(candlestick -> CACHE.put(candlestick.getCloseTime(), candlestick));
    }

    public void startStream(BinanceApiCallback<Candlestick> callback) {
        try (BinanceApiWebSocketClient client = ClientFactory.webSocketClient()) {
            client.onCandlestickEvent(this.symbol.toLowerCase(), this.interval, response -> {
                if (Boolean.TRUE.equals(response.getBarFinal())) {
                    Long closeTime = response.getCloseTime();
                    Candlestick candlestick = CandlestickMapper.valueOf(response);

                    CACHE.put(closeTime, candlestick);
                    log.info(String.format("New stick! Symbol: %s", response.getSymbol()));

                    callback.onResponse(candlestick);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Bar> convertToBarList() {
        return CACHE.values().stream().map(BarMapper::valueOf).collect(Collectors.toList());
    }

    public Collection<Candlestick> getValues() {
        return CACHE.values();
    }
}
