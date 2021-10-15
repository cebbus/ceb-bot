package com.cebbus.binance.order;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.SymbolFilter;
import com.cebbus.analysis.TheOracle;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class SellerAction extends TraderAction {

    public SellerAction(TheOracle theOracle, BinanceApiRestClient restClient) {
        super(theOracle, restClient);
    }

    public Trade exit() {
        NewOrderResponse orderResponse = sell();
        return createTradeRecord(orderResponse);
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

        if (noBalance(SYMBOL_BASE, true)) {
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
        SymbolFilter symbolFilter = getLotSizeFilter();
        BigDecimal stepSize = new BigDecimal(symbolFilter.getStepSize());
        int scale = Math.max(0, stepSize.stripTrailingZeros().scale());

        AssetBalance balance = getBalance(SYMBOL_BASE);
        BigDecimal quantity = new BigDecimal(balance.getFree()).setScale(scale, RoundingMode.HALF_DOWN);

        NewOrder sellOrder = NewOrder.marketSell(SYMBOL, quantity.toPlainString());

        return this.restClient.newOrder(sellOrder);
    }

}
