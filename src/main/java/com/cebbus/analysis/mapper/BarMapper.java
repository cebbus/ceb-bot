package com.cebbus.analysis.mapper;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.util.DateTimeUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class BarMapper {

    private BarMapper() {
    }

    public static Bar valueOf(Candlestick candlestick, CandlestickInterval interval) {
        return valueOf(interval,
                Candlestick::getCloseTime,
                Candlestick::getOpen,
                Candlestick::getHigh,
                Candlestick::getLow,
                Candlestick::getClose,
                Candlestick::getVolume,
                candlestick);
    }

    public static Bar valueOf(CandlestickEvent event, CandlestickInterval interval) {
        return valueOf(interval,
                CandlestickEvent::getCloseTime,
                CandlestickEvent::getOpen,
                CandlestickEvent::getHigh,
                CandlestickEvent::getLow,
                CandlestickEvent::getClose,
                CandlestickEvent::getVolume,
                event);
    }

    private static <T> Bar valueOf(
            CandlestickInterval interval,
            Function<T, Long> closeTimeGetter,
            Function<T, String> openGetter,
            Function<T, String> highGetter,
            Function<T, String> lowGetter,
            Function<T, String> closeGetter,
            Function<T, String> volumeGetter,
            T clazz) {
        ZonedDateTime dateTime = DateTimeUtil.millisToZonedTime(closeTimeGetter.apply(clazz));

        BigDecimal open = new BigDecimal(openGetter.apply(clazz));
        BigDecimal high = new BigDecimal(highGetter.apply(clazz));
        BigDecimal low = new BigDecimal(lowGetter.apply(clazz));
        BigDecimal close = new BigDecimal(closeGetter.apply(clazz));
        BigDecimal volume = new BigDecimal(volumeGetter.apply(clazz));

        Duration duration = intervalToDuration(interval);
        return new BaseBar(duration, dateTime, open, high, low, close, volume);
    }

    private static Duration intervalToDuration(CandlestickInterval interval) {
        switch (interval) {
            case ONE_MINUTE:
                return Duration.ofMinutes(1L);
            case THREE_MINUTES:
                return Duration.ofMinutes(3L);
            case FIVE_MINUTES:
                return Duration.ofMinutes(5L);
            case FIFTEEN_MINUTES:
                return Duration.ofMinutes(15L);
            case HALF_HOURLY:
                return Duration.ofMinutes(30L);
            case HOURLY:
                return Duration.ofHours(1L);
            case TWO_HOURLY:
                return Duration.ofHours(2L);
            case FOUR_HOURLY:
                return Duration.ofHours(4L);
            case SIX_HOURLY:
                return Duration.ofHours(6L);
            case EIGHT_HOURLY:
                return Duration.ofHours(8L);
            case TWELVE_HOURLY:
                return Duration.ofHours(12L);
            case DAILY:
                return Duration.ofDays(1L);
            case THREE_DAILY:
                return Duration.ofDays(3L);
            case WEEKLY:
                return Duration.ofDays(7L);
            case MONTHLY:
                return Duration.ofDays(30L);
            default:
                return Duration.ofDays(1L);
        }
    }
}
