package com.cebbus.notification;

public enum NotifierType {
    LOG(LogNotifier.class),
    MAIL(MailNotifier.class),
    TELEGRAM(null);

    private final Class<? extends Notifier> clazz;

    NotifierType(Class<? extends Notifier> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Notifier> getClazz() {
        return clazz;
    }
}
