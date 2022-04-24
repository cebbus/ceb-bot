package com.cebbus;

import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.util.PropertyReader;
import com.cebbus.util.SpeculatorHolder;
import com.cebbus.view.panel.CryptoAppFrame;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CebBotIT {

    private Speculator speculator;

    @BeforeEach
    void setUp() {
        CryptoAppFrame appFrame = new CryptoAppFrame();

        Symbol symbol = PropertyReader.getSymbols().get(0);
        SpeculatorHolder specHolder = SpeculatorHolder.getInstance();
        this.speculator = new Speculator(symbol, true);

        appFrame.addTab(this.speculator);
        specHolder.addSpeculator(this.speculator);
    }

    @Test
    @Order(1)
    void buyAndSell() {
        assertTrue(this.speculator.buy());
        assertTrue(this.speculator.sell());
    }

    @Test
    void buyAndSellInactiveMode() {
        this.speculator.setStatus(TradeStatus.INACTIVE);
        this.speculator.changeParameters(1000, 1000);

        assertTrue(this.speculator.buy());
        assertTrue(this.speculator.sell());
    }

    @Test
    void changeParameters() {
        Number[] expected = new Number[]{1, 2};
        this.speculator.changeParameters(expected);

        assertEquals(expected, this.speculator.getTheOracle().getProphesyParameters());
    }

    @Test
    void changeStrategy() {
        TheOracle oldOracle = this.speculator.getTheOracle();

        this.speculator.changeStrategy("AdxStrategy");
        TheOracle newOracle = this.speculator.getTheOracle();

        assertNotEquals(oldOracle, newOracle);
    }

    @Test
    void calcStrategies() {
        List<Pair<String, String>> calcResultList = this.speculator.calcStrategies();
        assertFalse(calcResultList.isEmpty());
    }

    @Test
    @Order(2)
    void startSpec() {
        assertDoesNotThrow(() -> {
            Date fireDate = this.speculator.startSpec();
            Date now = new Date();

            while (now.before(fireDate)) {
                Thread.sleep(10000);
                now = new Date();
            }
        });
    }
}
