package com.cebbus.view.chart;

import com.cebbus.util.DateTimeUtil;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.RegularTimePeriod;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import javax.swing.*;
import java.util.List;

public abstract class CryptoChart {

    final BarSeries series;

    CryptoChart(BarSeries series) {
        this.series = series;
    }

    public abstract List<JMenuItem> createMenuList();

    public abstract JFreeChart create();

    public abstract void refresh();

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
        Bar bar = this.series.getBar(index);
        RegularTimePeriod period = DateTimeUtil.getBarPeriod(bar);
        double barTime = period.getFirstMillisecond();

        plot.addDomainMarker(new CryptoMarker(barTime, trade.isBuy(), backtest));
    }

    int getStartIndex() {
        return Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
    }

}
