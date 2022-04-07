package com.cebbus;

import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.util.PropertyReader;
import com.cebbus.util.SpeculatorHolder;
import com.cebbus.view.panel.CryptoAppFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CebBotIT {

    private Speculator speculator;

    @BeforeEach
    void setUp() {
        CryptoAppFrame appFrame = new CryptoAppFrame();

        Symbol symbol = PropertyReader.getSymbols().get(0);

        SpeculatorHolder specHolder = SpeculatorHolder.getInstance();
        this.speculator = new Speculator(symbol);
        this.speculator.loadHistory();

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(symbol.getName())
                .withBars(this.speculator.convertToBarList())
                .withMaxBarCount(PropertyReader.getCacheSize())
                .build();

        CebStrategy cebStrategy = StrategyFactory.create(series, symbol.getStrategy());
        this.speculator.setTheOracle(new TheOracle(cebStrategy));
        this.speculator.loadTradeHistory();

        appFrame.addTab(this.speculator);
        specHolder.addSpeculator(this.speculator);
    }

    @Test
    void buyAndSell() {
        assertTrue(this.speculator.buy());
        assertTrue(this.speculator.sell());
    }

    @Test
    void buyAndSellInactiveMode() {
        this.speculator.setStatus(TradeStatus.INACTIVE);
        this.speculator.changeParameters(0, 0);

        assertTrue(this.speculator.buy());
        assertTrue(this.speculator.sell());
    }

    @Test
    void changeParameters() {
        Number[] expected = new Number[]{1, 2};
        this.speculator.changeParameters(expected);

        assertEquals(expected, this.speculator.getTheOracle().getProphesyParameters());
    }
}
