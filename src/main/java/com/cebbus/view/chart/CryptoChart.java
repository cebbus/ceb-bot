package com.cebbus.view.chart;

import com.cebbus.analysis.TheOracle;
import com.cebbus.dto.TradePointDto;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import javax.swing.*;
import java.util.List;

public abstract class CryptoChart {

    final TheOracle theOracle;

    CryptoChart(TheOracle theOracle) {
        this.theOracle = theOracle;
    }

    public abstract List<JMenuItem> createMenuList();

    public abstract JFreeChart create();

    public abstract void refresh();

    void addSignals(XYPlot plot) {
        List<TradePointDto> tradePointList = this.theOracle.getTradePointList();
        tradePointList.forEach(point -> plot.addDomainMarker(createMarker(point)));
    }

    CryptoMarker createMarker(TradePointDto point) {
        return new CryptoMarker((double) point.getTradeTime(), point.isBuy(), point.isBacktest());
    }

}
