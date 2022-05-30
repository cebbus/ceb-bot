package com.cebbus.properties;

import lombok.Data;

import java.util.Properties;

@Data
public class Mail {
    private final boolean auth;
    private final boolean startTls;
    private final Integer port;
    private final String host;
    private final String username;
    private final String password;
    private final String to;

    public Properties getProperties() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", this.auth);
        prop.put("mail.smtp.starttls.enable", this.startTls);
        prop.put("mail.smtp.host", this.host);
        prop.put("mail.smtp.port", this.port);

        return prop;
    }
}
