package com.cebbus.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.mapper.CandlestickMapper;
import com.cebbus.dto.CandleDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateSeriesOperation implements EventOperation {

    private final TheOracle theOracle;

    public UpdateSeriesOperation(Speculator speculator) {
        this.theOracle = speculator.getTheOracle();
    }

    @Override
    public void operate(CandlestickEvent response) {
        CandleDto newBar = CandlestickMapper.eventToDto(response);
        this.theOracle.addBar(newBar);
    }
}
