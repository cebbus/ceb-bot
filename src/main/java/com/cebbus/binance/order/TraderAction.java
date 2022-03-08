package com.cebbus.binance.order;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.Trade;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class TraderAction {

    static final int SCALE = 8;

    final Speculator speculator;
    final Symbol symbol;
    final TheOracle theOracle;
    final BinanceApiRestClient restClient;
    final AtomicInteger counter = new AtomicInteger(0);

    TraderAction(Speculator speculator) {
        this.speculator = speculator;
        this.symbol = speculator.getSymbol();
        this.theOracle = speculator.getTheOracle();
        this.restClient = speculator.getRestClient();
    }

    boolean noBalance(String s, boolean checkMinQty) {
        AssetBalance balance = getBalance(s);
        BigDecimal free = strToBd(balance.getFree());

        if (!checkMinQty) {
            return free.doubleValue() <= 0;
        } else {
            BigDecimal minQty = new BigDecimal(getLotSizeFilter().getMinQty());
            return free.compareTo(minQty) < 0;
        }
    }

    AssetBalance getBalance(String s) {
        Account account = this.restClient.getAccount();
        return account.getAssetBalance(s);
    }

    SymbolFilter getLotSizeFilter() {
        SymbolInfo symbolInfo = getSymbolInfo();
        return symbolInfo.getSymbolFilter(FilterType.LOT_SIZE);
    }

    SymbolInfo getSymbolInfo() {
        return this.restClient.getExchangeInfo().getSymbolInfo(this.symbol.getName());
    }

    Trade createBacktestRecord() {
        return this.theOracle.newTrade(false, null);
    }

    Trade createTradeRecord(NewOrderResponse response) {
        OrderStatus status = response.getStatus();
        if (status != OrderStatus.FILLED && status != OrderStatus.PARTIALLY_FILLED) {
            return null;
        }

        Order order = findOrder(response.getOrderId());
        Pair<Num, Num> priceAmount = getPriceAmountPair(order);

        return this.theOracle.newTrade(true, priceAmount);
    }

    //order not returns immediately, that's why wait a second before the retry
    private Order findOrder(Long orderId) {
        List<Order> orders = this.restClient.getAllOrders(new AllOrdersRequest(this.symbol.getName()));
        Optional<Order> order = orders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst();

        if (order.isPresent()) {
            counter.set(0);
            return order.get();
        } else if (counter.incrementAndGet() > 5) {
            log.error("Order not found! Order Id: {}}", orderId);
            throw new OrderNotFoundException();
        } else {
            log.warn("Order not found! Order Id: {} Attempt: {}", orderId, counter.get());

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }

            return findOrder(orderId);
        }
    }

    private Pair<Num, Num> getPriceAmountPair(Order order) {
        BigDecimal amount = strToBd(order.getExecutedQty());

        BigDecimal quote = strToBd(order.getCummulativeQuoteQty());
        BigDecimal price = quote.divide(amount, SCALE, RoundingMode.DOWN);

        return Pair.of(DecimalNum.valueOf(price), DecimalNum.valueOf(amount));
    }

    private BigDecimal strToBd(String value) {
        return new BigDecimal(value).setScale(SCALE, RoundingMode.DOWN);
    }
}
