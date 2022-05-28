package com.cebbus.view.panel.forward;

import com.cebbus.dto.CriterionResultDto;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.FormFieldSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.util.List;

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
        List<CriterionResultDto> criterionResultList = theOracle.getCriterionResultList(true);

        for (CriterionResultDto result : criterionResultList) {
            model.addRow(new Object[]{result.getLabel(), result.getFormattedValue()});
        }
    }

    public Box getPanel() {
        return panel;
    }
}
