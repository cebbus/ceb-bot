package com.cebbus.view.chart;

import org.jfree.chart.plot.DefaultDrawingSupplier;

import java.awt.*;
import java.util.Objects;

import static com.cebbus.view.chart.ColorPalette.COLORS;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChartDrawingSupplier that = (ChartDrawingSupplier) o;
        return paintIndex == that.paintIndex && fillPaintIndex == that.fillPaintIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paintIndex, fillPaintIndex);
    }
}