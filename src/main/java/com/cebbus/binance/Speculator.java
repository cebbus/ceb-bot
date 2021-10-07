package com.cebbus.binance;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.binance.mapper.CandlestickMapper;
import com.cebbus.util.LimitedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Speculator {

    private static final int SCALE = 8;
    private static final String CURRENCY = "USDT";
    private static final Map<Long, Candlestick> CACHE = new LimitedHashMap<>();

    private final String symbol;
    private final CandlestickInterval interval;
    private final BinanceApiRestClient restClient;

    public Speculator(String symbol, CandlestickInterval interval) {
        this.symbol = symbol;
        this.interval = interval;
        this.restClient = ClientFactory.restClient();
    }

    public void loadHistory() {
        List<Candlestick> bars = this.restClient.getCandlestickBars(this.symbol.toUpperCase(), this.interval);
        bars.forEach(candlestick -> CACHE.put(candlestick.getCloseTime(), candlestick));
    }

    public void startStream(BinanceApiCallback<Candlestick> callback) {
        try (BinanceApiWebSocketClient client = ClientFactory.webSocketClient()) {
            client.onCandlestickEvent(this.symbol.toLowerCase(), this.interval, response -> {
                if (Boolean.TRUE.equals(response.getBarFinal())) {
                    Long closeTime = response.getCloseTime();
                    Candlestick candlestick = CandlestickMapper.valueOf(response);

                    CACHE.put(closeTime, candlestick);
                    log.info(String.format("New stick! Symbol: %s", response.getSymbol()));

                    callback.onResponse(candlestick);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public NewOrder enter() {
        AssetBalance balance = getBalance(CURRENCY);
        BigDecimal freeBalance = new BigDecimal(balance.getFree());

        TickerPrice tickerPrice = this.restClient.getPrice(this.symbol);
        BigDecimal price = new BigDecimal(tickerPrice.getPrice());

        BigDecimal quantity = freeBalance.divide(price, SCALE, RoundingMode.HALF_DOWN);
        return NewOrder.marketBuy(this.symbol, quantity.toPlainString());
    }

    public NewOrder exit() {
        AssetBalance balance = getBalance(this.symbol);
        BigDecimal freeBalance = convertToBd(balance.getFree());

        return NewOrder.marketSell(this.symbol, freeBalance.toPlainString());
    }

    public boolean checkAccountToEnter() {
        return hasFreeBalance(CURRENCY);
    }

    public boolean checkAccountToExit() {
        return hasFreeBalance(this.symbol.replace(CURRENCY, ""));
    }

    private boolean hasFreeBalance(String symbol) {
        AssetBalance balance = getBalance(symbol);
        return convertToBd(balance.getFree()).doubleValue() > 0;
    }

    private AssetBalance getBalance(String symbol) {
        Account account = this.restClient.getAccount();
        return account.getAssetBalance(symbol);
    }

    private BigDecimal convertToBd(String value) {
        return new BigDecimal(value).setScale(SCALE, RoundingMode.HALF_DOWN);
    }

    public List<Bar> convertToBarList() {
        return CACHE.values().stream().map(BarMapper::valueOf).collect(Collectors.toList());
    }

}
