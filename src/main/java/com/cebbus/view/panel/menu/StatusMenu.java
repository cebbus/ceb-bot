package com.cebbus.view.panel.menu;

import com.cebbus.binance.Speculator;

import javax.swing.*;

public class StatusMenu {

    private final Speculator speculator;

    public StatusMenu(Speculator speculator) {
        this.speculator = speculator;
    }

    JMenu create() {
        JMenu status = new JMenu("Status");
        JMenuItem activate = new JMenuItem("Activate");
        activate.addActionListener(e -> this.speculator.activate());

        JMenuItem deactivate = new JMenuItem("Deactivate");
        deactivate.addActionListener(e -> this.speculator.deactivate());

        status.add(activate);
        status.add(deactivate);

        return status;
    }

}
