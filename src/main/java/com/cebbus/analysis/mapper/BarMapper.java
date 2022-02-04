package com.cebbus.analysis.mapper;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.binance.CsIntervalAdapter;
import com.cebbus.util.DateTimeUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.function.ToLongFunction;

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
            ToLongFunction<T> closeTimeGetter,
            Function<T, String> openGetter,
            Function<T, String> highGetter,
            Function<T, String> lowGetter,
            Function<T, String> closeGetter,
            Function<T, String> volumeGetter,
            T clazz) {
        ZonedDateTime dateTime = DateTimeUtil.millisToZonedTime(closeTimeGetter.applyAsLong(clazz));

        BigDecimal open = new BigDecimal(openGetter.apply(clazz));
        BigDecimal high = new BigDecimal(highGetter.apply(clazz));
        BigDecimal low = new BigDecimal(lowGetter.apply(clazz));
        BigDecimal close = new BigDecimal(closeGetter.apply(clazz));
        BigDecimal volume = new BigDecimal(volumeGetter.apply(clazz));

        Duration duration = CsIntervalAdapter.getDuration(interval);
        return new BaseBar(duration, dateTime, open, high, low, close, volume);
    }
}
