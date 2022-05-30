package com.cebbus;

import com.cebbus.binance.Speculator;
import com.cebbus.properties.Radar;
import com.cebbus.properties.Symbol;
import com.cebbus.util.PropertyReader;
import com.cebbus.util.SpeculatorHolder;
import com.cebbus.util.TaskScheduler;
import com.cebbus.view.panel.CryptoAppFrame;
import com.cebbus.view.panel.CryptoSplashFrame;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;

@Slf4j
public class CebBot {

    private static CryptoAppFrame appFrame;
    private static CryptoSplashFrame splashFrame;

    public static void main(String[] args) {
        Radar radar = PropertyReader.getRadar();
        List<Symbol> symbols = PropertyReader.getSymbolList();
        TaskScheduler scheduler = TaskScheduler.getInstance();
        SpeculatorHolder speHolder = SpeculatorHolder.getInstance();

        if (!GraphicsEnvironment.isHeadless()) {
            splashFrame = new CryptoSplashFrame(symbols.size());
            splashFrame.show();

            appFrame = new CryptoAppFrame();
        }

        for (Symbol symbol : symbols) {
            Speculator speculator = new Speculator(symbol, true);

            if (!GraphicsEnvironment.isHeadless()) {
                appFrame.addTab(speculator);
                splashFrame.progress();
            }

            speHolder.addSpeculator(speculator);
            scheduler.scheduleSpeculator(speculator);
        }

        if (!GraphicsEnvironment.isHeadless()) {
            appFrame.addTestTab();
            appFrame.addWalkForwardTab();

            splashFrame.hide();
            appFrame.show();
        }

        scheduler.scheduleRadar(radar);
    }

}
