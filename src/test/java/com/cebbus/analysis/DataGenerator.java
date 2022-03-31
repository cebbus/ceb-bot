package com.cebbus.analysis;

import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DataGenerator {
    private DataGenerator() {
    }

    public static BarSeries generateSeries() {
        Duration hour = Duration.ofHours(1);
        ZonedDateTime now = ZonedDateTime.now();

        BigDecimal zpo = new BigDecimal("0.1");
        BigDecimal one = new BigDecimal("1");
        BigDecimal opo = new BigDecimal("1.1");

        BarSeries series = new BaseBarSeries("test series");
        series.addBar(new BaseBar(hour, now.plus(1L, ChronoUnit.HOURS), zpo, zpo, zpo, zpo, zpo));
        series.addBar(new BaseBar(hour, now.plus(2L, ChronoUnit.HOURS), one, one, one, one, one));
        series.addBar(new BaseBar(hour, now.plus(3L, ChronoUnit.HOURS), opo, opo, opo, opo, opo));

        return series;
    }

    public static TradingRecord generateRecord() {
        TradingRecord tradingRecord = new BaseTradingRecord();
        tradingRecord.enter(0, DecimalNum.valueOf("1"), DecimalNum.valueOf("1"));

        return tradingRecord;
    }
}
