package com.cebbus.analysis;

import com.cebbus.binance.order.TradeStatus;
import com.cebbus.dto.CsIntervalAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SymbolTest {

    private Symbol symbol;

    @BeforeEach
    void setUp() {
        this.symbol = new Symbol(0, 0d, "BASE", "QUOTE", "Junk", CsIntervalAdapter.ONE_MINUTE, TradeStatus.INACTIVE);
    }

    @Test
    void getId() {
        assertEquals(0, this.symbol.getId());
    }

    @Test
    void getWeight() {
        assertEquals(0d, this.symbol.getWeight());
    }

    @Test
    void getBase() {
        assertEquals("BASE", this.symbol.getBase());
    }

    @Test
    void getQuote() {
        assertEquals("QUOTE", this.symbol.getQuote());
    }

    @Test
    void getStrategy() {
        assertEquals("Junk", this.symbol.getStrategy());
    }

    @Test
    void getInterval() {
        assertEquals(CsIntervalAdapter.ONE_MINUTE, this.symbol.getInterval());
    }

    @Test
    void getStatus() {
        assertEquals(TradeStatus.INACTIVE, this.symbol.getStatus());
    }

    @Test
    void getName() {
        assertEquals("BASEQUOTE", this.symbol.getName());
    }
}