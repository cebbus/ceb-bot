package com.cebbus.util;

import com.cebbus.analysis.Symbol;
import com.cebbus.binance.Speculator;
import com.cebbus.exception.SpeculatorBlockedException;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SpeculatorHolder {

    private static final SpeculatorHolder INSTANCE = new SpeculatorHolder();
    private static final List<Speculator> SPECULATORS = new ArrayList<>();

    private Speculator lockedBy;

    private SpeculatorHolder() {
    }

    public static SpeculatorHolder getInstance() {
        return INSTANCE;
    }

    public synchronized void lock(Speculator speculator) {
        waitForOtherSpec();
        this.lockedBy = speculator;

        Symbol holder = this.lockedBy.getSymbol();
        log.info("system locked by {} - {}", holder.getId(), holder.getName());
    }

    public synchronized double calculateWeight(Speculator speculator) {
        if (isLockedByOther(speculator)) {
            Symbol holder = this.lockedBy.getSymbol();
            log.warn("spec blocked by other spec! holder: {} - {}", holder.getId(), holder.getName());
            throw new SpeculatorBlockedException();
        }

        Symbol symbol = speculator.getSymbol();
        String quote = symbol.getQuote();
        double weight = symbol.getWeight();
        double totalWeight = SPECULATORS.stream()
                .filter(s -> !isInPosition(s))
                .filter(s -> s.getSymbol().getQuote().equals(quote))
                .mapToDouble(s -> s.getSymbol().getWeight())
                .sum();

        return totalWeight <= 0 ? 0 : weight / totalWeight;
    }

    public synchronized void releaseLock(Speculator speculator) {
        if (!isLocked() || isLockedByOther(speculator)) {
            return;
        }

        this.lockedBy = null;

        Symbol holder = speculator.getSymbol();
        log.info("lock released by {} - {}", holder.getId(), holder.getName());
    }

    public void addSpeculator(Speculator speculator) {
        SPECULATORS.add(speculator);
    }

    private void waitForOtherSpec() {
        int count = 0;
        boolean available = false;

        do {
            if (isLocked()) {
                Symbol holder = this.lockedBy.getSymbol();

                log.warn("spec blocked by other spec! attempt: {}, holder: {} - {}",
                        count, holder.getId(), holder.getName());

                if (count++ >= 5) {
                    throw new SpeculatorBlockedException();
                }

                try {
                    Thread.sleep(1000L);
                    waitForOtherSpec();
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            } else {
                available = true;
            }
        } while (!available);
    }

    private boolean isLockedByOther(Speculator speculator) {
        if (!isLocked()) {
            return false;
        }

        Symbol holder = this.lockedBy.getSymbol();
        return holder.getId() != speculator.getSymbol().getId();
    }

    private boolean isLocked() {
        return this.lockedBy != null;
    }

    private boolean isInPosition(Speculator speculator) {
        TradingRecord tradingRecord = speculator.getTheOracle().getTradingRecord();
        Trade lastTrade = tradingRecord.getLastTrade();
        return lastTrade != null && lastTrade.isBuy();
    }
}
