package com.cebbus.binance;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.Symbol;
import com.cebbus.binance.mapper.CandlestickMapper;
import com.cebbus.dto.CsIntervalAdapter;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class SpeculatorJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Speculator speculator = (Speculator) dataMap.get("speculator");

        Symbol symbol = speculator.getSymbol();
        String name = symbol.getName();
        CsIntervalAdapter intervalAdapter = symbol.getInterval();
        CandlestickInterval interval = CandlestickInterval.valueOf(intervalAdapter.name());

        List<Candlestick> bars = speculator.getRestClient().getCandlestickBars(name, interval, 2, null, null);
        CandlestickEvent event = CandlestickMapper.stickToEvent(bars.get(0), name);
        speculator.triggerListener(event);
    }
}
