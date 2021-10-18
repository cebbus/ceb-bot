package com.cebbus.analysis.rule;

import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class BackwardUnderIndicatorRule extends AbstractRule {

    private final int numberOfBars;
    private final double minStrength;
    private final UnderIndicatorRule underIndicatorRule;

    public BackwardUnderIndicatorRule(Indicator<Num> first, Indicator<Num> second, int numberOfBars) {
        this(first, second, numberOfBars, 1D);
    }

    public BackwardUnderIndicatorRule(Indicator<Num> first, Indicator<Num> second, int numberOfBars, double minStrength) {
        this.minStrength = minStrength;
        this.numberOfBars = numberOfBars;
        this.underIndicatorRule = new UnderIndicatorRule(first, second);
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        int count = 0;

        int startIndex = Math.max(0, i - this.numberOfBars);

        for (int j = startIndex; j < i; j++) {
            if (this.underIndicatorRule.isSatisfied(j)) {
                count++;
            }
        }

        double ratio = (double) count / (double) this.numberOfBars;
        boolean satisfied = ratio >= this.minStrength;
        this.traceIsSatisfied(i, satisfied);

        return satisfied;
    }
}
