package com.cebbus.binance;

import com.cebbus.analysis.Symbol;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.util.PropertyReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpeculatorIT {

    private static final int LIMIT = 5;

    private Speculator speculator;

    @BeforeEach
    void setUp() {
        Symbol symbol = PropertyReader.getSymbols().get(0);

        this.speculator = new Speculator(symbol, LIMIT);
        this.speculator.loadHistory();
    }

    @Test
    void loadHistory() {
        assertEquals(LIMIT - 1, this.speculator.convertToBarList().size());
    }

    @Test
    void activate() {
        this.speculator.activate();
        assertEquals(TradeStatus.ACTIVE, this.speculator.getStatus());
    }

    @Test
    void deactivate() {
        this.speculator.deactivate();
        assertEquals(TradeStatus.INACTIVE, this.speculator.getStatus());
    }

    @Test
    void isActive() {
        assertTrue(this.speculator.isActive());
    }

    @Test
    void isActiveStatusNull() {
        this.speculator.setStatus(null);
        assertTrue(this.speculator.isActive());
    }

}
