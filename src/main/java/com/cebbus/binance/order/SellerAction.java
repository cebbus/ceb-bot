package com.cebbus.binance.order;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.SymbolFilter;
import com.cebbus.binance.Speculator;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class SellerAction extends TraderAction {

    public SellerAction(Speculator speculator) {
        super(speculator);
    }

    public Trade exit() {
        if (this.speculator.isActive()) {
            NewOrderResponse orderResponse = sell();
            return createTradeRecord(orderResponse);
        } else {
            return createBacktestRecord();
        }
    }

    public boolean exitable(boolean isManual) {
        boolean isSpecActive = this.speculator.isActive();
        if (isSpecActive && noBalance(this.symbol.getBase(), true)) {
            log.info("{} - you have no coin!", this.symbol.getName());
            return false;
        }

        return this.theOracle.shouldExit(isSpecActive, isManual);
    }

    private NewOrderResponse sell() {
        SymbolFilter symbolFilter = getLotSizeFilter();
        BigDecimal stepSize = new BigDecimal(symbolFilter.getStepSize());
        int scale = Math.max(0, stepSize.stripTrailingZeros().scale());

        AssetBalance balance = getBalance(this.symbol.getBase());
        BigDecimal quantity = new BigDecimal(balance.getFree()).setScale(scale, RoundingMode.DOWN);

        NewOrder sellOrder = NewOrder.marketSell(this.symbol.getName(), quantity.toPlainString());

        return this.restClient.newOrder(sellOrder);
    }

}
