package com.cebbus.analysis.rule;

import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

public class LastEntryOverIndicatorRule extends AbstractRule {

    private final Indicator<Num> ref;

    public LastEntryOverIndicatorRule(Indicator<Num> ref) {
        this.ref = ref;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (tradingRecord == null || tradingRecord.getLastEntry() == null) {
            return false;
        }

        Num entryValue = tradingRecord.getLastEntry().getNetPrice();
        Num refValue = this.ref.getValue(i);

        boolean satisfied = entryValue.isGreaterThanOrEqual(refValue);
        this.traceIsSatisfied(i, satisfied);

        return satisfied;
    }
}
