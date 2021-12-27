package com.cebbus.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.mapper.BarMapper;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Slf4j
public class UpdateSeriesOperation implements EventOperation {

    private final BarSeries series;
    private final CandlestickInterval interval;

    public UpdateSeriesOperation(BarSeries series, CandlestickInterval interval) {
        this.series = series;
        this.interval = interval;
    }

    @Override
    public void operate(CandlestickEvent response) {
        Bar newBar = BarMapper.valueOf(response, this.interval);
        Bar lastBar = this.series.getLastBar();

        boolean replace = newBar.getBeginTime().equals(lastBar.getBeginTime());
        this.series.addBar(newBar, replace);
    }
}
