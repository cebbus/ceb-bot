package com.cebbus.chart.panel;

import com.cebbus.binance.Speculator;
import com.cebbus.util.PropertyReader;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.num.Num;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class PanelMenu {

    private final Speculator speculator;

    public PanelMenu(Speculator speculator) {
        this.speculator = speculator;
    }

    public JMenuBar create() {
        JMenuBar menuBar = new JMenuBar();

        if (PropertyReader.isCredentialsExist()
                && this.speculator.getSymbol().getWeight() > 0) {
            menuBar.add(createOrderMenu());
            menuBar.add(createStatusMenu());
        }

        menuBar.add(createStrategyMenu());
        return menuBar;
    }

    private JMenu createOrderMenu() {
        JMenu order = new JMenu("Order");
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

    private JMenu createStrategyMenu() {
        JMenu status = new JMenu("Strategy");
        JMenuItem compare = new JMenuItem("Compare");
        compare.addActionListener(e -> {
            List<Pair<String, Num>> resultList = this.speculator.calcStrategies();
            JTable resultTable = createResultTable(resultList);
            JScrollPane scrollPane = new JScrollPane(resultTable);

            JDialog dialog = createDialog();
            dialog.setTitle("Comparison of Strategies");
            dialog.add(scrollPane);
            dialog.setVisible(true);
        });

        status.add(compare);

        return status;
    }

    private JDialog createDialog() {
        Frame rootFrame = JOptionPane.getRootFrame();

        JDialog dialog = new JDialog(rootFrame, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(rootFrame);

        return dialog;
    }

    private JTable createResultTable(List<Pair<String, Num>> resultList) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Strategy");
        model.addColumn("Result");

        resultList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        resultList.forEach(result -> model.addRow(new Object[]{result.getKey(), formatResult(result.getValue())}));

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        return table;
    }

    private String formatResult(Num result) {
        DecimalFormat format = new DecimalFormat("#,###.0000");
        return format.format(result.doubleValue());
    }
}
