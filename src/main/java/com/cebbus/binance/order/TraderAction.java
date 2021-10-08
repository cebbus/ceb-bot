package com.cebbus.binance.order;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.cebbus.analysis.TheOracle;
import com.cebbus.util.PropertyReader;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class TraderAction {

    static final int SCALE = 8;
    static final String SYMBOL = PropertyReader.getSymbol();
    static final String SYMBOL_BASE = PropertyReader.getSymbolBase();
    static final String SYMBOL_QUOTE = PropertyReader.getSymbolQuote();

    final TheOracle theOracle;
    final BinanceApiRestClient restClient;

    public TraderAction(TheOracle theOracle, BinanceApiRestClient restClient) {
        this.theOracle = theOracle;
        this.restClient = restClient;
    }

    boolean noBalance(String s) {
        AssetBalance balance = getBalance(s);
        return !(new BigDecimal(balance.getFree()).setScale(SCALE, RoundingMode.HALF_DOWN).doubleValue() > 0);
    }

    AssetBalance getBalance(String s) {
        Account account = this.restClient.getAccount();
        return account.getAssetBalance(s);
    }

}
