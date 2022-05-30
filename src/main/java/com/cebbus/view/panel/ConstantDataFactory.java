package com.cebbus.view.panel;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.binance.SymbolLoader;

import java.util.Arrays;

public final class ConstantDataFactory {

    private static final Integer[] SIZES = {2, 4, 8, 16, 32, 64, 128, 256, 512, 1000};
    private static final String[] INTERVALS = Arrays.stream(CandlestickInterval.values()).map(Enum::name).toArray(String[]::new);
    private static final String[] SYMBOLS = SymbolLoader.getSymbolList().toArray(String[]::new);

    static {
        Arrays.sort(SYMBOLS);
    }

    private ConstantDataFactory() {
    }

    public static String[] getSymbols() {
        return SYMBOLS;
    }

    public static String[] getIntervals() {
        return INTERVALS;
    }

    public static Integer[] getSizes() {
        return SIZES;
    }
}
