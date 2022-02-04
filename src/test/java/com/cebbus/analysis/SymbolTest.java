package com.cebbus.analysis;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.binance.order.TradeStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SymbolTest {

    private Symbol symbol;

    @BeforeEach
    void setUp() {
        this.symbol = new Symbol(0, 0d, "BASE", "QUOTE", "Junk", CandlestickInterval.ONE_MINUTE, TradeStatus.INACTIVE);
    }

    @Test
    void getName() {
        Assertions.assertEquals("BASEQUOTE", this.symbol.getName());
    }
}