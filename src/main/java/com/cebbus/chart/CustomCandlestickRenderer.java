package com.cebbus.chart;

import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;

public class CustomCandlestickRenderer extends CandlestickRenderer {

    @Override
    public Paint getItemPaint(int row, int column) {
        XYDataset dataset = getPlot().getDataset();
        OHLCDataset highLowData = (OHLCDataset) dataset;
        Number yOpen = highLowData.getOpen(row, column);
        Number yClose = highLowData.getClose(row, column);
        boolean isUpCandle = yClose.doubleValue() > yOpen.doubleValue();

        return isUpCandle ? getUpPaint() : getDownPaint();
    }
}
