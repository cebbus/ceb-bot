package com.cebbus.view.chart;

import com.cebbus.analysis.TheOracle;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class CandlestickChart extends CryptoChart {

    private JFreeChart chart;

    private final OHLCSeries ohlcSeries = new OHLCSeries("");

    public CandlestickChart(TheOracle theOracle) {
        super(theOracle);
    }

    @Override
    public List<JMenuItem> createMenuList() {
        return Collections.emptyList();
    }

    @Override
    public JFreeChart create() {
        OHLCDataset dataset = createChartData();
        this.chart = ChartFactory.createCandlestickChart(null, null, null, dataset, false);
        this.chart.setBackgroundPaint(ColorPalette.SOFT_WIGHT);

        XYPlot xyPlot = this.chart.getXYPlot();
        xyPlot.setBackgroundPaint(ColorPalette.LIGHT_GRAY);

        NumberAxis numberAxis = (NumberAxis) xyPlot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);

        CandlestickRenderer renderer = new CustomCandlestickRenderer();
        renderer.setUpPaint(ColorPalette.GREEN);
        renderer.setDownPaint(ColorPalette.RED);

        renderer.setAutoWidthMethod(1);
        xyPlot.setRenderer(renderer);

        return this.chart;
    }

    @Override
    public void refresh() {
        if (this.chart == null) {
            return;
        }

        if (!this.theOracle.isNewCandle()) {
            int count = this.ohlcSeries.getItemCount();
            this.ohlcSeries.remove(count - 1);
        }

        this.ohlcSeries.add(this.theOracle.getLastCandle());
    }

    private OHLCDataset createChartData() {
        List<OHLCItem> dataList = this.theOracle.getCandleDataList();
        dataList.forEach(this.ohlcSeries::add);

        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(this.ohlcSeries);

        return dataset;
    }
}
