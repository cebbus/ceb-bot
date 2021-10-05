package com.cebbus.chart;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.TradingRecord;

import java.awt.*;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

public abstract class CryptoChart {

    final BarSeries series;

    CryptoChart(BarSeries series) {
        this.series = series;
    }

    abstract JFreeChart create();

    abstract void refresh();

    public RegularTimePeriod convertToPeriod(ZonedDateTime dateTime) {
        return new Millisecond(Timestamp.valueOf(dateTime.toLocalDateTime()));
    }

    public void addSignals(XYPlot plot, TradingRecord tradingRecord) {
        tradingRecord.getPositions().forEach(p -> addSignal(plot, p));
    }

    public void addSignal(XYPlot plot, Position position) {
        // Buy signal
        int entryIndex = position.getEntry().getIndex();
        ZonedDateTime entryDate = this.series.getBar(entryIndex).getEndTime();
        double buySignalBarTime = convertToPeriod(entryDate).getFirstMillisecond();

        Marker buyMarker = new ValueMarker(buySignalBarTime);
        buyMarker.setPaint(Color.GREEN);
        buyMarker.setLabel("B - " + entryIndex);
        plot.addDomainMarker(buyMarker);

        // Sell signal
        int exitIndex = position.getExit().getIndex();
        ZonedDateTime exitDate = this.series.getBar(exitIndex).getEndTime();
        double sellSignalBarTime = convertToPeriod(exitDate).getFirstMillisecond();

        Marker sellMarker = new ValueMarker(sellSignalBarTime);
        sellMarker.setPaint(Color.RED);
        sellMarker.setLabel("S - " + entryIndex);
        plot.addDomainMarker(sellMarker);
    }

    public int getStartIndex() {
        return Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
    }

}
