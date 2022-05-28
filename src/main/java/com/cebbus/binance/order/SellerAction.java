package com.cebbus.binance.order;

import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.binance.Speculator;
import com.cebbus.dto.TradeDto;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class SellerAction extends TraderAction {

    public SellerAction(Speculator speculator) {
        super(speculator);
    }

    public TradeDto exit() {
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
            BigDecimal balance = getFreeBalance(base);

            List<SymbolFilter> filterList = getLotSizeFilterList();
            for (SymbolFilter filter : filterList) {
                BigDecimal minQuantity = new BigDecimal(filter.getMinQty());

                if (balance.compareTo(minQuantity) < 0) {
                    log.info("{} - you have no coin! free balance: {}, min quantity: {}", base, balance, minQuantity);
                    return false;
                }
            }
        }

        return this.theOracle.shouldExit(isSpecActive, isManual);
    }

    private NewOrderResponse sell() {
        String base = this.symbol.getBase();
        String name = this.symbol.getName();

        List<SymbolFilter> filterList = getLotSizeFilterList();
        int scale = getScale(filterList);
        BigDecimal maxQuantity = getMaxQuantity(filterList);

        BigDecimal balance = getFreeBalance(base, scale);
        BigDecimal quantity = balance.min(maxQuantity);

        NewOrder sellOrder = NewOrder.marketSell(name, quantity.toPlainString());
        return this.restClient.newOrder(sellOrder);
    }

    private List<SymbolFilter> getLotSizeFilterList() {
        SymbolInfo symbolInfo = getSymbolInfo();
        SymbolFilter lotSize = symbolInfo.getSymbolFilter(FilterType.LOT_SIZE);
        SymbolFilter marketLotSize = symbolInfo.getSymbolFilter(FilterType.MARKET_LOT_SIZE);

        return List.of(lotSize, marketLotSize);
    }

    private Integer getScale(List<SymbolFilter> filterList) {
        return filterList.stream()
                .map(f -> new BigDecimal(f.getStepSize()))
                .filter(s -> s.doubleValue() > 0)
                .map(s -> s.stripTrailingZeros().scale())
                .min(Integer::compareTo)
                .orElse(2);
    }

    private BigDecimal getMaxQuantity(List<SymbolFilter> filterList) {
        return filterList.stream()
                .map(f -> new BigDecimal(f.getMaxQty()))
                .min(BigDecimal::compareTo)
                .orElseThrow();
    }

}
