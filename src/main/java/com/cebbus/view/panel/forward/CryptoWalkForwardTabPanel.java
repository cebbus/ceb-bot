package com.cebbus.view.panel.forward;

import com.cebbus.view.chart.ColorPalette;
import com.cebbus.view.panel.BoxTitlePanel;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CryptoWalkForwardTabPanel {

    static final int WEST_WIDTH = 250;
    static final int WEST_ITEM_WIDTH = 234;

    private final JPanel container;
    private final WalkForwardFormPanel formPanel;
    private final WalkForwardResultDetailTable resultDetailTable;
    private final WalkForwardChartPanel chartPanel;
    private final WalkForwardStepResultTable stepResultTable;

    public CryptoWalkForwardTabPanel() {
        this.container = new JPanel(new BorderLayout());

        this.formPanel = new WalkForwardFormPanel();
        this.resultDetailTable = new WalkForwardResultDetailTable();
        this.chartPanel = new WalkForwardChartPanel();
        this.stepResultTable = new WalkForwardStepResultTable();

        addWestPanel();
        addChartListPanel();
        addStepResultPanel();
    }

    public void addWestPanel() {
        BoxTitlePanel titlePanel = new BoxTitlePanel("Walk Forward Form");

        Box box = Box.createVerticalBox();
        box.add(titlePanel.getPanel());

        addForm(box);
        addResultDetailTable(box);

        this.container.add(box, BorderLayout.WEST);
    }

    public void addForm(Box box) {
        this.formPanel.addRunClickListeners(this.chartPanel::recreate);
        this.formPanel.addRunClickListeners(this.resultDetailTable::reload);
        this.formPanel.addRunClickListeners(this.stepResultTable::complete);
        this.formPanel.addOnStepDoneListener(this.stepResultTable::addResult);

        box.add(this.formPanel.getPanel());
        box.add(Box.createVerticalStrut(10));
    }

    public void addResultDetailTable(Box box) {
        box.add(this.resultDetailTable.getPanel());
        box.add(Box.createVerticalStrut(10));
    }

    private void addChartListPanel() {
        JPanel panel = this.chartPanel.getPanel();
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorPalette.MIDDLE_GRAY));

        this.container.add(panel, BorderLayout.CENTER);
    }

    private void addStepResultPanel() {
        JTable table = this.stepResultTable.getTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ColorPalette.MIDDLE_GRAY));
        scrollPane.setPreferredSize(new Dimension(250, 200));

        this.container.add(scrollPane, BorderLayout.SOUTH);
    }

    public JPanel getContainer() {
        return container;
    }
}
