package com.cebbus.view.chart;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ToggleMarkerListener implements ActionListener {
    private final XYPlot plot;
    private final boolean backtest;

    private boolean hidden;
    private List<CryptoMarker> markerBackupList;

    ToggleMarkerListener(XYPlot plot, boolean backtest) {
        this.plot = plot;
        this.backtest = backtest;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.hidden) {
            changeTitle(e, "Hide");
            this.markerBackupList.forEach(this.plot::addDomainMarker);
        } else {
            changeTitle(e, "Show");
            this.markerBackupList = getMarkerList();
            this.markerBackupList.forEach(this.plot::removeDomainMarker);
        }

        this.hidden = !this.hidden;
    }

    private void changeTitle(ActionEvent e, String prefix) {
        String suffix = this.backtest ? "Backtest" : "Trade";

        JMenuItem item = (JMenuItem) e.getSource();
        item.setText(prefix + " " + suffix);
    }

    private List<CryptoMarker> getMarkerList() {
        Collection<?> markerList = this.plot.getDomainMarkers(Layer.FOREGROUND);

        List<CryptoMarker> filteredMarkerList = new ArrayList<>();

        for (Object object : markerList) {
            if (!CryptoMarker.class.isAssignableFrom(object.getClass())) {
                continue;
            }

            CryptoMarker marker = (CryptoMarker) object;
            if ((this.backtest && marker.isBacktest())
                    || (!this.backtest && !marker.isBacktest())) {
                filteredMarkerList.add(marker);
            }
        }

        return filteredMarkerList;
    }
}
