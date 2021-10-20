package com.cebbus.util;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.CebBot;
import com.cebbus.analysis.Symbol;
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

    public static List<Symbol> getSymbols() {
        String[] baseArr = getProperty("symbol.base").split(",");
        String[] quoteArr = getProperty("symbol.quote").split(",");
        String[] intervalArr = getProperty("interval").split(",");
        String[] strategyArr = getProperty("strategy").split(",");

        if (baseArr.length != quoteArr.length
                || baseArr.length != intervalArr.length
                || baseArr.length != strategyArr.length) {
            log.error("base, quote and interval must be the same length!");
            System.exit(-1);
        }

        int size = baseArr.length;
        List<Symbol> symbols = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            symbols.add(new Symbol(
                    baseArr[i].trim(),
                    quoteArr[i].trim(),
                    strategyArr[i].trim(),
                    CandlestickInterval.valueOf(intervalArr[i].trim())
            ));
        }

        return symbols;
    }

    public static String getProperty(String key) {
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
