package com.cebbus.view.panel.test;

import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.FormFieldSet;
import org.apache.commons.lang3.math.NumberUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.cebbus.view.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;

public class TestParameterTable extends FormFieldSet {

    private final Box panel;
    private final JTable table;
    private final JButton applyBtn;
    private final List<Consumer<Speculator>> onApplyListeners = new ArrayList<>();

    private Speculator speculator;

    public TestParameterTable() {
        this.panel = Box.createVerticalBox();

        this.table = createTable();
        this.applyBtn = createApplyButton();
    }

    private JTable createTable() {
        Box resultDetailLabel = createTitleLabelBox("Parameters", WEST_ITEM_WIDTH, 20);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Name");
        tableModel.addColumn("Value");

        JTable jTable = new JTable(tableModel);
        jTable.setFillsViewportHeight(true);

        TableColumnModel columnModel = jTable.getColumnModel();
        TableColumn column = columnModel.getColumn(1);
        setColumnSize(column, 75);

        JScrollPane scrollPane = new JScrollPane(jTable);
        setSize(scrollPane, WEST_ITEM_WIDTH, 150);

        this.panel.add(resultDetailLabel);
        this.panel.add(scrollPane);

        return jTable;
    }

    private JButton createApplyButton() {
        JButton jButton = new JButton("Apply");
        setSize(jButton, WEST_ITEM_WIDTH, 20);
        jButton.setEnabled(false);

        jButton.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) this.table.getModel();

            Number[] parameters = new Number[model.getRowCount()];
            for (int i = 0; i < model.getRowCount(); i++) {
                String value = model.getValueAt(i, 1).toString();

                if (NumberUtils.isCreatable(value)) {
                    parameters[i] = value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value);
                } else {
                    parameters[i] = 0;
                }
            }

            this.speculator.changeParameters(parameters);
            this.onApplyListeners.forEach(l -> l.accept(this.speculator));
        });

        Box applyBtnBox = Box.createHorizontalBox();
        setSize(applyBtnBox, WEST_ITEM_WIDTH, 20);
        applyBtnBox.add(jButton);

        this.panel.add(Box.createVerticalStrut(2));
        this.panel.add(applyBtnBox);

        return jButton;
    }

    public void reload(Speculator speculator) {
        this.speculator = speculator;

        this.applyBtn.setEnabled(true);

        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setRowCount(0);

        TheOracle theOracle = speculator.getTheOracle();
        Map<String, Number> parameterMap = theOracle.getProphesyParameterMap();
        parameterMap.forEach((k, v) -> model.addRow(new Object[]{k, v}));
    }

    public void addApplyListener(Consumer<Speculator> operation) {
        this.onApplyListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }

}
