package com.cebbus.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

public class CandlestickChart extends CryptoChart {

    public CandlestickChart(BarSeries series) {
        super(series);
    }

    public JFreeChart create() {
        OHLCDataset dataset = createChartData();
        JFreeChart chart = ChartFactory.createCandlestickChart(null, null, null, dataset, false);

        XYPlot xyPlot = chart.getXYPlot();
        setDateAxisFormat(xyPlot);

        NumberAxis numberAxis = (NumberAxis) xyPlot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);

        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setAutoWidthMethod(1);
        xyPlot.setRenderer(renderer);

        return chart;
    }

    private OHLCDataset createChartData() {
        OHLCSeries ohlcSeries = new OHLCSeries("");

        int startIndex = getStartIndex();
        int endIndex = this.series.getEndIndex();
        for (int i = startIndex; i < endIndex; i++) {
            Bar bar = this.series.getBar(i);
            ohlcSeries.add(createItem(bar));
        }

        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(ohlcSeries);

        return dataset;
    }

    private OHLCItem createItem(Bar bar) {
        RegularTimePeriod period = convertToPeriod(bar.getEndTime());

        double open = bar.getOpenPrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();

        return new OHLCItem(period, open, high, low, close);
    }
}
