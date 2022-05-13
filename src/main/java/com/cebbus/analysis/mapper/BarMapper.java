package com.cebbus.analysis.mapper;

import com.cebbus.dto.CandleDto;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.util.DateTimeUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BarMapper {

    private BarMapper() {
    }

    public static List<Bar> valueOf(List<CandleDto> candlestickList, CsIntervalAdapter interval) {
        return candlestickList.stream().map(c -> BarMapper.valueOf(c, interval)).collect(Collectors.toList());
    }

    public static Bar valueOf(CandleDto candlestick, CsIntervalAdapter interval) {
        ZonedDateTime dateTime = DateTimeUtil.millisToZonedTime(candlestick.getCloseTime());

        BigDecimal open = new BigDecimal(candlestick.getOpen());
        BigDecimal high = new BigDecimal(candlestick.getHigh());
        BigDecimal low = new BigDecimal(candlestick.getLow());
        BigDecimal close = new BigDecimal(candlestick.getClose());
        BigDecimal volume = new BigDecimal(candlestick.getVolume());

        return new BaseBar(interval.getDuration(), dateTime, open, high, low, close, volume);
    }
}
