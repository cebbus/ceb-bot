package com.cebbus.view.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.view.chart.ColorPalette;
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
    private final DefaultTableModel tradeModel = new DefaultTableModel();
    private final DefaultTableModel backtestModel = new DefaultTableModel();

    private Trade lastTradeBuffer;
    private Trade lastBacktestBuffer;

    public TradeTable(TheOracle theOracle) {
        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.getBacktestRecord();

        this.lastTradeBuffer = this.tradingRecord.getLastTrade();
        this.lastBacktestBuffer = this.backtestRecord.getLastTrade();
    }

    public JTabbedPane create() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("Trade", createTable(this.tradeModel, this.tradingRecord));
        tabbedPane.addTab("Backtest", createTable(this.backtestModel, this.backtestRecord));

        return tabbedPane;
    }

    public void refresh() {
        Trade lastBacktest = this.backtestRecord.getLastTrade();
        if (lastBacktest != null && !lastBacktest.equals(this.lastBacktestBuffer)) {
            this.backtestModel.addRow(tradeToRow(lastBacktest));
            this.lastBacktestBuffer = lastBacktest;
        }

        Trade lastTrade = this.tradingRecord.getLastTrade();
        if (lastTrade != null && !lastTrade.equals(this.lastTradeBuffer)) {
            this.tradeModel.addRow(tradeToRow(lastTrade));
            this.lastTradeBuffer = lastTrade;
        }
    }

    private JScrollPane createTable(DefaultTableModel model, TradingRecord record) {
        model.addColumn("#");
        model.addColumn("Date");
        model.addColumn("B/S");
        model.addColumn("Amount");
        model.addColumn("Price");
        model.addColumn("Total");

        List<Trade> tradeList = new ArrayList<>();

        List<Position> positionList = record.getPositions();
        positionList.forEach(p -> tradeList.addAll(positionToTradeList(p)));

        Trade lastTrade = record.getLastTrade();
        Position lastPosition = record.getLastPosition();
        if (lastTrade != null) {
            if (tradeList.isEmpty()) {
                tradeList.add(lastTrade);
            } else if (lastPosition != null && !lastPosition.getExit().equals(lastTrade)){
                tradeList.add(lastTrade);
            }
        }

        tradeList.sort(Comparator.comparingInt(Trade::getIndex));
        tradeList.forEach(t -> model.addRow(tradeToRow(t)));

        JTable table = new JTable(model);
        table.setDefaultRenderer(Object.class, new BuySellRenderer());
        table.setFillsViewportHeight(true);

        return new JScrollPane(table);
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
