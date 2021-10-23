package com.cebbus.chart.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import org.jfree.chart.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;

public class CryptoAppFrame {

    private static final int PANEL_WIDTH = 1400;
    private static final int PANEL_HEIGHT = 800;

    private final JTabbedPane tabPane;
    private final ApplicationFrame appFrame;

    public CryptoAppFrame() {
        this.tabPane = new JTabbedPane();
        this.tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        this.appFrame = new ApplicationFrame("CebBot!");
        this.appFrame.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.appFrame.add(this.tabPane);
        this.appFrame.pack();
        this.appFrame.setLocationRelativeTo(null);
    }

    public void addTab(TheOracle theOracle, Speculator speculator) {
        CryptoTabPanel tabPanel = new CryptoTabPanel(theOracle, speculator);
        this.tabPane.addTab(theOracle.getSeries().getName(), tabPanel.getPanel());

        speculator.addCandlestickEventOperation(response -> tabPanel.refresh());
        speculator.addStatusChangeListener(tabPanel::changeStatus);
        speculator.addManualTradeListeners(success -> {
            if (success) {
                tabPanel.refresh();
            }
        });
    }

    public void show() {
        this.appFrame.setVisible(true);
    }

}
