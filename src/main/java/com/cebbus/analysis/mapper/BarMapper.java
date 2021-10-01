package com.cebbus.analysis.mapper;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class BarMapper {

    private BarMapper() {
    }

    public static Bar valueOf(Candlestick candlestick) {
        Instant instant = Instant.ofEpochMilli(candlestick.getCloseTime());
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("GMT"));

        BigDecimal open = new BigDecimal(candlestick.getOpen());
        BigDecimal high = new BigDecimal(candlestick.getHigh());
        BigDecimal low = new BigDecimal(candlestick.getLow());
        BigDecimal close = new BigDecimal(candlestick.getClose());
        BigDecimal volume = new BigDecimal(candlestick.getVolume());

        return new BaseBar(Duration.ofDays(1L), dateTime, open, high, low, close, volume);
    }
}
