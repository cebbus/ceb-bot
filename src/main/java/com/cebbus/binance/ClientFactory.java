package com.cebbus.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.cebbus.util.PropertyReader;
import org.apache.commons.lang3.StringUtils;

public class ClientFactory {

    private static final BinanceApiRestClient REST_CLIENT;
    private static final BinanceApiClientFactory CLIENT_FACTORY;

    static {
        String apiKey = PropertyReader.getApiKey();
        String apiSecret = PropertyReader.getApiSecret();


        String key = StringUtils.defaultIfBlank(apiKey, null);
        String secret = StringUtils.defaultIfBlank(apiSecret, null);
        boolean useTest = PropertyReader.isTestProfile() || PropertyReader.isDevelopmentProfile();

        CLIENT_FACTORY = BinanceApiClientFactory.newInstance(key, secret, useTest, useTest);
        REST_CLIENT = CLIENT_FACTORY.newRestClient();
    }

    private ClientFactory() {
    }

    public static BinanceApiRestClient getRestClient() {
        return REST_CLIENT;
    }

}
