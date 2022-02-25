package com.cebbus.view.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel;
import com.cebbus.view.panel.test.CryptoTestTabPanel;
import lombok.Data;
import org.jfree.chart.ui.ApplicationFrame;

import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
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
        addTab(speculator, null);
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

    private void addTab(Speculator speculator, Integer index) {
        TheOracle theOracle = speculator.getTheOracle();
        Objects.requireNonNull(theOracle);

        CryptoTabPanel tabPanel = new CryptoTabPanel(speculator);

        String name = speculator.getSymbol().getName();
        JPanel container = tabPanel.getContainer();
        if (index == null) {
            this.tabPane.addTab(name, container);
        } else {
            this.tabPane.insertTab(name, MetalIconFactory.getTreeFloppyDriveIcon(), container, null, index);
        }

        ListenerList listenerList = addListeners(speculator, tabPanel);
        speculator.addParameterChangeListener(spec -> {
            removeListeners(listenerList);

            int selectedIndex = this.tabPane.getSelectedIndex();
            removeTab(selectedIndex);
            addTab(spec, selectedIndex);
        });
    }

    private ListenerList addListeners(Speculator speculator, CryptoTabPanel tabPanel) {
        int csEventIndex = speculator.addCandlestickEventOperation(response -> tabPanel.refresh());
        int statusChangeIndex = speculator.addStatusChangeListener(tabPanel::changeStatus);
        int manualTradeIndex = speculator.addManualTradeListeners(success -> {
            if (Boolean.TRUE.equals(success)) {
                tabPanel.refresh();
            }
        });

        return new ListenerList(speculator, csEventIndex, statusChangeIndex, manualTradeIndex);
    }

    private void removeListeners(ListenerList listenerList) {
        Speculator speculator = listenerList.getSpeculator();
        speculator.removeCandlestickEventOperation(listenerList.getCsEventIndex());
        speculator.removeStatusChangeListener(listenerList.getStatusChangeIndex());
        speculator.removeManualTradeListeners(listenerList.getManualTradeIndex());
        speculator.clearParameterChangeListener();
    }

    private void removeTab(int index) {
        this.tabPane.invalidate();
        this.tabPane.removeTabAt(index);
        this.tabPane.revalidate();
    }

    @Data
    private static class ListenerList {
        private final Speculator speculator;
        private final int csEventIndex;
        private final int statusChangeIndex;
        private final int manualTradeIndex;
    }
}
