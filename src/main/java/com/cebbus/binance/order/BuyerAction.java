package com.cebbus.binance.order;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.cebbus.analysis.TheOracle;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

@Slf4j
public class BuyerAction extends TraderAction {

    public BuyerAction(TheOracle theOracle, BinanceApiRestClient restClient) {
        super(theOracle, restClient);
    }

    public Trade enter() {
        NewOrderResponse orderResponse = buy();
        return createTradeRecord(orderResponse);
    }

    public boolean enterable() {
        Strategy strategy = this.theOracle.prophesy();
        TradingRecord tradingRecord = this.theOracle.getTradingRecord();
        int endIndex = this.theOracle.getSeries().getEndIndex();

        if (!strategy.shouldEnter(endIndex, tradingRecord)) {
            return false;
        }

        if (noBalance(SYMBOL_QUOTE)) {
            log.info("you have no balance!");
            return false;
        }

        if (!tradingRecord.getCurrentPosition().isNew()) {
            log.info("you are already in a position!");
            return false;
        }

        return true;
    }

    private NewOrderResponse buy() {
        AssetBalance balance = getBalance(SYMBOL_QUOTE);

        NewOrder buyOrder = NewOrder.marketBuy(SYMBOL, null).quoteOrderQty(balance.getFree());
        return this.restClient.newOrder(buyOrder);
    }

}
