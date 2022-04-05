package com.cebbus.view.panel.menu;

import com.cebbus.binance.Speculator;

import javax.swing.*;

public class OrderMenu {

    private final Speculator speculator;

    public OrderMenu(Speculator speculator) {
        this.speculator = speculator;
    }

    JMenu create() {
        JMenu order = new JMenu("Order");
        order.add(createBuyItem());
        order.add(createSellItem());

        return order;
    }

    private JMenuItem createBuyItem() {
        JMenuItem buy = new JMenuItem("Buy");
        buy.addActionListener(e -> {
            if (!this.speculator.isActive()) {
                JOptionPane.showMessageDialog(null,
                        "You cannot buy the coin because of the speculator is inactive!",
                        "Invalid Process", JOptionPane.WARNING_MESSAGE);
                return;
            }

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

        return buy;
    }

    private JMenuItem createSellItem() {
        JMenuItem sell = new JMenuItem("Sell");

        sell.addActionListener(e -> {
            if (!this.speculator.isActive()) {
                JOptionPane.showMessageDialog(null,
                        "You cannot sell the coin because of the speculator is inactive!",
                        "Invalid Process", JOptionPane.WARNING_MESSAGE);
                return;
            }

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

        return sell;
    }
}
