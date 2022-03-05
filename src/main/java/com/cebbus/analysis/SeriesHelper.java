package com.cebbus.analysis;

import com.cebbus.util.DateTimeUtil;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.ohlc.OHLCItem;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;

public class SeriesHelper {

    private final BarSeries series;

    SeriesHelper(BarSeries series) {
        this.series = series;
    }

    public void addBar(Bar newBar) {
        Bar lastBar = this.series.getLastBar();
        boolean replace = newBar.getBeginTime().equals(lastBar.getBeginTime());

        this.series.addBar(newBar, replace);
    }

    public OHLCItem getLastCandle() {
        return createItem(this.series.getLastBar());
    }

    public TimeSeriesDataItem getLastSeriesItem(CachedIndicator<Num> indicator) {
        return createSeriesItem(this.series.getEndIndex(), indicator);
    }

    public List<OHLCItem> getCandleDataList() {
        List<OHLCItem> itemList = new ArrayList<>();

        int startIndex = getStartIndex();
        int endIndex = this.series.getEndIndex();
        for (int i = startIndex; i <= endIndex; i++) {
            Bar bar = this.series.getBar(i);
            itemList.add(createItem(bar));
        }

        return itemList;
    }

    public List<TimeSeriesDataItem> getSeriesDataList(CachedIndicator<Num> indicator) {
        List<TimeSeriesDataItem> itemList = new ArrayList<>();

        int startIndex = getStartIndex();
        int endIndex = this.series.getEndIndex();
        for (int i = startIndex; i <= endIndex; i++) {
            itemList.add(createSeriesItem(i, indicator));
        }

        return itemList;
    }

    private OHLCItem createItem(Bar bar) {
        RegularTimePeriod period = DateTimeUtil.getBarPeriod(bar);

        double open = bar.getOpenPrice().doubleValue();
        double high = bar.getHighPrice().doubleValue();
        double low = bar.getLowPrice().doubleValue();
        double close = bar.getClosePrice().doubleValue();

        return new OHLCItem(period, open, high, low, close);
    }

    private TimeSeriesDataItem createSeriesItem(int index, CachedIndicator<Num> indicator) {
        Bar bar = this.series.getBar(index);
        RegularTimePeriod period = DateTimeUtil.getBarPeriod(bar);
        double value = indicator.getValue(index).doubleValue();

        return new TimeSeriesDataItem(period, value);
    }

    private int getStartIndex() {
        return Math.max(this.series.getRemovedBarsCount(), this.series.getBeginIndex());
    }
}
