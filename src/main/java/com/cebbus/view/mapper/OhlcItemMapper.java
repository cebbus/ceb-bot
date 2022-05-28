package com.cebbus.view.mapper;

import com.cebbus.dto.CandleDto;
import com.cebbus.util.DateTimeUtil;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCItem;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class OhlcItemMapper {

    private OhlcItemMapper() {
    }

    public static OHLCItem dtoToItem(CandleDto candle) {
        return new OHLCItem(convertTimeToPeriod(candle.getOpenTime()),
                candle.getOpen().doubleValue(),
                candle.getHigh().doubleValue(),
                candle.getLow().doubleValue(),
                candle.getClose().doubleValue());

    }

    public static List<OHLCItem> dtoToItem(List<CandleDto> candleList) {
        return candleList.stream().map(OhlcItemMapper::dtoToItem).collect(Collectors.toList());
    }

    static RegularTimePeriod convertTimeToPeriod(Long beginTimeInMillis) {
        ZonedDateTime beginTime = DateTimeUtil.millisToSystemTime(beginTimeInMillis);
        LocalDateTime localDateTime = beginTime.toLocalDateTime().plus(1, ChronoUnit.MILLIS);
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        return new Millisecond(timestamp);
    }
}
