package com.cebbus.binance.listener;

import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.binance.listener.operation.EventOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CandlestickEventListener implements BinanceApiCallback<CandlestickEvent> {

    private final List<EventOperation> operationList;

    public CandlestickEventListener(List<EventOperation> operationList) {
        this.operationList = operationList;
    }

    @Override
    public void onResponse(CandlestickEvent response) {

        if (!Boolean.TRUE.equals(response.getBarFinal())) {
            return;
        }

        log.info(String.format("new stick! symbol: %s", response.getSymbol()));

        try {
            for (EventOperation operation : this.operationList) {
                operation.operate(response);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        log.error(cause.getMessage(), cause);
    }

}
