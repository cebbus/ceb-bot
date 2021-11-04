package com.cebbus.view.panel;

import com.cebbus.binance.Speculator;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.view.chart.ColorPalette;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class StatusPanel {

    private final JPanel panel = new JPanel();
    private final JLabel title = new JLabel();

    public StatusPanel(Speculator speculator) {
        String status = speculator.getStatus().name();

        this.title.setText(StringUtils.capitalize(status.toLowerCase(Locale.ROOT)));
        title.setForeground(ColorPalette.SOFT_WIGHT);

        this.panel.setBackground(getColor(speculator.isActive()));
        this.panel.add(this.title);
    }

    public void changeStatus(TradeStatus status) {
        this.title.setText(StringUtils.capitalize(status.name().toLowerCase(Locale.ROOT)));
        this.panel.setBackground(getColor(status == TradeStatus.ACTIVE));
    }

    public JPanel getPanel() {
        return panel;
    }

    private Color getColor(boolean active) {
        return active ? ColorPalette.DARK_GREEN : ColorPalette.DARK_RED;
    }
}
