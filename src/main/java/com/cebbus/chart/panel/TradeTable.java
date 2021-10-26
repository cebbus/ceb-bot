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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TradeTable {

    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;
    private final DefaultTableModel model = new DefaultTableModel();

    private Trade lastTradeBuffer;
    private Trade lastBacktestBuffer;

    public TradeTable(TheOracle theOracle) {
        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.getBacktestRecord();

        this.lastTradeBuffer = this.tradingRecord.getLastTrade();
        this.lastBacktestBuffer = this.backtestRecord.getLastTrade();
    }

    public JTable create() {
        this.model.addColumn("#");
        this.model.addColumn("Date");
        this.model.addColumn("B/S");
        this.model.addColumn("Amount");
        this.model.addColumn("Price");
        this.model.addColumn("Total");

        List<Trade> tradeList = new ArrayList<>();

        List<Position> positionList = this.backtestRecord.getPositions();
        positionList.forEach(p -> tradeList.addAll(positionToTradeList(p)));

        Trade lastEntry = this.tradingRecord.getLastEntry();
        if (lastEntry != null) {
            tradeList.add(lastEntry);
        }

        tradeList.sort(Comparator.comparingInt(Trade::getIndex));
        tradeList.forEach(t -> this.model.addRow(tradeToRow(t)));

        JTable table = new JTable(this.model);
        table.setDefaultRenderer(Object.class, new BuySellRenderer());
        table.setFillsViewportHeight(true);

        return table;
    }

    public void refresh() {
        Trade lastBacktest = this.backtestRecord.getLastTrade();
        if (lastBacktest != null && !lastBacktest.equals(this.lastBacktestBuffer)) {
            this.model.addRow(tradeToRow(lastBacktest));
            this.lastBacktestBuffer = lastBacktest;
        }

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

    private List<Trade> positionToTradeList(Position position) {
        return List.of(position.getEntry(), position.getExit());
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
