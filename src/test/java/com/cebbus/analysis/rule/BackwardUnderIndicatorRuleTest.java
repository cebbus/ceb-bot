package com.cebbus.analysis.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.FixedIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackwardUnderIndicatorRuleTest {

    private Indicator<Num> first;
    private Indicator<Num> second;

    @BeforeEach
    void setUp() {
        this.first = new FixedIndicator<>(null,
                DoubleNum.valueOf(1),
                DoubleNum.valueOf(2),
                DoubleNum.valueOf(3),
                DoubleNum.valueOf(40));

        this.second = new FixedIndicator<>(null,
                DoubleNum.valueOf(10),
                DoubleNum.valueOf(20),
                DoubleNum.valueOf(30),
                DoubleNum.valueOf(4));
    }

    @Test
    void isSatisfiedTrue() {
        BackwardUnderIndicatorRule rule = new BackwardUnderIndicatorRule(this.first, this.second, 2);
        assertTrue(rule.isSatisfied(2));
    }

    @Test
    void isSatisfiedFalse() {
        BackwardUnderIndicatorRule rule = new BackwardUnderIndicatorRule(this.first, this.second, 2);
        assertFalse(rule.isSatisfied(4));
    }
}