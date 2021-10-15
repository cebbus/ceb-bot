package com.cebbus.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

public abstract class CryptoChart {

    final BarSeries series;

    CryptoChart(BarSeries series) {
        this.series = series;
    }

    public abstract JFreeChart create();

    public abstract void refresh();

    RegularTimePeriod convertToPeriod(ZonedDateTime dateTime) {
        return new Millisecond(Timestamp.valueOf(dateTime.toLocalDateTime()));
    }

    void addSignals(XYPlot plot, TradingRecord tradingRecord) {
        tradingRecord.getPositions().forEach(p -> {
            addSignal(plot, p.getEntry());
            addSignal(plot, p.getExit());
        });

        Trade lastTrade = tradingRecord.getLastTrade();
        if (lastTrade != null && lastTrade.isBuy()) {
            addSignal(plot, lastTrade);
        }
    }

    void addSignal(XYPlot plot, Trade trade) {
        int index = trade.getIndex();
        ZonedDateTime dateTime = this.series.getBar(index).getEndTime();
        double barTime = convertToPeriod(dateTime).getFirstMillisecond();

        Marker marker = new ValueMarker(barTime);
        marker.setPaint(trade.isBuy() ? ColorPalette.GREEN : ColorPalette.RED);
        marker.setLabel((trade.isBuy() ? "B - " : "S - ") + index);
        plot.addDomainMarker(marker);
    }

    int getStartIndex() {
        return Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
    }

}
