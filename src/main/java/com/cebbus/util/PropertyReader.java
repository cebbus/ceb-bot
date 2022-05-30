package com.cebbus.util;

import com.cebbus.CebBot;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.notification.NotifierType;
import com.cebbus.properties.Mail;
import com.cebbus.properties.Radar;
import com.cebbus.properties.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.properties.EncryptableProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.cebbus.util.PropertyEncryptor.checkSecretsEncryption;
import static com.cebbus.util.PropertyEncryptor.getEncryptor;

@Slf4j
public class PropertyReader {

    private static final Properties PROPERTIES;
    private static final List<Mail> MAIL_LIST;
    private static final List<Radar> RADAR_LIST;
    private static final List<Symbol> SYMBOL_LIST;

    static {
        MAIL_LIST = new ArrayList<>();
        RADAR_LIST = new ArrayList<>();
        SYMBOL_LIST = new ArrayList<>();
        PROPERTIES = new EncryptableProperties(getEncryptor());

        try (InputStream is = findPropertyUrl().openStream()) {
            PROPERTIES.load(is);
            checkSecretsEncryption(PROPERTIES.entrySet());

            MAIL_LIST.add(readMail());
            RADAR_LIST.add(readRadar());
            SYMBOL_LIST.addAll(readSymbols());
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
        return getProfile().equals("test");
    }

    public static boolean isDevelopmentProfile() {
        return getProfile().equals("development");
    }

    public static boolean isProductionProfile() {
        return getProfile().equals("production");
    }

    public static List<NotifierType> getNotifierList() {
        return Arrays.stream(getProperty("notifier").split(","))
                .map(s -> NotifierType.valueOf(s.trim()))
                .collect(Collectors.toList());
    }

    private static Mail readMail() {
        boolean auth = Boolean.parseBoolean(getProperty("mail.auth"));
        boolean startTls = Boolean.parseBoolean(getProperty("mail.starttls"));
        Integer port = Integer.valueOf(getProperty("mail.port"));
        String host = getProperty("mail.host");
        String username = getProperty("mail.username");
        String password = getProperty("mail.password");
        String to = getProperty("mail.to");

        return new Mail(auth, startTls, port, host, username, password, to);
    }

    private static Radar readRadar() {
        boolean active = Boolean.parseBoolean(getProperty("radar.active"));
        String quote = getProperty("radar.quote");
        CsIntervalAdapter interval = CsIntervalAdapter.valueOf(getProperty("radar.interval"));

        return new Radar(active, quote, interval);
    }

    private static List<Symbol> readSymbols() {
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
            CsIntervalAdapter interval = CsIntervalAdapter.valueOf(intervalArr[i].trim());
            TradeStatus status = weight <= 0 ? TradeStatus.INACTIVE : TradeStatus.valueOf(statusArr[i].trim());

            symbols.add(new Symbol(i, weight, base, quote, strategy, interval, status));
        }

        return symbols;
    }

    private static String getProperty(String key) {
        return PROPERTIES.getProperty(key).trim();
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

    private static String getProfile() {
        return getProperty("api.profile");
    }

    public static Mail getMail() {
        return MAIL_LIST.get(0);
    }

    public static Radar getRadar() {
        return RADAR_LIST.get(0);
    }

    public static List<Symbol> getSymbolList() {
        return Collections.unmodifiableList(SYMBOL_LIST);
    }
}
