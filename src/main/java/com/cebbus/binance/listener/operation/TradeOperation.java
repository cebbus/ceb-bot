package com.cebbus.binance.listener.operation;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.order.BuyerAction;
import com.cebbus.binance.order.SellerAction;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;

@Slf4j
public class TradeOperation implements EventOperation {

    private final Speculator speculator;
    private final BuyerAction buyerAction;
    private final SellerAction sellerAction;

    public TradeOperation(TheOracle theOracle, Speculator speculator) {
        BinanceApiRestClient restClient = speculator.getRestClient();

        this.speculator = speculator;
        this.buyerAction = new BuyerAction(theOracle, restClient);
        this.sellerAction = new SellerAction(theOracle, restClient);
    }

    @Override
    public void operate(CandlestickEvent response) {
        if (!this.speculator.isActive()) {
            return;
        }

        Trade trade = null;
        if (this.buyerAction.enterable(true)) {
            log.info("should enter!");
            trade = this.buyerAction.enter();
        } else if (this.sellerAction.exitable(true)) {
            log.info("should exit!");
            trade = this.sellerAction.exit();
        }

        writeTradeLog(trade);
    }

    public boolean manualEnter() {
        if (this.buyerAction.enterable(false)) {
            log.info("manual enter triggered!");

            try {
                Trade trade = this.buyerAction.enter();
                writeTradeLog(trade);
                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    public boolean manualExit() {
        if (this.sellerAction.exitable(false)) {
            log.info("manual exit triggered!");

            try {
                Trade trade = this.sellerAction.exit();
                writeTradeLog(trade);
                return true;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    private void writeTradeLog(Trade trade) {
        if (trade != null) {
            log.info("{} amount: {} price: {}", trade.getType(), trade.getAmount(), trade.getNetPrice());
        }
    }
}
