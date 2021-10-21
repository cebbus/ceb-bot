package com.cebbus.analysis;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.binance.order.TradeStatus;
import lombok.Data;

@Data
public class Symbol {
    private final String base;
    private final String quote;
    private final String strategy;
    private final CandlestickInterval interval;
    private final TradeStatus status;

    public String getName() {
        return this.base + this.quote;
    }
}
