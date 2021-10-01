package com.cebbus.chart;

import org.jfree.chart.axis.DateAxis;
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
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;

public class CryptoChart {

    final BarSeries series;

    public CryptoChart(BarSeries series) {
        this.series = series;
    }

    public RegularTimePeriod convertToPeriod(ZonedDateTime dateTime) {
        return new Millisecond(Timestamp.valueOf(dateTime.toLocalDateTime()));
    }

    public void setDateAxisFormat(XYPlot plot) {
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd.MM"));
    }

    public void addSignals(XYPlot plot, TradingRecord tradingRecord) {
        for (Position position : tradingRecord.getPositions()) {
            // Buy signal
            int entryIndex = position.getEntry().getIndex();
            ZonedDateTime entryDate = this.series.getBar(entryIndex).getEndTime();
            double buySignalBarTime = convertToPeriod(entryDate).getFirstMillisecond();

            Marker buyMarker = new ValueMarker(buySignalBarTime);
            buyMarker.setPaint(Color.GREEN);
            buyMarker.setLabel("B");
            plot.addDomainMarker(buyMarker);

            // Sell signal
            int exitIndex = position.getExit().getIndex();
            ZonedDateTime exitDate = this.series.getBar(exitIndex).getEndTime();
            double sellSignalBarTime = convertToPeriod(exitDate).getFirstMillisecond();

            Marker sellMarker = new ValueMarker(sellSignalBarTime);
            sellMarker.setPaint(Color.RED);
            sellMarker.setLabel("S");
            plot.addDomainMarker(sellMarker);
        }
    }

    public int getStartIndex() {
        return Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
    }

}
