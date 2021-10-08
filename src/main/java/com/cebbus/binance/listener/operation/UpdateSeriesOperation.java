package com.cebbus.binance.listener.operation;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.cebbus.analysis.mapper.BarMapper;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Slf4j
public class UpdateSeriesOperation implements EventOperation {

    private final BarSeries series;

    public UpdateSeriesOperation(BarSeries series) {
        this.series = series;
    }

    @Override
    public void operate(CandlestickEvent response) {
        Bar newBar = BarMapper.valueOf(response);
        Bar lastBar = this.series.getLastBar();

        boolean replace = newBar.getEndTime().equals(lastBar.getEndTime());
        this.series.addBar(newBar, replace);
    }
}
