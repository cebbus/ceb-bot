package com.cebbus.chart.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.chart.ColorPalette;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TradeTable {

    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;
    private final DefaultTableModel model = new DefaultTableModel();

    private Trade lastTradeBuffer;

    public TradeTable(TheOracle theOracle) {
        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.backtest();
    }

    public JTable create() {
        this.model.addColumn("#");
        this.model.addColumn("Date");
        this.model.addColumn("B/S");
        this.model.addColumn("Amount");
        this.model.addColumn("Price");
        this.model.addColumn("Total");

        List<Position> positions = this.backtestRecord.getPositions();
        for (Position position : positions) {
            this.model.addRow(tradeToRow(position.getEntry()));
            this.model.addRow(tradeToRow(position.getExit()));
        }

        JTable table = new JTable(this.model);
        table.setDefaultRenderer(Object.class, new BuySellRenderer());
        table.setFillsViewportHeight(true);

        return table;
    }

    public void refresh() {
        Trade lastTrade = this.tradingRecord.getLastTrade();
        if (lastTrade != null && !lastTrade.equals(this.lastTradeBuffer)) {
            this.model.addRow(tradeToRow(lastTrade));
            this.lastTradeBuffer = lastTrade;
        }
    }

    private Object[] tradeToRow(Trade trade) {
        int index = trade.getIndex();
        ZonedDateTime dateTime = this.series.getBar(index).getEndTime();
        String formattedTime = dateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

        Object[] row = new Object[6];
        row[0] = index;
        row[1] = formattedTime;
        row[2] = trade.isBuy() ? "B" : "S";
        row[3] = trade.getAmount();
        row[4] = trade.getNetPrice();
        row[5] = trade.getAmount().multipliedBy(trade.getNetPrice());

        return row;
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
