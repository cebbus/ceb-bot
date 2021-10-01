package com.cebbus;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.binance.DataLoader;
import com.cebbus.chart.CryptoChartPanel;
import com.cebbus.util.PropertyReader;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CebBot {

    private static final String SYMBOL = PropertyReader.getSymbol();
    private static final CandlestickInterval INTERVAL = PropertyReader.getInterval();

    public static void main(String[] args) throws InterruptedException {
        DataLoader loader = new DataLoader(SYMBOL, INTERVAL);
        loader.loadHistory();

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(SYMBOL.toUpperCase())
                .withBars(loader.convertToBarList())
                .withMaxBarCount(100)
                .build();

        TheOracle theOracle = new TheOracle(series);

        CryptoChartPanel chartPanel = new CryptoChartPanel(theOracle);
        chartPanel.show();

        //startStream(loader, theOracle);
        startDummyStream(loader, theOracle);
    }

    private static void startDummyStream(DataLoader loader, TheOracle theOracle) throws InterruptedException {
        BarSeries series = theOracle.getSeries();
        Strategy strategy = theOracle.prophesy();
        TradingRecord tradingRecord = theOracle.getTradingRecord();
        CachedIndicator<Num> indicator = theOracle.getIndicators().get("CPI").get("CPI");

        List<Bar> barList = createDummyBarList(theOracle);
        for (Bar bar : barList) {
            Thread.sleep(3000L);

            series.addBar(bar);

            int endIndex = series.getEndIndex();
            log.info("end index: " + endIndex);
            boolean exited;
            Trade exit;
            if (strategy.shouldEnter(endIndex)) {
                log.info("Strategy should ENTER on " + endIndex);
                exited = tradingRecord.enter(endIndex, bar.getClosePrice(), DecimalNum.valueOf(10));
                if (exited) {
                    exit = tradingRecord.getLastEntry();
                    log.info("Entered on " + exit.getIndex() + " (price=" + exit.getNetPrice().doubleValue() + ", amount=" + exit.getAmount().doubleValue() + ")");
                }
            } else if (strategy.shouldExit(endIndex)) {
                log.info("Strategy should EXIT on " + endIndex);
                exited = tradingRecord.exit(endIndex, bar.getClosePrice(), DecimalNum.valueOf(10));
                if (exited) {
                    exit = tradingRecord.getLastExit();
                    log.info("Exited on " + exit.getIndex() + " (price=" + exit.getNetPrice().doubleValue() + ", amount=" + exit.getAmount().doubleValue() + ")");
                }
            }
        }

        log.info("CPI Size: " + indicator.getBarSeries().getBarCount());
    }

    private static List<Bar> createDummyBarList(TheOracle theOracle) {
        BarSeries series = theOracle.getSeries();
        Num lastClosePrice = series.getBar(series.getEndIndex()).getClosePrice();

        List<Bar> barList = new ArrayList<>();
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(1L), new BigDecimal(lastClosePrice.toString()), new BigDecimal("500"), new BigDecimal("300"), new BigDecimal("360"), BigDecimal.TEN)); //256 no action
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(2L), new BigDecimal("360"), new BigDecimal("500"), new BigDecimal("300"), new BigDecimal("340"), BigDecimal.TEN)); //257 al
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(3L), new BigDecimal("340"), new BigDecimal("400"), new BigDecimal("200"), new BigDecimal("320"), BigDecimal.TEN)); //258 aldin zaten
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(4L), new BigDecimal("320"), new BigDecimal("450"), new BigDecimal("400"), new BigDecimal("410"), BigDecimal.TEN)); //259 sat
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(5L), new BigDecimal("410"), new BigDecimal("500"), new BigDecimal("200"), new BigDecimal("415"), BigDecimal.TEN)); //260 sattin zaten
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(6L), new BigDecimal("415"), new BigDecimal("600"), new BigDecimal("420"), new BigDecimal("480"), BigDecimal.TEN)); //261 sattin zaten
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(7L), new BigDecimal("480"), new BigDecimal("500"), new BigDecimal("240"), new BigDecimal("290"), BigDecimal.TEN)); //262 al
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(8L), new BigDecimal("290"), new BigDecimal("320"), new BigDecimal("100"), new BigDecimal("310"), BigDecimal.TEN)); //263 aldin zaten
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(9L), new BigDecimal("310"), new BigDecimal("700"), new BigDecimal("100"), new BigDecimal("360"), BigDecimal.TEN)); //264 no action
        barList.add(new BaseBar(Duration.ofDays(1L), ZonedDateTime.now().plusDays(10L), new BigDecimal("360"), new BigDecimal("500"), new BigDecimal("300"), new BigDecimal("340"), BigDecimal.TEN)); //265 aldin zaten

        return barList;
    }

    private static void startStream(DataLoader loader, TheOracle theOracle) {
        BarSeries series = theOracle.getSeries();
        Strategy strategy = theOracle.prophesy();
        TradingRecord tradingRecord = theOracle.getTradingRecord();

        loader.startStream(response -> {
            Bar newBar = BarMapper.valueOf(response);

            series.addBar(newBar);

            int endIndex = series.getEndIndex();
            if (strategy.shouldEnter(endIndex)) {
                boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
                if (entered) {
                    Trade entry = tradingRecord.getLastEntry();
                    log.info("Entered on " + entry.getIndex() + prepareTradeLog(entry));
                }
            } else if (strategy.shouldExit(endIndex)) {
                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), DecimalNum.valueOf(10));
                if (exited) {
                    Trade exit = tradingRecord.getLastExit();
                    log.info("Exited on " + exit.getIndex() + prepareTradeLog(exit));
                }
            }
        });
    }

    private static String prepareTradeLog(Trade entry) {
        return " (price: " + entry.getNetPrice().doubleValue() + ", amount: " + entry.getAmount().doubleValue() + ")";
    }

}
