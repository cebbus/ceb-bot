package com.cebbus.analysis.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForceToWinRuleTest {

    private TradingRecord rec;
    private ForceToWinRule rule;

    @BeforeEach
    void setUp() {
        Duration hour = Duration.ofHours(1);
        ZonedDateTime now = ZonedDateTime.now();

        BigDecimal one = new BigDecimal("1");
        BigDecimal two = new BigDecimal("1.1");

        BarSeries series = new BaseBarSeries();
        series.addBar(new BaseBar(hour, now.plus(1L, ChronoUnit.HOURS), one, one, one, one, one));
        series.addBar(new BaseBar(hour, now.plus(2L, ChronoUnit.HOURS), two, two, two, two, two));
        series.addBar(new BaseBar(hour, now.plus(3L, ChronoUnit.HOURS), one, one, one, one, one));
        series.addBar(new BaseBar(hour, now.plus(4L, ChronoUnit.HOURS), two, two, two, two, two));

        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
        this.rule = new ForceToWinRule(indicator, 0.1);

        this.rec = new BaseTradingRecord();
        this.rec.enter(0, DecimalNum.valueOf(one), DecimalNum.valueOf(one));
    }

    @Test
    void isSatisfiedTrue() {
        assertTrue(this.rule.isSatisfied(1, this.rec));
    }

    @Test
    void isSatisfiedFalse() {
        assertFalse(this.rule.isSatisfied(2, this.rec));
    }
}