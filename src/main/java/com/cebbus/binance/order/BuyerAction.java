package com.cebbus.binance.order;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

@Slf4j
public class BuyerAction extends TraderAction {

    public BuyerAction(TheOracle theOracle, Speculator speculator) {
        super(theOracle, speculator);
    }

    public Trade enter() {
        if (this.speculator.isActive()) {
            NewOrderResponse orderResponse = buy();
            return createTradeRecord(orderResponse);
        } else {
            return createTradeRecord();
        }

    }

    public boolean enterable(boolean askTheOracle) {
        TradingRecord tradingRecord = this.theOracle.getTradingRecord();

        if (askTheOracle) {
            Strategy strategy = this.theOracle.prophesy();
            int endIndex = this.theOracle.getSeries().getEndIndex();
            if (!strategy.shouldEnter(endIndex, tradingRecord)) {
                return false;
            }
        }

        if (this.speculator.isActive()) {
            if (noBalance(this.symbol.getQuote(), false)) {
                log.info("you have no balance!");
                return false;
            }
        }

        if (!tradingRecord.getCurrentPosition().isNew()) {
            log.info("you are already in a position!");
            return false;
        }

        return true;
    }

    private NewOrderResponse buy() {
        AssetBalance balance = getBalance(this.symbol.getQuote());

        NewOrder buyOrder = NewOrder
                .marketBuy(this.symbol.getName(), null)
                .quoteOrderQty(balance.getFree());

        return this.restClient.newOrder(buyOrder);
    }

}
