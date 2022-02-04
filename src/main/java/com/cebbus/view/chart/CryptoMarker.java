package com.cebbus.view.chart;

import org.jfree.chart.plot.ValueMarker;

import java.awt.*;
import java.util.Objects;

public class CryptoMarker extends ValueMarker {

    private final boolean backtest;

    public CryptoMarker(double value, boolean buy, boolean backtest) {
        super(value);
        this.backtest = backtest;

        Color color = createColor(buy, backtest);

        this.setLabel(buy ? "B" : "S");
        this.setPaint(color);
        this.setLabelBackgroundColor(color);
    }

    public boolean isBacktest() {
        return backtest;
    }

    private Color createColor(boolean buy, boolean backtest) {
        if (backtest) {
            return buy ? ColorPalette.ORANGE : ColorPalette.PURPLE;
        } else {
            return buy ? ColorPalette.GREEN : ColorPalette.RED;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CryptoMarker that = (CryptoMarker) o;
        return backtest == that.backtest;
    }

    @Override
    public int hashCode() {
        return Objects.hash(backtest);
    }
}
