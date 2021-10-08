package com.cebbus.binance.listener.operation;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.order.BuyerAction;
import com.cebbus.binance.order.SellerAction;
import lombok.extern.slf4j.Slf4j;

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
        if (this.buyerAction.enterable()) {
            log.info("should enter!");
            NewOrderResponse entry = this.buyerAction.enter();
            log.info("entered! price: " + entry.getPrice() + " quantity: " + entry.getExecutedQty());
        } else if (this.sellerAction.exitable()) {
            log.info("should exit!");
            NewOrderResponse exit = this.sellerAction.exit();
            log.info("exited! price: " + exit.getPrice() + " quantity: " + exit.getExecutedQty());
        }
    }
}
