package com.cebbus.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.cebbus.util.PropertyReader;

public class ClientFactory {

    private static final BinanceApiClientFactory CLIENT_FACTORY;

    static {
        String key = PropertyReader.getApiKey();
        String secret = PropertyReader.getApiSecret();
        CLIENT_FACTORY = BinanceApiClientFactory.newInstance((key.equals("") ? null : key), (secret.equals("") ? null : secret));
    }

    private ClientFactory() {
    }

    public static BinanceApiRestClient restClient() {
        return CLIENT_FACTORY.newRestClient();
    }

    public static BinanceApiWebSocketClient webSocketClient() {
        return CLIENT_FACTORY.newWebSocketClient();
    }

}
