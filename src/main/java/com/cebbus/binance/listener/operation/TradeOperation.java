package com.cebbus.binance.listener.operation;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.order.BuyerAction;
import com.cebbus.binance.order.SellerAction;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;

@Slf4j
public class TradeOperation implements EventOperation {

    private final BuyerAction buyerAction;
    private final SellerAction sellerAction;

    public TradeOperation(TheOracle theOracle, BinanceApiRestClient restClient) {
        this.buyerAction = new BuyerAction(theOracle, restClient);
        this.sellerAction = new SellerAction(theOracle, restClient);
    }

    @Override
    public void operate(CandlestickEvent response) {
        Trade trade = null;
        if (this.buyerAction.enterable()) {
            log.info("should enter!");
            trade = this.buyerAction.enter();
        } else if (this.sellerAction.exitable()) {
            log.info("should exit!");
            trade = this.sellerAction.exit();
        }

        if (trade != null) {
            log.info("{} amount: {} price: {}", trade.getType().name().toLowerCase(),
                    trade.getAmount(), trade.getNetPrice());
        }
    }
}
