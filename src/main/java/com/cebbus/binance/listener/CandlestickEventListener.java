package com.cebbus.binance.listener;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.binance.listener.operation.EventOperation;
import com.cebbus.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CandlestickEventListener implements BinanceApiCallback<CandlestickEvent> {

    private final AtomicInteger count = new AtomicInteger(0);
    private final List<EventOperation> operationList = new CopyOnWriteArrayList<>();

    @Override
    public void onResponse(CandlestickEvent response) {

        if (!Boolean.TRUE.equals(response.getBarFinal())) {
            if (count.incrementAndGet() >= 1000) {
                log.info("stick response! symbol: {}", response.getSymbol());
                count.set(0);
            }

            return;
        }

        log.info("stick closed! symbol: {}", response.getSymbol());

        try {
            for (EventOperation operation : this.operationList) {
                operation.operate(response);
            }
        } catch (OrderNotFoundException e) {
            System.exit(-1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        log.error(cause.getMessage(), cause);
    }

    public int addOperation(EventOperation operation) {
        this.operationList.add(operation);
        return this.operationList.size() - 1;
    }

    public void removeOperation(int index) {
        this.operationList.remove(index);
    }

}
