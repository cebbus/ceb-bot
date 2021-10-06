package com.cebbus;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.binance.Speculator;
import com.cebbus.chart.CryptoChartPanel;
import com.cebbus.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;

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
            if (enter(strategy, tradingRecord, newBar, endIndex)) {
                speculator.enter();

                Trade entry = tradingRecord.getLastEntry();
                log.info("Entered on " + entry.getIndex() + prepareTradeLog(entry));
            } else if (exit(strategy, tradingRecord, newBar, endIndex)) {
                speculator.exit();

                Trade exit = tradingRecord.getLastExit();
                log.info("Exited on " + exit.getIndex() + prepareTradeLog(exit));
            }

            chartPanel.refresh();
        });
    }

    private static boolean enter(Strategy strategy, TradingRecord tradingRecord, Bar newBar, int endIndex) {
        if (strategy.shouldEnter(endIndex)) {
            //TODO check account
            return tradingRecord.enter(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
        }

        return false;
    }

    private static boolean exit(Strategy strategy, TradingRecord tradingRecord, Bar newBar, int endIndex) {
        if (strategy.shouldExit(endIndex)) {
            //TODO check account
            return tradingRecord.exit(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
        }

        return false;
    }

    private static String prepareTradeLog(Trade entry) {
        return " (price: " + entry.getNetPrice().doubleValue() + ", amount: " + entry.getAmount().doubleValue() + ")";
    }

}
