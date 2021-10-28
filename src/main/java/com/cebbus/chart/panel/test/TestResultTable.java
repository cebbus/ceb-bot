package com.cebbus.chart.panel.test;

import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.Num;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cebbus.chart.panel.test.CryptoTestTabPanel.RESULT_FORMAT;
import static com.cebbus.chart.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;

public class TestResultTable {

    private final Box panel;
    private final TestTitlePanel title;
    private final DefaultTableModel tableModel;
    private final List<Consumer<Speculator>> onDetailClickListeners = new ArrayList<>();

    private Speculator speculator;
    private List<String> strategies;

    public TestResultTable() {
        this.panel = Box.createVerticalBox();
        this.panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));

        this.title = new TestTitlePanel("Strategy Results", false);

        this.tableModel = new DefaultTableModel();

        createTable();
    }

    private void createTable() {
        this.tableModel.addColumn("Strategy");
        this.tableModel.addColumn("Result");

        JButton showButton = new JButton("Show Detail");
        setSize(showButton, WEST_ITEM_WIDTH, 20);
        showButton.setEnabled(false);
        showButton.addActionListener(e -> {
            BarSeries series = this.speculator.getTheOracle().getSeries();
            String strategy = String.join(" & ", this.strategies);

            TheOracle theOracle = new TheOracle(series, strategy);
            this.speculator.setTheOracle(theOracle);

            this.onDetailClickListeners.forEach(action -> action.accept(speculator));
        });

        Box showButtonBox = Box.createHorizontalBox();
        setSize(showButtonBox, WEST_ITEM_WIDTH, 20);
        showButtonBox.add(showButton);

        JTable table = new JTable(this.tableModel);
        table.setFillsViewportHeight(true);

        TableColumn valueColumn = table.getColumnModel().getColumn(1);
        valueColumn.setWidth(10);
        valueColumn.setMinWidth(10);
        valueColumn.setPreferredWidth(10);

        ListSelectionModel model = table.getSelectionModel();
        model.addListSelectionListener(e -> {
            showButton.setEnabled(true);

            this.strategies = Arrays.stream(model.getSelectedIndices())
                    .mapToObj(value -> this.tableModel.getValueAt(value, 0).toString())
                    .collect(Collectors.toList());
        });

        JScrollPane scrollPane = new JScrollPane(table);
        setSize(scrollPane, WEST_ITEM_WIDTH, 200);

        this.panel.add(scrollPane);
        this.panel.add(Box.createVerticalStrut(2));
        this.panel.add(showButtonBox);
    }

    private Object[] createTableRow(Pair<String, Num> result) {
        return new Object[]{result.getKey(), RESULT_FORMAT.format(result.getValue().doubleValue())};
    }

    private void setSize(JComponent component, int width, int height) {
        component.setMinimumSize(new Dimension(width, height));
        component.setMaximumSize(new Dimension(width, height));
        component.setPreferredSize(new Dimension(width, height));
    }

    public void reload(Speculator speculator) {
        this.speculator = speculator;

        this.tableModel.setRowCount(0);

        List<Pair<String, Num>> resultList = speculator.calcStrategies();
        resultList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        resultList.forEach(result -> this.tableModel.addRow(createTableRow(result)));
    }

    public void addDetailClickListener(Consumer<Speculator> operation) {
        this.onDetailClickListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }

    public TestTitlePanel getTitle() {
        return title;
    }
}
