package com.cebbus.analysis;

import com.cebbus.binance.order.TradeStatus;
import com.cebbus.dto.CsIntervalAdapter;
import lombok.Data;

@Data
public class Symbol {
    private final int id;
    private final double weight;
    private final String base;
    private final String quote;
    private final String strategy;
    private final CsIntervalAdapter interval;
    private final TradeStatus status;

    public String getName() {
        return this.base + this.quote;
    }

    public Symbol copy(String newStrategy) {
        return new Symbol(this.id, this.weight, this.base, this.quote, newStrategy, this.interval, this.status);
    }
}
