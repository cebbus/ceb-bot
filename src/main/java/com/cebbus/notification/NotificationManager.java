package com.cebbus.notification;

import com.cebbus.util.PropertyReader;
import com.cebbus.util.ReflectionUtil;

import java.util.List;

public class NotificationManager {

    private static final NotificationManager INSTANCE = new NotificationManager();

    private Notifier notifier;

    private NotificationManager() {
        List<NotifierType> notifierList = PropertyReader.getNotifierList();
        notifierList.forEach(n -> this.notifier = ReflectionUtil.initNotifier(this.notifier, n.getClazz()));
    }

    public void send(String message) {
        if (this.notifier != null) {
            this.notifier.send(message);
        }
    }

    public static NotificationManager getInstance() {
        return INSTANCE;
    }
}
