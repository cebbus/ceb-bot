package com.cebbus.util;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.CebBot;
import com.cebbus.analysis.Symbol;
import com.cebbus.binance.order.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.properties.EncryptableProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.cebbus.util.PropertyEncryptor.checkSecretsEncryption;
import static com.cebbus.util.PropertyEncryptor.getEncryptor;

@Slf4j
public class PropertyReader {

    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new EncryptableProperties(getEncryptor());

        try (InputStream is = findPropertyUrl().openStream()) {
            PROPERTIES.load(is);
            checkSecretsEncryption(PROPERTIES.entrySet());
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

    public static boolean isCredentialsExist() {
        return !getApiKey().isBlank() && !getApiSecret().isBlank();
    }

    public static boolean isTestProfile() {
        return getProperty("api.profile").equals("test");
    }

    public static boolean isDevelopmentProfile() {
        return getProperty("api.profile").equals("development");
    }

    public static boolean isProductionProfile() {
        return getProperty("api.profile").equals("production");
    }

    public static List<Symbol> getSymbols() {
        String[] baseArr = getProperty("symbol.base").split(",");
        String[] quoteArr = getProperty("symbol.quote").split(",");
        String[] intervalArr = getProperty("symbol.interval").split(",");
        String[] strategyArr = getProperty("symbol.strategy").split(",");
        String[] statusArr = getProperty("symbol.status").split(",");
        String[] weightArr = getProperty("symbol.weight").split(",");

        if (baseArr.length != quoteArr.length
                || baseArr.length != intervalArr.length
                || baseArr.length != strategyArr.length
                || baseArr.length != statusArr.length
                || baseArr.length != weightArr.length) {
            log.error("base, quote, interval, strategy, status and weight must be the same length!");
            System.exit(-1);
        }

        int size = baseArr.length;
        List<Symbol> symbols = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            String base = baseArr[i].trim();
            String quote = quoteArr[i].trim();
            String strategy = strategyArr[i].trim();
            double weight = Double.parseDouble(weightArr[i].trim());
            CandlestickInterval interval = CandlestickInterval.valueOf(intervalArr[i].trim());
            TradeStatus status = weight <= 0 ? TradeStatus.INACTIVE : TradeStatus.valueOf(statusArr[i].trim());

            symbols.add(new Symbol(i, weight, base, quote, strategy, interval, status));
        }

        return symbols;
    }

    private static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static URL findPropertyUrl() throws MalformedURLException {
        String fileName = "api.properties";

        ClassLoader loader = PropertyReader.class.getClassLoader();

        URL jarLoc = CebBot.class.getProtectionDomain().getCodeSource().getLocation();
        File jarDir = new File(jarLoc.getPath()).getParentFile();
        File propFile = new File(jarDir, fileName);

        URL url = propFile.exists() ? propFile.toURI().toURL() : loader.getResource(fileName);
        log.info("Property file loaded from {}", url != null ? url.getPath() : null);

        return url;
    }

}
