package com.cebbus.util;

import com.binance.api.client.domain.market.CandlestickInterval;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.properties.EncryptableProperties;
import org.jasypt.properties.PropertyValueEncryptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertyReader {

    private static final Properties PROPERTIES;
    private static final StandardPBEStringEncryptor ENCRYPTOR;

    static {
        ENCRYPTOR = new StandardPBEStringEncryptor();
        ENCRYPTOR.setPassword("&RICHIE_RICH&"); //TODO move to env variable
        ENCRYPTOR.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        ENCRYPTOR.setIvGenerator(new RandomIvGenerator());
        ENCRYPTOR.setStringOutputType("HEXADECIMAL");

        PROPERTIES = new EncryptableProperties(ENCRYPTOR);
        ClassLoader classLoader = PropertyReader.class.getClassLoader();
        try {
            InputStream inputStream = classLoader.getResourceAsStream("api.properties");
            PROPERTIES.load(inputStream);

            PROPERTIES.entrySet().stream()
                    .filter(e -> e.getKey().equals("api.key") || e.getKey().equals("api.secret"))
                    .filter(e -> e.getValue() != null && !e.getValue().toString().isBlank())
                    .filter(e -> !PropertyValueEncryptionUtils.isEncryptedValue(e.getValue().toString()))
                    .forEach(e -> {
                        Object key = e.getKey();
                        String value = e.getValue().toString();
                        log.warn("you should encrypt your private for your own safety. you can use this encrypted value for {}: {}",
                                key, PropertyValueEncryptionUtils.encrypt(value, ENCRYPTOR));
                    });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    private PropertyReader() {
    }

    public static String getApiKey() {
        return getProperty("api.key");
    }

    public static String getApiSecret() {
        return getProperty("api.secret");
    }

    public static Integer getCacheSize() {
        return Integer.valueOf(getProperty("cache.size"));
    }

    public static String getSymbol() {
        return getSymbolBase() + getSymbolQuote();
    }

    public static String getSymbolBase() {
        return getProperty("symbol.base");
    }

    public static String getSymbolQuote() {
        return getProperty("symbol.quote");
    }

    public static CandlestickInterval getInterval() {
        return CandlestickInterval.valueOf(getProperty("interval"));
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

}
