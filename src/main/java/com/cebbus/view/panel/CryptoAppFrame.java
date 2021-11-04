package com.cebbus.view.panel;

import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel;
import com.cebbus.view.panel.test.CryptoTestTabPanel;
import org.jfree.chart.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

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

    public void addTab(Speculator speculator) {
        Objects.requireNonNull(speculator.getTheOracle());

        CryptoTabPanel tabPanel = new CryptoTabPanel(speculator);
        this.tabPane.addTab(speculator.getTheOracle().getSeries().getName(), tabPanel.getContainer());

        speculator.addCandlestickEventOperation(response -> tabPanel.refresh());
        speculator.addStatusChangeListener(tabPanel::changeStatus);
        speculator.addManualTradeListeners(success -> {
            if (Boolean.TRUE.equals(success)) {
                tabPanel.refresh();
            }
        });
    }

    public void addTestTab() {
        CryptoTestTabPanel tabPanel = new CryptoTestTabPanel();
        this.tabPane.addTab("BACKTEST", tabPanel.getContainer());
    }

    public void addWalkForwardTab() {
        CryptoWalkForwardTabPanel tabPanel = new CryptoWalkForwardTabPanel();
        this.tabPane.addTab("WALK FORWARD", tabPanel.getContainer());
    }

    public void show() {
        this.appFrame.setVisible(true);
    }
}
