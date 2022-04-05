package com.cebbus.binance.order;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.general.SymbolInfo;
import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.exception.OrderNotFilledException;
import com.cebbus.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.Trade;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.binance.api.client.domain.OrderStatus.*;
import static java.math.RoundingMode.DOWN;

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

    BigDecimal getFreeBalance(String s) {
        Account account = this.restClient.getAccount();
        AssetBalance balance = account.getAssetBalance(s);

        return new BigDecimal(balance.getFree());
    }

    BigDecimal getFreeBalance(String s, int scale) {
        return getFreeBalance(s).setScale(scale, DOWN);
    }

    SymbolInfo getSymbolInfo() {
        return this.restClient.getExchangeInfo().getSymbolInfo(this.symbol.getName());
    }

    Trade createBacktestRecord() {
        return this.theOracle.newTrade(false, null);
    }

    Trade createTradeRecord(NewOrderResponse response) {
        Long orderId = response.getOrderId();
        OrderStatus status = response.getStatus();

        if (!checkOrderStatus(orderId, status)) {
            throw new OrderNotFilledException();
        }

        Order order = findOrder(orderId);
        Pair<Num, Num> priceAmount = getPriceAmountPair(order);
        return this.theOracle.newTrade(true, priceAmount);
    }

    //order not returns immediately, that's why wait a second before the retry
    private Order findOrder(Long orderId) {
        List<Order> orders = this.restClient.getAllOrders(new AllOrdersRequest(this.symbol.getName()));
        Optional<Order> order = orders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst();

        if (order.isPresent()) {
            this.counter.set(0);
            return order.get();
        } else if (this.counter.incrementAndGet() > 5) {
            log.error("Order not found! Order Id: {}", orderId);
            throw new OrderNotFoundException();
        } else {
            log.warn("Order not found! Order Id: {}, Attempt: {}", orderId, this.counter.get());
            sleepThread();

            return findOrder(orderId);
        }
    }

    private Pair<Num, Num> getPriceAmountPair(Order order) {
        BigDecimal amount = strToBd(order.getExecutedQty());

        BigDecimal quote = strToBd(order.getCummulativeQuoteQty());
        BigDecimal price = quote.divide(amount, SCALE, DOWN);

        return Pair.of(DecimalNum.valueOf(price), DecimalNum.valueOf(amount));
    }

    private BigDecimal strToBd(String value) {
        return new BigDecimal(value).setScale(SCALE, DOWN);
    }

    private boolean checkOrderStatus(Long orderId, OrderStatus status) {
        List<OrderStatus> invalidStatusList = List.of(CANCELED, PENDING_CANCEL, REJECTED);

        if (invalidStatusList.contains(status)) {
            log.error("Order not filled! Order Id: {}, Status: {}", orderId, status);
            return false;
        }

        for (int i = 0; i < 5; i++) {
            if (status != FILLED) {
                log.warn("Order not filled! Order Id: {}, Status: {}, Attempt: {}", orderId, status, i);
                sleepThread();

                status = getOrderStatus(orderId);
            }
        }

        return true;
    }

    private OrderStatus getOrderStatus(Long orderId) {
        OrderStatusRequest request = new OrderStatusRequest(this.symbol.getName(), orderId);
        Order order = this.restClient.getOrderStatus(request);
        return order != null ? order.getStatus() : PARTIALLY_FILLED;
    }

    private void sleepThread() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
