package com.cebbus.view.panel.menu;

import com.cebbus.binance.Speculator;
import com.cebbus.util.PropertyReader;

import javax.swing.*;

public class PanelMenu {

    private final Speculator speculator;

    public PanelMenu(Speculator speculator) {
        this.speculator = speculator;
    }

    public JMenuBar create() {
        JMenuBar menuBar = new JMenuBar();

        if (PropertyReader.isCredentialsExist() && this.speculator.getSymbol().getWeight() > 0) {
            OrderMenu orderMenu = new OrderMenu(this.speculator);
            menuBar.add(orderMenu.create());

            StatusMenu statusMenu = new StatusMenu(this.speculator);
            menuBar.add(statusMenu.create());
        }

        StrategyMenu strategyMenu = new StrategyMenu(this.speculator);
        menuBar.add(strategyMenu.create());

        return menuBar;
    }
}
