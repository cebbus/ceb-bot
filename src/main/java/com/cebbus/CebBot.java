package com.cebbus;

import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.chart.CryptoChartPanel;
import com.cebbus.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

@Slf4j
public class CebBot {

    public static void main(String[] args) {
        Speculator speculator = new Speculator();
        speculator.loadHistory();

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(PropertyReader.getSymbol())
                .withBars(speculator.convertToBarList())
                .withMaxBarCount(PropertyReader.getCacheSize())
                .build();

        TheOracle theOracle = new TheOracle(series);
        theOracle.chooseBest();

        CryptoChartPanel chartPanel = new CryptoChartPanel(theOracle);
        chartPanel.show();

        speculator.startSpec(theOracle, chartPanel);
    }

}
