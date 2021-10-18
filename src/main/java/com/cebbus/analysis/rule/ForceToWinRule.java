package com.cebbus.analysis.rule;

import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.AbstractRule;

public class ForceToWinRule extends AbstractRule {

    private final Double minProfit;
    private final ClosePriceIndicator closePrice;

    public ForceToWinRule(ClosePriceIndicator closePrice, Double minProfit) {
        this.minProfit = minProfit;
        this.closePrice = closePrice;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (tradingRecord == null || tradingRecord.getLastEntry() == null) {
            return false;
        }

        double entryValue = tradingRecord.getLastEntry().getNetPrice().doubleValue();
        double exitValue = this.closePrice.getValue(i).doubleValue();

        if (exitValue < entryValue) {
            return false;
        }

        double profit = (exitValue / entryValue) - 1;
        boolean satisfied = profit > minProfit;
        this.traceIsSatisfied(i, satisfied);

        return satisfied;
    }
}
