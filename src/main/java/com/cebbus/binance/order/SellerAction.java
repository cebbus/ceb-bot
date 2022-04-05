package com.cebbus.binance.order;

import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.binance.Speculator;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Trade;

import java.math.BigDecimal;

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
        if (isSpecActive) {
            String base = this.symbol.getBase();
            BigDecimal freeBalance = getFreeBalance(base);

            SymbolFilter symbolFilter = getMarketLotSizeFilter();
            BigDecimal minQuantity = new BigDecimal(symbolFilter.getMinQty());

            if (freeBalance.compareTo(minQuantity) < 0) {
                log.info("{} - you have no coin!", base);
                return false;
            }
        }

        return this.theOracle.shouldExit(isSpecActive, isManual);
    }

    private NewOrderResponse sell() {
        SymbolFilter symbolFilter = getMarketLotSizeFilter();
        BigDecimal stepSize = new BigDecimal(symbolFilter.getStepSize());
        int scale = Math.max(0, stepSize.stripTrailingZeros().scale());

        BigDecimal balance = getFreeBalance(this.symbol.getBase(), scale);
        BigDecimal maxQuantity = new BigDecimal(symbolFilter.getMaxQty());
        BigDecimal quantity = balance.min(maxQuantity);

        NewOrder sellOrder = NewOrder.marketSell(this.symbol.getName(), quantity.toPlainString());

        return this.restClient.newOrder(sellOrder);
    }

    private SymbolFilter getMarketLotSizeFilter() {
        SymbolInfo symbolInfo = getSymbolInfo();
        return symbolInfo.getSymbolFilter(FilterType.MARKET_LOT_SIZE);
    }

}
