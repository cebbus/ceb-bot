package com.cebbus.notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogNotifier extends BaseNotifier {

    public LogNotifier(Notifier notifier) {
        super(notifier);
    }

    @Override
    public void send(String message) {
        super.send(message);
        log.info(message);
    }
}
