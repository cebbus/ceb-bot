package com.cebbus.analysis.rule;

import com.cebbus.analysis.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LastEntryOverIndicatorRuleTest {

    private TradingRecord rec;
    private LastEntryOverIndicatorRule rule;

    @BeforeEach
    void setUp() {
        ClosePriceIndicator indicator = new ClosePriceIndicator(DataGenerator.generateSeries());
        this.rule = new LastEntryOverIndicatorRule(indicator);
        this.rec = DataGenerator.generateRecord();
    }

    @Test
    void isSatisfiedTrue() {
        assertTrue(this.rule.isSatisfied(1, this.rec));
    }

    @Test
    void isSatisfiedFalse() {
        assertFalse(this.rule.isSatisfied(2, this.rec));
    }

    @Test
    void isSatisfiedFalseIsLastEntryNull() {
        assertFalse(this.rule.isSatisfied(1, new BaseTradingRecord()));
    }

    @Test
    void isSatisfiedFalseIsRecordNull() {
        assertFalse(this.rule.isSatisfied(2, null));
    }

}