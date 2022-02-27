package com.cebbus.view.panel.test;

import com.cebbus.view.chart.ColorPalette;
import com.cebbus.view.panel.BoxTitlePanel;

import javax.swing.*;
import java.awt.*;

public class CryptoTestTabPanel {

    static final int WEST_WIDTH = 250;
    static final int WEST_ITEM_WIDTH = 234;

    private final JPanel container;
    private final TestFormPanel formPanel;
    private final TestResultTable resultTable;
    private final TestResultDetailTable resultDetailTable;
    private final TestParameterTable parameterTable;
    private final TestChartPanel chartPanel;

    public CryptoTestTabPanel() {
        this.container = new JPanel(new BorderLayout());

        this.formPanel = new TestFormPanel();
        this.resultTable = new TestResultTable();
        this.resultDetailTable = new TestResultDetailTable();
        this.parameterTable = new TestParameterTable();
        this.chartPanel = new TestChartPanel();

        addWestPanel();
        addChartListPanel();
    }

    public void addWestPanel() {
        BoxTitlePanel titlePanel = new BoxTitlePanel("Test Form");

        Box box = Box.createVerticalBox();
        box.add(titlePanel.getPanel());

        addForm(box);
        addResultTable(box);
        addResultDetailTable(box);
        addParameterTable(box);

        this.container.add(box, BorderLayout.WEST);
    }

    public void addForm(Box box) {
        this.formPanel.addRunClickListeners(this.resultTable::reload);

        box.add(this.formPanel.getPanel());
        box.add(Box.createVerticalStrut(10));
    }

    public void addResultTable(Box box) {
        this.resultTable.addDetailClickListener(this.chartPanel::recreate);
        this.resultTable.addDetailClickListener(this.resultDetailTable::reload);
        this.resultTable.addDetailClickListener(this.parameterTable::reload);

        box.add(this.resultTable.getPanel());
        box.add(Box.createVerticalStrut(10));
    }

    public void addResultDetailTable(Box box) {
        this.resultDetailTable.addOptimizeDoneListener(this.chartPanel::recreate);
        this.resultDetailTable.addOptimizeDoneListener(this.resultDetailTable::update);
        this.resultDetailTable.addOptimizeDoneListener(this.parameterTable::reload);

        box.add(this.resultDetailTable.getPanel());
        box.add(Box.createVerticalStrut(10));
    }

    public void addParameterTable(Box box) {
        this.parameterTable.addApplyListener(this.chartPanel::recreate);
        this.parameterTable.addApplyListener(this.resultDetailTable::update);

        box.add(this.parameterTable.getPanel());
        box.add(Box.createVerticalStrut(10));
    }

    private void addChartListPanel() {
        JPanel chart = this.chartPanel.getPanel();
        chart.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorPalette.MIDDLE_GRAY));

        this.container.add(chart, BorderLayout.CENTER);
    }

    public JPanel getContainer() {
        return container;
    }
}
