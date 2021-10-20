package com.cebbus.binance.order;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.SymbolFilter;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class SellerAction extends TraderAction {

    public SellerAction(TheOracle theOracle, Speculator speculator) {
        super(theOracle, speculator);
    }

    public Trade exit() {
        if (this.speculator.isActive()) {
            NewOrderResponse orderResponse = sell();
            return createTradeRecord(orderResponse);
        } else {
            return createTradeRecord();
        }
    }

    public boolean exitable(boolean askTheOracle) {
        TradingRecord tradingRecord = this.theOracle.getTradingRecord();

        if (askTheOracle) {
            Strategy strategy = this.theOracle.prophesy();
            int endIndex = this.theOracle.getSeries().getEndIndex();
            if (!strategy.shouldExit(endIndex, tradingRecord)) {
                return false;
            }
        }

        if (this.speculator.isActive()) {
            if (noBalance(this.symbol.getBase(), true)) {
                log.info("you have no coin!");
                return false;
            }
        }

        if (!tradingRecord.getCurrentPosition().isOpened()) {
            log.info("you have no position!");
            return false;
        }

        return true;
    }

    private NewOrderResponse sell() {
        SymbolFilter symbolFilter = getLotSizeFilter();
        BigDecimal stepSize = new BigDecimal(symbolFilter.getStepSize());
        int scale = Math.max(0, stepSize.stripTrailingZeros().scale());

        AssetBalance balance = getBalance(this.symbol.getBase());
        BigDecimal quantity = new BigDecimal(balance.getFree()).setScale(scale, RoundingMode.HALF_DOWN);

        NewOrder sellOrder = NewOrder.marketSell(this.symbol.getName(), quantity.toPlainString());

        return this.restClient.newOrder(sellOrder);
    }

}
