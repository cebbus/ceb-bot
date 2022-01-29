package com.cebbus.view.panel.test;

import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.FormFieldSet;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cebbus.view.panel.test.CryptoTestTabPanel.RESULT_FORMAT;
import static com.cebbus.view.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;

public class TestResultTable extends FormFieldSet {

    private final Box panel;
    private final JTable table;
    private final JButton showBtn;
    private final List<Consumer<Speculator>> onDetailClickListeners = new ArrayList<>();

    private Speculator speculator;
    private List<String> strategies;

    public TestResultTable() {
        this.panel = Box.createVerticalBox();

        this.table = createTable();
        this.showBtn = createShowButton();
    }

    private JTable createTable() {
        Box resultLabel = createTitleLabelBox("Strategy Results", WEST_ITEM_WIDTH, 20);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Strategy");
        tableModel.addColumn("Result");

        JTable jTable = new JTable(tableModel);
        jTable.setFillsViewportHeight(true);

        TableColumnModel columnModel = jTable.getColumnModel();
        TableColumn valueColumn = columnModel.getColumn(1);
        valueColumn.setMaxWidth(75);

        ListSelectionModel model = jTable.getSelectionModel();
        model.addListSelectionListener(e -> {
            int[] selectedIndices = model.getSelectedIndices();
            if (selectedIndices.length == 0) {
                return;
            }

            this.showBtn.setEnabled(true);

            this.strategies = Arrays.stream(selectedIndices)
                    .mapToObj(value -> tableModel.getValueAt(value, 0).toString())
                    .collect(Collectors.toList());
        });

        JScrollPane scrollPane = new JScrollPane(jTable);
        setSize(scrollPane, WEST_ITEM_WIDTH, 200);

        this.panel.add(resultLabel);
        this.panel.add(scrollPane);

        return jTable;
    }

    private JButton createShowButton() {
        JButton jButton = new JButton("Show Detail");
        setSize(jButton, WEST_ITEM_WIDTH, 20);
        jButton.setEnabled(false);
        jButton.addActionListener(e -> {
            BarSeries series = this.speculator.getTheOracle().getSeries();
            String strategy = String.join(" & ", this.strategies);
            CebStrategy cebStrategy = StrategyFactory.create(series, strategy);

            this.speculator.setTheOracle(new TheOracle(cebStrategy));

            this.onDetailClickListeners.forEach(action -> action.accept(this.speculator));
        });

        Box showButtonBox = Box.createHorizontalBox();
        setSize(showButtonBox, WEST_ITEM_WIDTH, 20);
        showButtonBox.add(jButton);

        this.panel.add(Box.createVerticalStrut(2));
        this.panel.add(showButtonBox);

        return jButton;
    }

    private Object[] createTableRow(Pair<String, Num> result) {
        return new Object[]{result.getKey(), RESULT_FORMAT.format(result.getValue().doubleValue())};
    }

    public void reload(Speculator speculator) {
        this.speculator = speculator;

        this.showBtn.setEnabled(false);

        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setRowCount(0);

        List<Pair<String, Num>> resultList = speculator.calcStrategies();
        resultList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        resultList.forEach(result -> model.addRow(createTableRow(result)));
    }

    public void addDetailClickListener(Consumer<Speculator> operation) {
        this.onDetailClickListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }
}
