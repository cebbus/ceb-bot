package com.cebbus.analysis.mapper;

import com.cebbus.dto.CandleDto;
import com.cebbus.dto.CsIntervalAdapter;
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
    private ZonedDateTime dateTime;
    private CsIntervalAdapter interval;

    @BeforeEach
    void setUp() {
        this.open = new BigDecimal("1");
        this.high = new BigDecimal("100");
        this.low = new BigDecimal("0.5");
        this.close = new BigDecimal("52.5");
        this.volume = new BigDecimal("1000");
        this.dateTime = ZonedDateTime.now();
        this.interval = CsIntervalAdapter.ONE_MINUTE;
        this.bar = new BaseBar(Duration.ofMinutes(1L), this.dateTime, this.open, this.high, this.low, this.close, this.volume);
    }

    @Test
    void dtoToBar() {
        long time = this.dateTime.toEpochSecond();

        CandleDto dto = new CandleDto();
        dto.setOpen(this.open);
        dto.setHigh(this.high);
        dto.setLow(this.low);
        dto.setClose(this.close);
        dto.setVolume(this.volume);
        dto.setOpenTime(time);
        dto.setCloseTime(time);

        Bar actual;
        try (MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class)) {
            dateTimeUtilMock.when(() -> DateTimeUtil.millisToZonedTime(time)).thenReturn(this.dateTime);

            actual = BarMapper.dtoToBar(dto, this.interval);
        }

        assertEquals(this.bar, actual);
    }

    @Test
    void valueOfCandlestickEvent() {
        long time = this.dateTime.toEpochSecond();
        ZonedDateTime endTime = this.bar.getEndTime();
        ZonedDateTime beginTime = this.bar.getBeginTime();

        CandleDto expected = new CandleDto();
        expected.setOpen(this.open);
        expected.setHigh(this.high);
        expected.setLow(this.low);
        expected.setClose(this.close);
        expected.setVolume(this.volume);
        expected.setOpenTime(time);
        expected.setCloseTime(time);

        CandleDto actual;
        try (MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class)) {
            dateTimeUtilMock.when(() -> DateTimeUtil.zonedTimeToMillis(endTime)).thenReturn(time);
            dateTimeUtilMock.when(() -> DateTimeUtil.zonedTimeToMillis(beginTime)).thenReturn(time);

            actual = BarMapper.barToDto(this.bar);
        }

        assertEquals(expected, actual);
    }
}