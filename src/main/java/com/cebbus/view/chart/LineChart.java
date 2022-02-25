package com.cebbus.view.chart;

import com.cebbus.analysis.TheOracle;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineChart extends CryptoChart {

    private JFreeChart chart;

    private final Map<String, CachedIndicator<Num>> indicatorMap;
    private final Map<String, TimeSeries> timeSeriesMap = new HashMap<>();

    public LineChart(TheOracle theOracle, Map<String, CachedIndicator<Num>> indicatorMap) {
        super(theOracle);
        this.indicatorMap = indicatorMap;
    }

    @Override
    public List<JMenuItem> createMenuList() {
        XYPlot xyPlot = this.chart.getXYPlot();

        JMenu markers = new JMenu("Markers");

        JMenuItem toggleTradeMarkers = new JMenuItem("Hide Trade");
        toggleTradeMarkers.addActionListener(new ToggleMarkerListener(xyPlot, false));

        JMenuItem toggleBacktestMarkers = new JMenuItem("Hide Backtest");
        toggleBacktestMarkers.addActionListener(new ToggleMarkerListener(xyPlot, true));

        markers.add(toggleTradeMarkers);
        markers.add(toggleBacktestMarkers);

        return Collections.singletonList(markers);
    }

    @Override
    public JFreeChart create() {
        this.indicatorMap.forEach((name, indicator) -> this.timeSeriesMap.put(name, createChartData(name, indicator)));

        TimeSeriesCollection axis = new TimeSeriesCollection();
        this.timeSeriesMap.forEach((name, timeSeries) -> axis.addSeries(timeSeries));

        this.chart = ChartFactory.createTimeSeriesChart(null, null,
                null, axis, true, true, false);

        this.chart.setBackgroundPaint(ColorPalette.SOFT_WIGHT);
        this.chart.getLegend().setBackgroundPaint(ColorPalette.SOFT_WIGHT);

        XYPlot xyPlot = this.chart.getXYPlot();
        xyPlot.setDrawingSupplier(new ChartDrawingSupplier());
        xyPlot.setBackgroundPaint(ColorPalette.LIGHT_GRAY);
        xyPlot.setFixedLegendItems(xyPlot.getLegendItems());

        addSignals(xyPlot);

        return this.chart;
    }

    @Override
    public void refresh() {
        if (this.chart == null) {
            return;
        }

        this.indicatorMap.forEach((name, indicator) -> {
            TimeSeries timeSeries = this.timeSeriesMap.get(name);

            if (!this.theOracle.isNewCandle()) {
                int count = timeSeries.getItemCount();
                RegularTimePeriod lastPeriod = timeSeries.getTimePeriod(count - 1);
                timeSeries.delete(lastPeriod);
            }

            timeSeries.add(this.theOracle.getLastSeriesItem(indicator));
        });

        XYPlot xyPlot = this.chart.getXYPlot();

        if (this.theOracle.hasNewTrade(true)) {
            addSignal(xyPlot, this.theOracle.getLastTradePoint(true));
        }

        if (this.theOracle.hasNewTrade(false)) {
            addSignal(xyPlot, this.theOracle.getLastTradePoint(false));
        }
    }

    private TimeSeries createChartData(String name, CachedIndicator<Num> indicator) {
        List<TimeSeriesDataItem> dataList = this.theOracle.getSeriesDataList(indicator);

        TimeSeries timeSeries = new TimeSeries(name);
        dataList.forEach(timeSeries::add);

        return timeSeries;
    }
}
