package com.cebbus.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.cebbus.binance.mapper.CandlestickMapper;

import java.util.Map;

public class UpdateCacheOperation implements EventOperation {

    private final Map<Long, Candlestick> cache;

    public UpdateCacheOperation(Map<Long, Candlestick> cache) {
        this.cache = cache;
    }

    @Override
    public void operate(CandlestickEvent response) {
        Long closeTime = response.getCloseTime();
        Candlestick candlestick = CandlestickMapper.valueOf(response);

        this.cache.put(closeTime, candlestick);
    }
}
