package com.cebbus.chart.panel.test;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CryptoTestTabPanel {

    static final int WEST_WIDTH = 250;
    static final int WEST_ITEM_WIDTH = 234;
    static final DecimalFormat RESULT_FORMAT = new DecimalFormat("#,###.0000");

    private final JPanel container;
    private final TestFormPanel formPanel;
    private final TestResultTable resultTable;
    private final TestResultDetailTable resultDetailTable;
    private final TestChartPanel chartPanel;

    public CryptoTestTabPanel() {
        this.container = new JPanel(new BorderLayout());

        this.formPanel = new TestFormPanel();
        this.resultTable = new TestResultTable();
        this.resultDetailTable = new TestResultDetailTable();
        this.chartPanel = new TestChartPanel();

        addWestPanel();
        addChartListPanel();
    }

    public void addWestPanel() {
        Box box = Box.createVerticalBox();
        addForm(box);
        addResultTable(box);
        addResultDetailTable(box);

        this.container.add(box, BorderLayout.WEST);
    }

    public void addForm(Box box) {
        this.formPanel.addRunClickListeners(this.resultTable::reload);

        box.add(this.formPanel.getTitle().getPanel());
        box.add(this.formPanel.getPanel());
    }

    public void addResultTable(Box box) {
        this.resultTable.addDetailClickListener(this.chartPanel::recreate);
        this.resultTable.addDetailClickListener(this.resultDetailTable::reload);

        box.add(this.resultTable.getTitle().getPanel());
        box.add(this.resultTable.getPanel());
    }

    public void addResultDetailTable(Box box) {
        this.resultDetailTable.addOptimizeClickListener(this.chartPanel::recreate);
        this.resultDetailTable.addOptimizeClickListener(this.resultDetailTable::update);

        box.add(this.resultDetailTable.getTitle().getPanel());
        box.add(this.resultDetailTable.getPanel());
    }

    private void addChartListPanel() {
        JPanel chart = this.chartPanel.getPanel();
        this.container.add(chart, BorderLayout.CENTER);
    }

    public JPanel getContainer() {
        return container;
    }
}
