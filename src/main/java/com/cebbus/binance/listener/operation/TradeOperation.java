package com.cebbus.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.analysis.Symbol;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.order.BuyerAction;
import com.cebbus.binance.order.SellerAction;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;

@Slf4j
public class TradeOperation implements EventOperation {

    private final Symbol symbol;
    private final BuyerAction buyerAction;
    private final SellerAction sellerAction;

    public TradeOperation(Speculator speculator) {
        this.symbol = speculator.getSymbol();
        this.buyerAction = new BuyerAction(speculator);
        this.sellerAction = new SellerAction(speculator);
    }

    @Override
    public void operate(CandlestickEvent response) {
        Trade trade = null;
        if (this.buyerAction.enterable(false)) {
            log.info(prepareLog("should enter!"));
            trade = this.buyerAction.enter();
        } else if (this.sellerAction.exitable(false)) {
            log.info(prepareLog("should exit!"));
            trade = this.sellerAction.exit();
        }

        writeTradeLog(trade);
    }

    public boolean manualEnter() {
        if (this.buyerAction.enterable(true)) {
            log.info(prepareLog("manual enter triggered!"));

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
        if (this.sellerAction.exitable(true)) {
            log.info(prepareLog("manual exit triggered!"));

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
            log.info("{} - {} amount: {} price: {}", this.symbol.getName(), trade.getType(), trade.getAmount(), trade.getNetPrice());
        }
    }

    private String prepareLog(String msg) {
        return this.symbol.getName() + " - " + msg;
    }
}
