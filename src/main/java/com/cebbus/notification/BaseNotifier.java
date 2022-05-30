package com.cebbus.notification;

public class BaseNotifier implements Notifier {

    private final Notifier notifier;

    public BaseNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void send(String message) {
        if (this.notifier != null) {
            this.notifier.send(message);
        }
    }
}
