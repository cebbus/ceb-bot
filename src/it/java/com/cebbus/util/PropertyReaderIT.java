package com.cebbus.util;

import com.cebbus.analysis.Symbol;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.dto.CsIntervalAdapter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropertyReaderIT {

    @Test
    void getApiKey() {
        String actual = PropertyReader.getApiKey();
        assertEquals("kjPvnb6TMJvaR33nXoVUtudaRBHAJKHDssurQkgZtikkuAtNbwuleJLJe3RJkPCI", actual);
    }

    @Test
    void getApiSecret() {
        String actual = PropertyReader.getApiSecret();
        assertEquals("yuUBaD6IwszVn8KGqpCRyYKgmc2ImUKeqaqI9ipBf9MF3rO0k3kp9nx7pEazRruY", actual);
    }

    @Test
    void getCacheSize() {
        Integer actual = PropertyReader.getCacheSize();
        assertEquals(90, actual);
    }

    @Test
    void isCredentialsExist() {
        assertTrue(PropertyReader.isCredentialsExist());
    }

    @Test
    void isTestProfile() {
        assertTrue(PropertyReader.isTestProfile());
    }

    @Test
    void isDevelopmentProfile() {
        assertFalse(PropertyReader.isDevelopmentProfile());
    }

    @Test
    void isProductionProfile() {
        assertFalse(PropertyReader.isProductionProfile());
    }

    @Test
    void getSymbols() {
        List<Symbol> expected = new ArrayList<>();
        expected.add(new Symbol(0, 0.9, "BNB", "USDT", "MacdStrategy", CsIntervalAdapter.ONE_MINUTE, TradeStatus.ACTIVE));
        expected.add(new Symbol(1, 0.1, "ETH", "USDT", "MacdStrategy", CsIntervalAdapter.ONE_MINUTE, TradeStatus.ACTIVE));
        expected.add(new Symbol(2, 0.0, "XRP", "USDT", "MacdStrategy", CsIntervalAdapter.ONE_MINUTE, TradeStatus.INACTIVE));

        List<Symbol> actual = PropertyReader.getSymbols();
        assertIterableEquals(expected, actual);
    }
}
