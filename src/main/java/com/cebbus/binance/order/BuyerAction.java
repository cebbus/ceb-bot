package com.cebbus.binance.order;

import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.binance.Speculator;
import com.cebbus.dto.TradeDto;
import com.cebbus.exception.ZeroWeightException;
import com.cebbus.util.SpeculatorHolder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class BuyerAction extends TraderAction {

    public BuyerAction(Speculator speculator) {
        super(speculator);
    }

    public TradeDto enter() {
        if (this.speculator.isActive()) {
            SpeculatorHolder specHolder = SpeculatorHolder.getInstance();

            try {
                specHolder.lock(this.speculator);

                NewOrderResponse orderResponse = buy(specHolder);
                return createTradeRecord(orderResponse);
            } finally {
                specHolder.releaseLock(this.speculator);
            }
        } else {
            return createBacktestRecord();
        }
    }

    public boolean enterable(boolean isManual) {
        boolean isSpecActive = this.speculator.isActive();
        if (isSpecActive) {
            String quote = this.symbol.getQuote();
            BigDecimal freeBalance = getFreeBalance(quote);

            if (freeBalance.doubleValue() <= 0) {
                log.info("{} - you have no balance!", quote);
                return false;
            }
        }

        return this.theOracle.shouldEnter(isSpecActive, isManual);
    }

    private NewOrderResponse buy(SpeculatorHolder specHolder) {
        double weight = specHolder.calculateWeight(this.speculator);
        if (weight == 0) {
            throw new ZeroWeightException("weight must be greater than zero");
        }

        SymbolInfo symbolInfo = getSymbolInfo();

        BigDecimal quantity = getFreeBalance(this.symbol.getQuote())
                .multiply(BigDecimal.valueOf(weight))
                .setScale(symbolInfo.getQuotePrecision(), RoundingMode.DOWN);

        NewOrder buyOrder = NewOrder
                .marketBuy(this.symbol.getName(), null)
                .quoteOrderQty(quantity.toPlainString());

        return this.restClient.newOrder(buyOrder);
    }

}
