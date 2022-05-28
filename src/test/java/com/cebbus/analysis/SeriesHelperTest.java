package com.cebbus.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeriesHelperTest {

    private BarSeries series;
    private SeriesHelper helper;

    @BeforeEach
    void setUp() {
        this.series = DataGenerator.generateSeries();
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
        ZonedDateTime endTime = now.plus(4L, ChronoUnit.HOURS);
        BigDecimal zpo = BigDecimal.valueOf(0.1);

        BaseBar newBar = new BaseBar(hour, endTime, zpo, zpo, zpo, zpo, zpo);

        this.helper.addBar(newBar);
        assertEquals(4, this.helper.getCandleDataList().size());
    }

    @Test
    void addBarReplace() {
        BigDecimal zpo = new BigDecimal("0.1");

        Bar lastBar = this.series.getLastBar();
        BaseBar newBar = new BaseBar(lastBar.getTimePeriod(), lastBar.getEndTime(), zpo, zpo, zpo, zpo, zpo);

        this.helper.addBar(newBar);
        assertEquals(3, this.helper.getCandleDataList().size());
    }

    @Test
    void getLastCandle() {
        Bar expected = this.series.getLastBar();
        Bar actual = this.helper.getLastBar();

        assertEquals(expected, actual);
    }
}