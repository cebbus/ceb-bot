package com.cebbus.view.panel.forward;

import com.cebbus.analysis.AnalysisCriterionCalculator;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.FormFieldSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import static com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel.RESULT_FORMAT;
import static com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel.WEST_ITEM_WIDTH;

public class WalkForwardResultDetailTable extends FormFieldSet {

    private final Box panel;
    private final JTable table;

    public WalkForwardResultDetailTable() {
        this.panel = Box.createVerticalBox();
        this.table = createTable();
    }

    private JTable createTable() {
        Box resultDetailLabel = createTitleLabelBox("Result Details", WEST_ITEM_WIDTH, 20);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Metric");
        tableModel.addColumn("Value");

        JTable jTable = new JTable(tableModel);
        jTable.setFillsViewportHeight(true);

        TableColumnModel columnModel = jTable.getColumnModel();
        setColumnSize(columnModel.getColumn(1), 75);

        JScrollPane scrollPane = new JScrollPane(jTable);
        setSize(scrollPane, WEST_ITEM_WIDTH, 150);

        this.panel.add(resultDetailLabel);
        this.panel.add(scrollPane);

        return jTable;
    }

    public void reload(Speculator speculator) {
        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setRowCount(0);

        TheOracle theOracle = speculator.getTheOracle();
        AnalysisCriterionCalculator calculator = theOracle.getCriterionCalculator();

        model.addRow(new Object[]{"Number of Pos", calculator.backtestPosCount()});
        model.addRow(new Object[]{"Number of Bars", calculator.backtestBarCount()});
        model.addRow(new Object[]{"Strategy Return", RESULT_FORMAT.format(calculator.backtestStrategyReturn().doubleValue())});
        model.addRow(new Object[]{"Buy and Hold Return", RESULT_FORMAT.format(calculator.backtestBuyAndHold().doubleValue())});
        model.addRow(new Object[]{"Strategy vs Hold (%)", RESULT_FORMAT.format(calculator.backtestVersus().doubleValue() * 100)});
        model.addRow(new Object[]{"Strategy Winning Ratio (%)", RESULT_FORMAT.format(calculator.backtestWinnigRatio().doubleValue() * 100)});
    }

    public Box getPanel() {
        return panel;
    }
}
