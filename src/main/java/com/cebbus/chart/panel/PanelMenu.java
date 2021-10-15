package com.cebbus.chart.panel;

import com.cebbus.binance.Speculator;

import javax.swing.*;

public class PanelMenu {

    private final Speculator speculator;

    public PanelMenu(Speculator speculator) {
        this.speculator = speculator;
    }

    public JMenuBar create() {
        JMenuBar menuBar = new JMenuBar();

        JMenu order = createOrderMenu();
        JMenu status = createStatusMenu();

        menuBar.add(order);
        menuBar.add(status);
        return menuBar;
    }

    private JMenu createOrderMenu() {
        JMenu order = new JMenu("Order");
        JMenuItem buy = new JMenuItem("Buy");
        buy.addActionListener(e -> {
            int input = JOptionPane.showConfirmDialog(null, "Are you sure you want to buy coins?");
            if (input == 0) {
                boolean success = this.speculator.buy();
                if (success) {
                    JOptionPane.showMessageDialog(null, "You are in!");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Something went wrong, check the log file.", null, JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JMenuItem sell = new JMenuItem("Sell");
        sell.addActionListener(e -> {
            int input = JOptionPane.showConfirmDialog(null, "Are you sure you want to sell coins?");
            if (input == 0) {
                boolean success = this.speculator.sell();
                if (success) {
                    JOptionPane.showMessageDialog(null, "You are out!");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Something went wrong, check the log file.", null, JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        order.add(buy);
        order.add(sell);

        return order;
    }

    private JMenu createStatusMenu() {
        JMenu status = new JMenu("Status");
        JMenuItem activate = new JMenuItem("Activate");
        activate.addActionListener(e -> this.speculator.activate());

        JMenuItem deactivate = new JMenuItem("Deactivate");
        deactivate.addActionListener(e -> this.speculator.deactivate());

        status.add(activate);
        status.add(deactivate);

        return status;
    }
}
