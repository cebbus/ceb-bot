package com.cebbus.binance.order;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.cebbus.analysis.TheOracle;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

@Slf4j
public class SellerAction extends TraderAction {

    public SellerAction(TheOracle theOracle, BinanceApiRestClient restClient) {
        super(theOracle, restClient);
    }

    public NewOrderResponse exit() {
        NewOrderResponse orderResponse = sell();
        tradeRecordOperate();

        return orderResponse;
    }

    public boolean exitable() {
        Strategy strategy = this.theOracle.prophesy();
        TradingRecord tradingRecord = this.theOracle.getTradingRecord();
        int endIndex = this.theOracle.getSeries().getEndIndex();

        if (!strategy.shouldExit(endIndex, tradingRecord)) {
            return false;
        }

        if (noBalance(SYMBOL_BASE)) {
            log.info("you have no coin!");
            return false;
        }

        if (!tradingRecord.getCurrentPosition().isOpened()) {
            log.info("you have no position!");
            return false;
        }

        return true;
    }

    private NewOrderResponse sell() {
        AssetBalance balance = getBalance(SYMBOL_BASE);
        NewOrder sellOrder = NewOrder.marketSell(SYMBOL, balance.getFree());

        return this.restClient.newOrder(sellOrder);
    }

}
