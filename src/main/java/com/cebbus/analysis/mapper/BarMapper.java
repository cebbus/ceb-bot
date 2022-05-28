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

    public static Bar dtoToBar(CandleDto candlestick, CsIntervalAdapter interval) {
        ZonedDateTime closeTime = DateTimeUtil.millisToZonedTime(candlestick.getCloseTime());

        BigDecimal open = new BigDecimal(candlestick.getOpen().toString());
        BigDecimal high = new BigDecimal(candlestick.getHigh().toString());
        BigDecimal low = new BigDecimal(candlestick.getLow().toString());
        BigDecimal close = new BigDecimal(candlestick.getClose().toString());
        BigDecimal volume = new BigDecimal(candlestick.getVolume().toString());

        return new BaseBar(interval.getDuration(), closeTime, open, high, low, close, volume);
    }

    public static List<Bar> dtoToBar(List<CandleDto> candlestickList, CsIntervalAdapter interval) {
        return candlestickList.stream().map(c -> BarMapper.dtoToBar(c, interval)).collect(Collectors.toList());
    }

    public static CandleDto barToDto(Bar bar) {
        CandleDto dto = new CandleDto();

        dto.setOpenTime(DateTimeUtil.zonedTimeToMillis(bar.getBeginTime()));
        dto.setCloseTime(DateTimeUtil.zonedTimeToMillis(bar.getEndTime()));
        dto.setOpen(bar.getOpenPrice().getDelegate());
        dto.setHigh(bar.getHighPrice().getDelegate());
        dto.setLow(bar.getLowPrice().getDelegate());
        dto.setClose(bar.getClosePrice().getDelegate());
        dto.setVolume(bar.getVolume().getDelegate());

        return dto;
    }

    public static List<CandleDto> barToDto(List<Bar> barList) {
        return barList.stream().map(BarMapper::barToDto).collect(Collectors.toList());
    }
}
