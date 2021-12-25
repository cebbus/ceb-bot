package com.cebbus.view.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import javax.swing.*;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

public abstract class CryptoChart {

    final BarSeries series;

    CryptoChart(BarSeries series) {
        this.series = series;
    }

    public abstract List<JMenuItem> createMenuList();

    public abstract JFreeChart create();

    public abstract void refresh();

    RegularTimePeriod convertToPeriod(ZonedDateTime dateTime) {
        return new Millisecond(Timestamp.valueOf(dateTime.toLocalDateTime()));
    }

    void addSignals(XYPlot plot, TradingRecord tradingRecord, boolean backtest) {
        tradingRecord.getPositions().forEach(p -> {
            addSignal(plot, p.getEntry(), backtest);
            addSignal(plot, p.getExit(), backtest);
        });

        Trade lastTrade = tradingRecord.getLastTrade();
        if (lastTrade != null && lastTrade.isBuy()) {
            addSignal(plot, lastTrade, backtest);
        }
    }

    void addSignal(XYPlot plot, Trade trade, boolean backtest) {
        int index = trade.getIndex();
        ZonedDateTime dateTime = this.series.getBar(index).getEndTime();
        double barTime = convertToPeriod(dateTime).getFirstMillisecond();

        plot.addDomainMarker(new CryptoMarker(barTime, trade.isBuy(), backtest));
    }

    int getStartIndex() {
        return Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
    }

}
