package com.cebbus.analysis;

import com.cebbus.util.PropertyReader;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.ArrayList;
import java.util.List;

public class SeriesHelper {

    private final BarSeries series;

    SeriesHelper(BarSeries series) {
        this.series = series;
    }

    SeriesHelper(Symbol symbol, List<Bar> barList) {
        this.series = new BaseBarSeriesBuilder()
                .withBars(barList)
                .withName(symbol.getName())
                .withMaxBarCount(PropertyReader.getCacheSize())
                .build();
    }

    String getName() {
        return this.series.getName();
    }

    void addBar(Bar newBar) {
        Bar last = getLastBar();
        boolean replace = newBar.getBeginTime().equals(last.getBeginTime());

        this.series.addBar(newBar, replace);
    }

    Bar getBar(int index) {
        return this.series.getBar(index);
    }

    Bar getLastBar() {
        return this.series.getLastBar();
    }

    List<Bar> getCandleDataList() {
        int startIndex = getStartIndex();
        int endIndex = this.series.getEndIndex();
        return this.series.getSubSeries(startIndex, endIndex + 1).getBarData();
    }

    int getEndIndex() {
        return this.series.getEndIndex();
    }

    int getStartIndex() {
        return Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
    }

    List<Integer> getSeriesIndexList() {
        List<Integer> indexList = new ArrayList<>();

        int startIndex = getStartIndex();
        int endIndex = getEndIndex();
        for (int i = startIndex; i <= endIndex; i++) {
            indexList.add(i);
        }

        return indexList;
    }

    public BarSeries getSeries() {
        return series;
    }
}
