package com.cebbus.chart.panel.test;

import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;

import static com.cebbus.chart.panel.test.CryptoTestTabPanel.RESULT_FORMAT;
import static com.cebbus.chart.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;

public class TestResultDetailTable {

    private final Box panel;
    private final TestTitlePanel title;
    private final DefaultTableModel tableModel;

    public TestResultDetailTable() {
        this.panel = Box.createVerticalBox();
        this.panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 10, 8));

        this.title = new TestTitlePanel("Result Details", false);

        this.tableModel = new DefaultTableModel();

        createTable();
    }

    private void createTable() {
        this.tableModel.addColumn("Metric");
        this.tableModel.addColumn("Value");


        JTable table = new JTable(this.tableModel);
        table.setFillsViewportHeight(true);

        TableColumn valueColumn = table.getColumnModel().getColumn(1);
        valueColumn.setWidth(10);
        valueColumn.setMinWidth(10);
        valueColumn.setPreferredWidth(10);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setMinimumSize(new Dimension(WEST_ITEM_WIDTH, 200));
        scrollPane.setMaximumSize(new Dimension(WEST_ITEM_WIDTH, 200));
        scrollPane.setPreferredSize(new Dimension(WEST_ITEM_WIDTH, 200));

        this.panel.add(scrollPane);
    }

    public void reload(Speculator speculator) {
        this.tableModel.setRowCount(0);

        TheOracle theOracle = speculator.getTheOracle();
        BarSeries series = theOracle.getSeries();
        TradingRecord record = theOracle.getBacktestRecord();

        this.tableModel.addRow(new Object[]{"Number of Pos", record.getPositionCount()});

        NumberOfBarsCriterion numberOfBarsCriterion = new NumberOfBarsCriterion();
        int numOfBars = numberOfBarsCriterion.calculate(series, record).intValue();
        this.tableModel.addRow(new Object[]{"Number of Bars", numOfBars});

        GrossReturnCriterion returnCriterion = new GrossReturnCriterion();
        double totalReturn = returnCriterion.calculate(series, record).doubleValue();
        this.tableModel.addRow(new Object[]{"Strategy Return", RESULT_FORMAT.format(totalReturn)});

        BuyAndHoldReturnCriterion buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
        double buyAndHold = buyAndHoldReturnCriterion.calculate(series, record).doubleValue();
        this.tableModel.addRow(new Object[]{"Buy and Hold Return", RESULT_FORMAT.format(buyAndHold)});

        VersusBuyAndHoldCriterion versusBuyAndHoldCriterion = new VersusBuyAndHoldCriterion(returnCriterion);
        double versus = versusBuyAndHoldCriterion.calculate(series, record).doubleValue();
        this.tableModel.addRow(new Object[]{"Strategy vs Hold (%)", RESULT_FORMAT.format(versus * 100)});

        WinningPositionsRatioCriterion winningRatioCriterion = new WinningPositionsRatioCriterion();
        double winningRatio = winningRatioCriterion.calculate(series, record).doubleValue();
        this.tableModel.addRow(new Object[]{"Strategy Winning Ratio (%)", RESULT_FORMAT.format(winningRatio * 100)});
    }

    public Box getPanel() {
        return panel;
    }

    public TestTitlePanel getTitle() {
        return title;
    }
}
