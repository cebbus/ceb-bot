package com.cebbus.view.panel.forward;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CryptoWalkForwardTabPanel {

    static final int WEST_WIDTH = 250;
    static final int WEST_ITEM_WIDTH = 234;
    static final DecimalFormat RESULT_FORMAT = new DecimalFormat("#,###.0000");

    private final JPanel container;
    private final WalkForwardFormPanel formPanel;
    private final WalkForwardChartPanel chartPanel;

    public CryptoWalkForwardTabPanel() {
        this.container = new JPanel(new BorderLayout());

        this.formPanel = new WalkForwardFormPanel();
        this.chartPanel = new WalkForwardChartPanel();

        addWestPanel();
        addChartListPanel();
    }

    public void addWestPanel() {
        Box box = Box.createVerticalBox();
        addForm(box);
        //addResultTable(box);
        //addResultDetailTable(box);

        this.container.add(box, BorderLayout.WEST);
    }

    public void addForm(Box box) {
        this.formPanel.addRunClickListeners(this.chartPanel::recreate);

        box.add(this.formPanel.getTitlePanel());
        box.add(this.formPanel.getPanel());
    }

    private void addChartListPanel() {
        JPanel chart = this.chartPanel.getPanel();
        this.container.add(chart, BorderLayout.CENTER);
    }

    public JPanel getContainer() {
        return container;
    }
}
