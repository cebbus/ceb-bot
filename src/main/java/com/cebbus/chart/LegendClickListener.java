package com.cebbus.chart;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

public class LegendClickListener implements ChartMouseListener {


    @Override
    public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
        ChartEntity entity = chartMouseEvent.getEntity();
        if (!LegendItemEntity.class.isAssignableFrom(entity.getClass())) {
            return;
        }

        LegendItemEntity itemEntity = (LegendItemEntity) entity;
        XYDataset dataset = (XYDataset) itemEntity.getDataset();
        int index = dataset.indexOf(itemEntity.getSeriesKey());

        XYPlot plot = (XYPlot) chartMouseEvent.getChart().getPlot();

        XYItemRenderer renderer = plot.getRenderer();
        boolean seriesVisible = renderer.isSeriesVisible(index);
        renderer.setSeriesVisible(index, !seriesVisible);
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
    }
}
