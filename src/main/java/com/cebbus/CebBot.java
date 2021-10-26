package com.cebbus;

import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.chart.panel.CryptoAppFrame;
import com.cebbus.chart.panel.CryptoSplashFrame;
import com.cebbus.util.PropertyReader;
import com.cebbus.util.SpeculatorHolder;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.List;

@Slf4j
public class CebBot {

    private static final List<Symbol> SYMBOLS = PropertyReader.getSymbols();

    public static void main(String[] args) {
        SpeculatorHolder specHolder = SpeculatorHolder.getInstance();

        CryptoSplashFrame splashFrame = new CryptoSplashFrame(SYMBOLS.size());
        splashFrame.show();

        CryptoAppFrame appFrame = new CryptoAppFrame();

        for (Symbol symbol : SYMBOLS) {
            Speculator speculator = new Speculator(symbol);
            speculator.loadHistory();

            BarSeries series = new BaseBarSeriesBuilder()
                    .withName(symbol.getName())
                    .withBars(speculator.convertToBarList())
                    .withMaxBarCount(PropertyReader.getCacheSize())
                    .build();

            TheOracle theOracle = new TheOracle(series, symbol.getStrategy());
            speculator.setTheOracle(theOracle);
            speculator.recordOpenPosition();
            speculator.startSpec();

            appFrame.addTab(speculator);
            splashFrame.progress();

            specHolder.addSpeculator(speculator);
        }

        splashFrame.hide();
        appFrame.show();
    }

}
