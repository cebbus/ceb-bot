package com.cebbus.analysis;

import com.cebbus.util.DateTimeUtil;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.ohlc.OHLCItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class SeriesHelperTest {

    private SeriesHelper helper;

    @BeforeEach
    void setUp() {
        BarSeries series = DataGenerator.generateSeries();
        this.helper = new SeriesHelper(series);
    }

    @Test
    void getName() {
        assertEquals("test series", this.helper.getName());
    }

    @Test
    void addBar() {
        Duration hour = Duration.ofHours(1);
        ZonedDateTime now = ZonedDateTime.now();
        BigDecimal zpo = new BigDecimal("0.1");

        BaseBar newBar = new BaseBar(hour, now.plus(4L, ChronoUnit.HOURS), zpo, zpo, zpo, zpo, zpo);

        this.helper.addBar(newBar);
        assertEquals(4, this.helper.getCandleDataList().size());
    }

    @Test
    void addBarReplace() {
        Duration hour = Duration.ofHours(1);
        ZonedDateTime now = ZonedDateTime.now();
        BigDecimal zpo = new BigDecimal("0.1");
        BaseBar newBar = new BaseBar(hour, now.plus(3L, ChronoUnit.HOURS), zpo, zpo, zpo, zpo, zpo);

        this.helper.addBar(newBar);
        assertEquals(3, this.helper.getCandleDataList().size());
    }

    @Test
    void getLastCandle() {
        RegularTimePeriod period = new Millisecond();
        OHLCItem expected = new OHLCItem(period, 1.1, 1.1, 1.1, 1.1);

        OHLCItem actual;
        try (MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class)) {
            dateTimeUtilMock.when(() -> DateTimeUtil.getBarPeriod(any())).thenReturn(period);

            actual = this.helper.getLastCandle();
        }

        assertEquals(expected, actual);
    }

    @Test
    void getLastSeriesItem() {
        ClosePriceIndicator indicator = new ClosePriceIndicator(DataGenerator.generateSeries());

        RegularTimePeriod period = new Millisecond();
        TimeSeriesDataItem expected = new TimeSeriesDataItem(period, 1.1);

        TimeSeriesDataItem actual;
        try (MockedStatic<DateTimeUtil> dateTimeUtilMock = mockStatic(DateTimeUtil.class)) {
            dateTimeUtilMock.when(() -> DateTimeUtil.getBarPeriod(any())).thenReturn(period);

            actual = this.helper.getLastSeriesItem(indicator);
        }

        assertEquals(expected, actual);
    }
}