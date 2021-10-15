package com.cebbus.chart.panel;

import com.cebbus.binance.order.TradeStatus;
import com.cebbus.chart.ColorPalette;
import com.cebbus.util.PropertyReader;

import javax.swing.*;

public class StatusPanel {

    private final JPanel panel = new JPanel();

    public StatusPanel() {
        this.panel.setBackground(ColorPalette.DARK_GREEN);

        JLabel title = new JLabel(PropertyReader.getSymbol() + " - " + PropertyReader.getInterval().name());
        title.setForeground(ColorPalette.SOFT_WIGHT);
        this.panel.add(title);
    }

    public void changeStatus(TradeStatus status) {
        this.panel.setBackground(status == TradeStatus.ACTIVE ? ColorPalette.DARK_GREEN : ColorPalette.DARK_RED);
    }

    public JPanel getPanel() {
        return panel;
    }
}
