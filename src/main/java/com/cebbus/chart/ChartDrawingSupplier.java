package com.cebbus.chart;

import org.jfree.chart.plot.DefaultDrawingSupplier;

import java.awt.*;

import static com.cebbus.chart.ColorPalette.COLORS;

public class ChartDrawingSupplier extends DefaultDrawingSupplier {

    private int paintIndex;
    private int fillPaintIndex;

    @Override
    public Paint getNextPaint() {
        Paint result = COLORS[paintIndex % COLORS.length];
        paintIndex++;
        return result;
    }


    @Override
    public Paint getNextFillPaint() {
        Paint result = COLORS[fillPaintIndex % COLORS.length];
        fillPaintIndex++;
        return result;
    }
}