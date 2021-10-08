package com.cebbus.analysis.mapper;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class BarMapper {

    private BarMapper() {
    }

    public static Bar valueOf(Candlestick candlestick) {
        return valueOf(Candlestick::getCloseTime,
                Candlestick::getOpen,
                Candlestick::getHigh,
                Candlestick::getLow,
                Candlestick::getClose,
                Candlestick::getVolume,
                candlestick);
    }

    public static Bar valueOf(CandlestickEvent event) {
        return valueOf(CandlestickEvent::getCloseTime,
                CandlestickEvent::getOpen,
                CandlestickEvent::getHigh,
                CandlestickEvent::getLow,
                CandlestickEvent::getClose,
                CandlestickEvent::getVolume,
                event);
    }

    private static <T> Bar valueOf(
            Function<T, Long> closeTimeGetter,
            Function<T, String> openGetter,
            Function<T, String> highGetter,
            Function<T, String> lowGetter,
            Function<T, String> closeGetter,
            Function<T, String> volumeGetter,
            T clazz) {
        Instant instant = Instant.ofEpochMilli(closeTimeGetter.apply(clazz));
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("GMT+3"));

        BigDecimal open = new BigDecimal(openGetter.apply(clazz));
        BigDecimal high = new BigDecimal(highGetter.apply(clazz));
        BigDecimal low = new BigDecimal(lowGetter.apply(clazz));
        BigDecimal close = new BigDecimal(closeGetter.apply(clazz));
        BigDecimal volume = new BigDecimal(volumeGetter.apply(clazz));

        return new BaseBar(Duration.ofDays(1L), dateTime, open, high, low, close, volume);
    }
}
