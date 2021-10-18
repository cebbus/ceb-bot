package com.cebbus.util;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.properties.PropertyValueEncryptionUtils;

import java.util.Map;
import java.util.Set;

@Slf4j
public class PropertyEncryptor {

    private static final StandardPBEStringEncryptor ENCRYPTOR;

    static {
        ENCRYPTOR = new StandardPBEStringEncryptor();
        ENCRYPTOR.setPassword("&RICHIE_RICH&"); //TODO move to env variable
        ENCRYPTOR.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        ENCRYPTOR.setIvGenerator(new RandomIvGenerator());
        ENCRYPTOR.setStringOutputType("HEXADECIMAL");
    }

    private PropertyEncryptor() {
    }

    public static void checkSecretsEncryption(Set<Map.Entry<Object, Object>> propertySet) {
        propertySet.stream()
                .filter(e -> e.getKey().equals("api.key") || e.getKey().equals("api.secret"))
                .filter(e -> e.getValue() != null && !e.getValue().toString().isBlank())
                .filter(e -> !PropertyValueEncryptionUtils.isEncryptedValue(e.getValue().toString()))
                .forEach(e -> {
                    Object key = e.getKey();
                    String value = e.getValue().toString();
                    log.warn("you should encrypt your private for your own safety. you can use this encrypted value for {}: {}",
                            key, PropertyValueEncryptionUtils.encrypt(value, ENCRYPTOR));
                });
    }

    public static StandardPBEStringEncryptor getEncryptor() {
        return ENCRYPTOR;
    }
}
