package com.cebbus.view.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.view.chart.ColorPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class TradeTable {

    private final TheOracle theOracle;
    private final DefaultTableModel tradeModel = new DefaultTableModel();
    private final DefaultTableModel backtestModel = new DefaultTableModel();

    public TradeTable(TheOracle theOracle) {
        this.theOracle = theOracle;
    }

    public JTabbedPane create() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("Trade", createTable(this.tradeModel, false));
        tabbedPane.addTab("Backtest", createTable(this.backtestModel, true));

        return tabbedPane;
    }

    public void refresh() {
        addTradeRow(this.backtestModel, true);
        addTradeRow(this.tradeModel, false);
    }

    private JScrollPane createTable(DefaultTableModel model, boolean backtest) {
        model.addColumn("#");
        model.addColumn("Date");
        model.addColumn("B/S");
        model.addColumn("Amount");
        model.addColumn("Price");
        model.addColumn("Total");

        List<Object[]> rowList = this.theOracle.getTradeRowList(backtest);
        rowList.forEach(model::addRow);

        JTable table = new JTable(model);
        table.setDefaultRenderer(Object.class, new BuySellRenderer());
        table.setFillsViewportHeight(true);

        return new JScrollPane(table);
    }

    private void addTradeRow(DefaultTableModel model, boolean backtest) {
        Optional<Object[]> tradeRow = this.theOracle.getLastTradeRow(backtest);
        tradeRow.ifPresent(row -> {
            if (!exists(model, row)) {
                model.addRow(row);
            }
        });
    }

    private boolean exists(DefaultTableModel model, Object[] row) {
        int rowIndex = model.getRowCount() - 1;
        return model.getValueAt(rowIndex, 1).equals(row[1]);
    }

    private static class BuySellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if ("B".equals(value)) {
                c.setForeground(ColorPalette.GREEN);
            } else if ("S".equals(value)) {
                c.setForeground(ColorPalette.RED);
            } else {
                c.setForeground(ColorPalette.DARK_GRAY);
            }

            c.setBackground(row % 2 == 0 ? ColorPalette.LIGHT_GRAY : ColorPalette.SOFT_WIGHT);

            return c;
        }
    }

}
