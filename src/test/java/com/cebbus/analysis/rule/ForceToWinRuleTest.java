package com.cebbus.analysis.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ForceToWinRuleTest {

    private ForceToWinRule rule;

    @BeforeEach
    void setUp() {
        Duration hour = Duration.ofHours(1);
        ZonedDateTime now = ZonedDateTime.now();

        BarSeries series = new BaseBarSeries();
        series.addBar(new BaseBar(hour, now.plus(1L, ChronoUnit.HOURS), 1d, 1d, 1d, 1d, 1d));
        series.addBar(new BaseBar(hour, now.plus(2L, ChronoUnit.HOURS), 2d, 2d, 2d, 2d, 2d));
        series.addBar(new BaseBar(hour, now.plus(3L, ChronoUnit.HOURS), 1d, 1d, 1d, 1d, 1d));
        series.addBar(new BaseBar(hour, now.plus(4L, ChronoUnit.HOURS), 2d, 2d, 2d, 2d, 2d));

        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
        this.rule = new ForceToWinRule(indicator, 0.1);
    }

    @Test
    void isSatisfiedTrue() {
        TradingRecord record = new BaseTradingRecord();
        record.enter(0, DoubleNum.valueOf(1), DoubleNum.valueOf(1));

        assertTrue(this.rule.isSatisfied(2));
    }

    @Test
    void isSatisfiedFalse() {
        assertFalse(this.rule.isSatisfied(4));
    }
}