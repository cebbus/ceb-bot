package com.cebbus.chart.panel;

import com.cebbus.analysis.Symbol;
import com.cebbus.binance.Speculator;
import com.cebbus.binance.order.TradeStatus;
import com.cebbus.chart.ColorPalette;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class StatusPanel {

    private final JPanel panel = new JPanel();

    public StatusPanel(Speculator speculator) {
        Symbol symbol = speculator.getSymbol();
        String strategy = symbol.getStrategy().replace("Strategy", "");
        String interval = snakeCaseToCapitalWord(symbol.getInterval().name());

        JLabel title = new JLabel(String.format("%s - %s", strategy, interval));
        title.setForeground(ColorPalette.SOFT_WIGHT);

        this.panel.setBackground(getColor(speculator.isActive()));
        this.panel.add(title);
    }

    public void changeStatus(TradeStatus status) {
        this.panel.setBackground(getColor(status == TradeStatus.ACTIVE));
    }

    public JPanel getPanel() {
        return panel;
    }

    private Color getColor(boolean active) {
        return active ? ColorPalette.DARK_GREEN : ColorPalette.DARK_RED;
    }

    private String snakeCaseToCapitalWord(String value) {
        return Arrays.stream(value.split("_"))
                .map(w -> StringUtils.capitalize(w.toLowerCase(Locale.ROOT)))
                .collect(Collectors.joining(" "));
    }
}
