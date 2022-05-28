package com.cebbus.view.chart;

import com.cebbus.analysis.TheOracle;
import com.cebbus.dto.IndicatorValueDto;
import com.cebbus.dto.TradePointDto;
import com.cebbus.view.mapper.TimeSeriesItemMapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import javax.swing.*;
import java.util.*;

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
            IndicatorValueDto indicatorValue = this.theOracle.getLastIndicatorValue(indicator);

            TimeSeriesDataItem lastItem = TimeSeriesItemMapper.dtoToItem(indicatorValue);
            timeSeries.addOrUpdate(lastItem);
        });

        addTradeMarker(true);
        addTradeMarker(false);
    }

    private TimeSeries createChartData(String name, CachedIndicator<Num> indicator) {
        List<IndicatorValueDto> dataList = this.theOracle.getIndicatorValueList(indicator);
        List<TimeSeriesDataItem> itemList = TimeSeriesItemMapper.dtoToItem(dataList);

        TimeSeries timeSeries = new TimeSeries(name);
        itemList.forEach(timeSeries::add);

        return timeSeries;
    }

    private void addTradeMarker(boolean backtest) {
        Optional<TradePointDto> tradePoint = this.theOracle.getLastTradePoint(backtest);
        tradePoint.ifPresent(point -> {
            XYPlot xyPlot = this.chart.getXYPlot();

            CryptoMarker marker = createMarker(point);
            xyPlot.removeDomainMarker(marker);
            xyPlot.addDomainMarker(marker);
        });
    }
}
