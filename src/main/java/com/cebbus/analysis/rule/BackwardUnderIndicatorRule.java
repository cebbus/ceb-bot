package com.cebbus.analysis.rule;

import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class BackwardUnderIndicatorRule extends AbstractRule {

    private final int numberOfBars;
    private final UnderIndicatorRule underIndicatorRule;

    public BackwardUnderIndicatorRule(Indicator<Num> first, Indicator<Num> second, int numberOfBars) {
        this.numberOfBars = numberOfBars;
        this.underIndicatorRule = new UnderIndicatorRule(first, second);
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        boolean result = true;
        int startIndex = Math.max(0, i - this.numberOfBars);

        for (int j = startIndex; j < i; j++) {
            result &= this.underIndicatorRule.isSatisfied(j);
        }

        return result;
    }
}
