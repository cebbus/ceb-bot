package com.cebbus.binance.mapper;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.cebbus.dto.CandleDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CandlestickMapper {

    private CandlestickMapper() {
    }

    public static CandleDto candleToDto(Candlestick candlestick) {
        CandleDto dto = new CandleDto();
        dto.setOpenTime(candlestick.getOpenTime());
        dto.setCloseTime(candlestick.getCloseTime());
        dto.setOpen(new BigDecimal(candlestick.getOpen()));
        dto.setHigh(new BigDecimal(candlestick.getHigh()));
        dto.setLow(new BigDecimal(candlestick.getLow()));
        dto.setClose(new BigDecimal(candlestick.getClose()));
        dto.setVolume(new BigDecimal(candlestick.getVolume()));
        dto.setQuoteAssetVolume(new BigDecimal(candlestick.getQuoteAssetVolume()));
        dto.setNumberOfTrades(candlestick.getNumberOfTrades());
        dto.setTakerBuyBaseAssetVolume(new BigDecimal(candlestick.getTakerBuyBaseAssetVolume()));
        dto.setTakerBuyQuoteAssetVolume(new BigDecimal(candlestick.getTakerBuyQuoteAssetVolume()));

        return dto;
    }

    public static List<CandleDto> candleToDto(List<Candlestick> candlestickList) {
        return candlestickList.stream().map(CandlestickMapper::candleToDto).collect(Collectors.toList());
    }

    public static CandleDto eventToDto(CandlestickEvent candlestick) {
        CandleDto dto = new CandleDto();
        dto.setOpenTime(candlestick.getOpenTime());
        dto.setCloseTime(candlestick.getCloseTime());
        dto.setOpen(new BigDecimal(candlestick.getOpen()));
        dto.setHigh(new BigDecimal(candlestick.getHigh()));
        dto.setLow(new BigDecimal(candlestick.getLow()));
        dto.setClose(new BigDecimal(candlestick.getClose()));
        dto.setVolume(new BigDecimal(candlestick.getVolume()));
        dto.setQuoteAssetVolume(new BigDecimal(candlestick.getQuoteAssetVolume()));
        dto.setNumberOfTrades(candlestick.getNumberOfTrades());
        dto.setTakerBuyBaseAssetVolume(new BigDecimal(candlestick.getTakerBuyBaseAssetVolume()));
        dto.setTakerBuyQuoteAssetVolume(new BigDecimal(candlestick.getTakerBuyQuoteAssetVolume()));

        return dto;
    }

//    public static Candlestick valueOf(CandlestickEvent event) {
//        Candlestick candlestick = new Candlestick();
//
//        candlestick.setOpenTime(event.getOpenTime());
//        candlestick.setOpen(event.getOpen());
//        candlestick.setLow(event.getLow());
//        candlestick.setHigh(event.getHigh());
//        candlestick.setClose(event.getClose());
//        candlestick.setCloseTime(event.getCloseTime());
//        candlestick.setVolume(event.getVolume());
//        candlestick.setNumberOfTrades(event.getNumberOfTrades());
//        candlestick.setQuoteAssetVolume(event.getQuoteAssetVolume());
//        candlestick.setTakerBuyQuoteAssetVolume(event.getTakerBuyQuoteAssetVolume());
//        candlestick.setTakerBuyBaseAssetVolume(event.getTakerBuyQuoteAssetVolume());
//
//        return candlestick;
//    }

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

}
