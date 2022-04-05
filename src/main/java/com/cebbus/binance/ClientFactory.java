package com.cebbus.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.cebbus.util.PropertyReader;

public class ClientFactory {

    private static final BinanceApiRestClient REST_CLIENT;
    private static final BinanceApiClientFactory CLIENT_FACTORY;

    static {
        String apiKey = PropertyReader.getApiKey();
        String apiSecret = PropertyReader.getApiSecret();

        String key = apiKey.equals("") ? null : apiKey;
        String secret = apiSecret.equals("") ? null : apiSecret;
        boolean useTest = PropertyReader.isTestProfile() || PropertyReader.isDevelopmentProfile();

        CLIENT_FACTORY = BinanceApiClientFactory.newInstance(key, secret, useTest, useTest);
        REST_CLIENT = CLIENT_FACTORY.newRestClient();
    }

    private ClientFactory() {
    }

    public static BinanceApiRestClient getRestClient() {
        return REST_CLIENT;
    }

    public static BinanceApiWebSocketClient newWebSocketClient() {
        return CLIENT_FACTORY.newWebSocketClient();
    }

}
