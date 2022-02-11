package com.cebbus.view.panel.test;

import com.cebbus.analysis.AnalysisCriterionCalculator;
import com.cebbus.analysis.OptimizeTask;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.FormFieldSet;
import com.cebbus.view.panel.WaitDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.cebbus.view.panel.test.CryptoTestTabPanel.RESULT_FORMAT;
import static com.cebbus.view.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;

public class TestResultDetailTable extends FormFieldSet {

    private final Box panel;
    private final JTable table;
    private final JButton optBtn;
    private final List<Consumer<Speculator>> onOptimizeDoneListeners = new ArrayList<>();

    private Speculator speculator;

    public TestResultDetailTable() {
        this.panel = Box.createVerticalBox();

        this.table = createTable();
        this.optBtn = createOptimizeButton();
    }

    private JTable createTable() {
        Box resultDetailLabel = createTitleLabelBox("Result Details", WEST_ITEM_WIDTH, 20);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Metric");
        tableModel.addColumn("Value");

        JTable jTable = new JTable(tableModel);
        jTable.setPreferredScrollableViewportSize(new Dimension(WEST_ITEM_WIDTH, 150));
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable.setFillsViewportHeight(true);

        TableColumnModel columnModel = jTable.getColumnModel();
        rearrangeColumnSize(columnModel);

        JScrollPane scrollPane = new JScrollPane(jTable);
        setSize(scrollPane, WEST_ITEM_WIDTH, 150);

        this.panel.add(resultDetailLabel);
        this.panel.add(scrollPane);

        return jTable;
    }

    private JButton createOptimizeButton() {
        JButton jButton = new JButton("Optimize");
        setSize(jButton, WEST_ITEM_WIDTH, 20);
        jButton.setEnabled(false);

        jButton.addActionListener(e -> {
            OptimizeTask task = new OptimizeTask(this.speculator);

            WaitDialog waitDialog = new WaitDialog(el -> task.cancel());

            task.addOnDoneListener(this.onOptimizeDoneListeners);
            task.addOnDoneListener(s -> waitDialog.hide());

            Thread thread = new Thread(task);
            thread.start();
            waitDialog.show();
        });

        Box showButtonBox = Box.createHorizontalBox();
        setSize(showButtonBox, WEST_ITEM_WIDTH, 20);
        showButtonBox.add(jButton);

        this.panel.add(Box.createVerticalStrut(2));
        this.panel.add(showButtonBox);

        return jButton;
    }

    private void rearrangeColumnSize(TableColumnModel columnModel) {
        TableColumn metricColumn = columnModel.getColumn(0);
        setColumnSize(metricColumn, WEST_ITEM_WIDTH - 78);

        for (int i = 1; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(1);
            setColumnSize(column, 75);
        }
    }

    public void reload(Speculator speculator) {
        this.speculator = speculator;

        this.optBtn.setEnabled(true);

        DefaultTableModel model = (DefaultTableModel) this.table.getModel();

        model.setColumnCount(0);
        model.addColumn("Metric");
        model.addColumn("Value");

        TableColumnModel columnModel = this.table.getColumnModel();
        rearrangeColumnSize(columnModel);

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

    public void update(Speculator speculator) {
        this.speculator = speculator;

        DefaultTableModel model = (DefaultTableModel) this.table.getModel();

        if (model.getColumnCount() == 2) {
            model.addColumn("Optimization");

            TableColumnModel columnModel = this.table.getColumnModel();
            rearrangeColumnSize(columnModel);
        }

        TheOracle theOracle = speculator.getTheOracle();
        AnalysisCriterionCalculator calculator = theOracle.getCriterionCalculator();

        model.setValueAt(calculator.backtestPosCount(), 0, 2);
        model.setValueAt(calculator.backtestBarCount(), 1, 2);

        model.setValueAt(RESULT_FORMAT.format(calculator.backtestStrategyReturn().doubleValue()), 2, 2);
        model.setValueAt(RESULT_FORMAT.format(calculator.backtestBuyAndHold().doubleValue()), 3, 2);
        model.setValueAt(RESULT_FORMAT.format(calculator.backtestVersus().doubleValue() * 100), 4, 2);
        model.setValueAt(RESULT_FORMAT.format(calculator.backtestWinnigRatio().doubleValue() * 100), 5, 2);
    }

    public void addOptimizeDoneListener(Consumer<Speculator> operation) {
        this.onOptimizeDoneListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }
}
