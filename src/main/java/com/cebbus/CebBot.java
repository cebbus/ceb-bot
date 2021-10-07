package com.cebbus;

import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.binance.Speculator;
import com.cebbus.chart.CryptoChartPanel;
import com.cebbus.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;

@Slf4j
public class CebBot {

    private static final String SYMBOL = PropertyReader.getSymbol();
    private static final CandlestickInterval INTERVAL = PropertyReader.getInterval();

    public static void main(String[] args) {
        Speculator speculator = new Speculator(SYMBOL, INTERVAL);
        speculator.loadHistory();

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(SYMBOL.toUpperCase())
                .withBars(speculator.convertToBarList())
                .withMaxBarCount(PropertyReader.getCacheSize())
                .build();

        TheOracle theOracle = new TheOracle(series);
        theOracle.chooseBest();

        CryptoChartPanel chartPanel = new CryptoChartPanel(theOracle);
        chartPanel.show();

        startStream(speculator, theOracle, chartPanel);
    }

    private static void startStream(Speculator speculator, TheOracle theOracle, CryptoChartPanel chartPanel) {
        BarSeries series = theOracle.getSeries();
        Strategy strategy = theOracle.prophesy();
        TradingRecord tradingRecord = theOracle.getTradingRecord();

        speculator.startStream(response -> {
            Bar newBar = BarMapper.valueOf(response);
            Bar lastBar = series.getLastBar();

            boolean replace = newBar.getEndTime().equals(lastBar.getEndTime());
            series.addBar(newBar, replace);

            int endIndex = series.getEndIndex();
            if (enterable(speculator, strategy, tradingRecord, endIndex)) {
                log.info("should enter!");
                NewOrderResponse entry = speculator.enter();
                log.info("entered! price: " + entry.getPrice() + " quantity: " + entry.getExecutedQty());
            } else if (exitable(speculator, strategy, tradingRecord, endIndex)) {
                log.info("should exit!");
                NewOrderResponse exit = speculator.exit();
                log.info("exited! price: " + exit.getPrice() + " quantity: " + exit.getExecutedQty());
            }

            chartPanel.refresh();
        });
    }

    private static boolean enterable(
            Speculator speculator,
            Strategy strategy,
            TradingRecord tradingRecord,
            int endIndex) {

        if (strategy.shouldEnter(endIndex)) {
            if (!speculator.checkAccountToEnter()) {
                log.info("you have no balance!");
                return false;
            }

            if (!tradingRecord.enter(endIndex)) {
                log.info("you are already in a position!");
                return false;
            }

            return true;
        }

        return false;
    }

    private static boolean exitable(
            Speculator speculator,
            Strategy strategy,
            TradingRecord tradingRecord,
            int endIndex) {

        if (strategy.shouldExit(endIndex)) {
            if (!speculator.checkAccountToExit()) {
                log.info("you have no coin!");
                return false;
            }

            if (!tradingRecord.exit(endIndex)) {
                log.info("you have no position!");
                return false;
            }

            return true;
        }

        return false;
    }

}
