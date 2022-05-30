package com.cebbus.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SymbolLoader {

    public static Set<String> getSymbolList() {
        BinanceApiRestClient client = ClientFactory.getRestClient();
        ExchangeInfo exchangeInfo = client.getExchangeInfo();

        return exchangeInfo.getSymbols().stream()
                .map(SymbolInfo::getBaseAsset)
                .collect(Collectors.toSet());
    }

    public static List<SymbolInfo> getSymbolListByQuoteAsset(String quote) {
        BinanceApiRestClient client = ClientFactory.getRestClient();
        ExchangeInfo exchangeInfo = client.getExchangeInfo();

        return exchangeInfo.getSymbols().stream()
                .filter(s -> s.getQuoteAsset().equalsIgnoreCase(quote))
                .sorted(Comparator.comparing(SymbolInfo::getBaseAsset))
                .collect(Collectors.toList());
    }

}
