package com.cebbus.binance;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.cebbus.binance.mapper.CandlestickMapper;
import com.cebbus.properties.Symbol;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class SpeculatorJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Speculator speculator = (Speculator) dataMap.get("speculator");

        Symbol symbol = speculator.getSymbol();
        Candlestick bar = speculator.getLastCandle();

        CandlestickEvent event = CandlestickMapper.stickToEvent(bar, symbol.getName());
        speculator.triggerListener(event);
    }
}
