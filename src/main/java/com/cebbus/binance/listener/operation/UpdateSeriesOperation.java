package com.cebbus.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.mapper.BarMapper;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

@Slf4j
public class UpdateSeriesOperation implements EventOperation {

    private final TheOracle theOracle;
    private final CandlestickInterval interval;

    public UpdateSeriesOperation(TheOracle theOracle, CandlestickInterval interval) {
        this.theOracle = theOracle;
        this.interval = interval;
    }

    @Override
    public void operate(CandlestickEvent response) {
        Bar newBar = BarMapper.valueOf(response, this.interval);
        this.theOracle.addBar(newBar);
    }
}
