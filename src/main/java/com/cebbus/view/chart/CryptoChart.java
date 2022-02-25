package com.cebbus.view.chart;

import com.cebbus.analysis.TheOracle;
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
        List<Object[]> tradePointList = this.theOracle.getTradePointList();
        tradePointList.forEach(point -> addSignal(plot, point));
    }

    void addSignal(XYPlot plot, Object[] point) {
        CryptoMarker marker = new CryptoMarker((double) point[0], (boolean) point[1], (boolean) point[2]);
        plot.addDomainMarker(marker);
    }

}
