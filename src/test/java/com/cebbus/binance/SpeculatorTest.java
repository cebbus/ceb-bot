package com.cebbus.binance;

import com.cebbus.analysis.Symbol;
import com.cebbus.util.PropertyReader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

class SpeculatorTest {

    @Test
    void loadTradeHistory() {
        Symbol symbol = new Symbol(1, 0d, "", "", "", null, null);
        Speculator speculator = new Speculator(symbol);

        try (MockedStatic<PropertyReader> propertyReaderMock = mockStatic(PropertyReader.class)) {
            propertyReaderMock.when(PropertyReader::isCredentialsExist).thenReturn(false);
            assertDoesNotThrow(speculator::loadTradeHistory);
        }
    }
}