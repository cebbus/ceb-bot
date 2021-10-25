package com.cebbus.chart.panel;

import org.jfree.chart.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;

public class CryptoSplashFrame {

    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 75;

    private final JProgressBar bar;
    private final ApplicationFrame appFrame;

    public CryptoSplashFrame(int numOfSymbols) {
        this.bar = new JProgressBar(0, numOfSymbols);
        this.bar.setValue(0);
        this.bar.setStringPainted(true);

        this.appFrame = new ApplicationFrame("CebBot! Loading...");
        this.appFrame.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.appFrame.add(this.bar);
        this.appFrame.pack();
        this.appFrame.setLocationRelativeTo(null);
    }

    public void progress() {
        int value = this.bar.getValue();
        this.bar.setValue(value + 1);
    }

    public void show() {
        this.appFrame.setVisible(true);
    }

    public void hide() {
        this.appFrame.dispose();
    }
}
