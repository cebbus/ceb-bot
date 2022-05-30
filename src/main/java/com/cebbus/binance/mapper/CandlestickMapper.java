package com.cebbus.binance.mapper;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.cebbus.dto.CandleDto;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CandlestickMapper {

    private CandlestickMapper() {
    }

    public static CandleDto candleToDto(Candlestick candlestick) {
        return copyFieldsToDto(candlestick);
    }

    public static List<CandleDto> candleToDto(List<Candlestick> candlestickList) {
        return candlestickList.stream().map(CandlestickMapper::candleToDto).collect(Collectors.toList());
    }

    public static CandleDto eventToDto(CandlestickEvent candlestick) {
        return copyFieldsToDto(candlestick);
    }

    public static CandlestickEvent stickToEvent(Candlestick candlestick, String symbol) {
        CandlestickEvent event = new CandlestickEvent();

        event.setOpenTime(candlestick.getOpenTime());
        event.setOpen(candlestick.getOpen());
        event.setLow(candlestick.getLow());
        event.setHigh(candlestick.getHigh());
        event.setClose(candlestick.getClose());
        event.setCloseTime(candlestick.getCloseTime());
        event.setVolume(candlestick.getVolume());
        event.setNumberOfTrades(candlestick.getNumberOfTrades());
        event.setQuoteAssetVolume(candlestick.getQuoteAssetVolume());
        event.setTakerBuyQuoteAssetVolume(candlestick.getTakerBuyQuoteAssetVolume());
        event.setTakerBuyBaseAssetVolume(candlestick.getTakerBuyQuoteAssetVolume());
        event.setSymbol(symbol);
        event.setBarFinal(true);

        return event;
    }

    private static CandleDto copyFieldsToDto(Object source) {
        CandleDto target = new CandleDto();

        copyFieldsToDto(target, source, "openTime", false);
        copyFieldsToDto(target, source, "closeTime", false);
        copyFieldsToDto(target, source, "numberOfTrades", false);
        copyFieldsToDto(target, source, "open", true);
        copyFieldsToDto(target, source, "high", true);
        copyFieldsToDto(target, source, "low", true);
        copyFieldsToDto(target, source, "close", true);
        copyFieldsToDto(target, source, "volume", true);
        copyFieldsToDto(target, source, "quoteAssetVolume", true);
        copyFieldsToDto(target, source, "takerBuyBaseAssetVolume", true);
        copyFieldsToDto(target, source, "takerBuyQuoteAssetVolume", true);

        return target;
    }

    private static void copyFieldsToDto(CandleDto target, Object source, String fieldName, boolean bigDecimal) {
        try {
            Object sourceValue = FieldUtils.readField(source, fieldName, true);
            if (bigDecimal) {
                sourceValue = new BigDecimal((String) sourceValue);
            }

            FieldUtils.writeField(target, fieldName, sourceValue, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
