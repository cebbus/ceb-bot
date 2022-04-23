package com.cebbus;

import com.cebbus.analysis.Symbol;
import com.cebbus.binance.Speculator;
import com.cebbus.util.PropertyReader;
import com.cebbus.util.SpeculatorHolder;
import com.cebbus.view.panel.CryptoAppFrame;
import com.cebbus.view.panel.CryptoSplashFrame;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;

@Slf4j
public class CebBot {

    private static final List<Symbol> SYMBOLS = PropertyReader.getSymbols();

    private static CryptoAppFrame appFrame;
    private static CryptoSplashFrame splashFrame;

    public static void main(String[] args) {
        SpeculatorHolder specHolder = SpeculatorHolder.getInstance();

        if (!GraphicsEnvironment.isHeadless()) {
            splashFrame = new CryptoSplashFrame(SYMBOLS.size());
            splashFrame.show();

            appFrame = new CryptoAppFrame();
        }

        for (Symbol symbol : SYMBOLS) {
            Speculator speculator = new Speculator(symbol, true);
            speculator.startSpec();

            if (!GraphicsEnvironment.isHeadless()) {
                appFrame.addTab(speculator);
                splashFrame.progress();
            }

            specHolder.addSpeculator(speculator);
        }

        if (!GraphicsEnvironment.isHeadless()) {
            appFrame.addTestTab();
            appFrame.addWalkForwardTab();

            splashFrame.hide();
            appFrame.show();
        }
    }

}
