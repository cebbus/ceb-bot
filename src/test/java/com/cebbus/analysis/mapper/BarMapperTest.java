package com.cebbus.analysis.mapper;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.binance.CsIntervalAdapter;
import com.cebbus.util.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class BarMapperTest {

    private Bar bar;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private Duration duration;
    private ZonedDateTime dateTime;
    private CandlestickInterval interval;

    @BeforeEach
    void setUp() {
        this.open = new BigDecimal("1");
        this.high = new BigDecimal("100");
        this.low = new BigDecimal("0.5");
        this.close = new BigDecimal("52.5");
        this.volume = new BigDecimal("1000");
        this.dateTime = ZonedDateTime.now();
        this.duration = Duration.ofMinutes(1L);
        this.interval = CandlestickInterval.ONE_MINUTE;
        this.bar = new BaseBar(this.duration, this.dateTime, this.open, this.high, this.low, this.close, this.volume);
    }

    @Test
    void valueOfCandlestick() {
        long closeTime = 1L;

        Candlestick candlestick = new Candlestick();
        candlestick.setOpen(this.open.toPlainString());
        candlestick.setHigh(this.high.toPlainString());
        candlestick.setLow(this.low.toPlainString());
        candlestick.setClose(this.close.toPlainString());
        candlestick.setVolume(this.volume.toPlainString());
        candlestick.setCloseTime(closeTime);

        Bar actual;
        try (
                MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class);
                MockedStatic<CsIntervalAdapter> csIntervalAdapterMock = mockStatic(CsIntervalAdapter.class)
        ) {
            dateTimeUtilMock.when(() -> DateTimeUtil.millisToZonedTime(closeTime)).thenReturn(this.dateTime);
            csIntervalAdapterMock.when(() -> CsIntervalAdapter.getDuration(this.interval)).thenReturn(this.duration);

            actual = BarMapper.valueOf(candlestick, this.interval);
        }

        assertEquals(this.bar, actual);
    }

    @Test
    void valueOfCandlestickEvent() {
        long closeTime = 1L;

        CandlestickEvent event = new CandlestickEvent();
        event.setOpen(this.open.toPlainString());
        event.setHigh(this.high.toPlainString());
        event.setLow(this.low.toPlainString());
        event.setClose(this.close.toPlainString());
        event.setVolume(this.volume.toPlainString());
        event.setCloseTime(closeTime);

        Bar actual;
        try (
                MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class);
                MockedStatic<CsIntervalAdapter> csIntervalAdapterMock = mockStatic(CsIntervalAdapter.class)
        ) {
            dateTimeUtilMock.when(() -> DateTimeUtil.millisToZonedTime(closeTime)).thenReturn(this.dateTime);
            csIntervalAdapterMock.when(() -> CsIntervalAdapter.getDuration(this.interval)).thenReturn(this.duration);

            actual = BarMapper.valueOf(event, this.interval);
        }

        assertEquals(this.bar, actual);
    }
}