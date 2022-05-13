package com.cebbus.dto;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CandleDto {

    private Long openTime;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private Long closeTime;
    private String quoteAssetVolume;
    private Long numberOfTrades;
    private String takerBuyBaseAssetVolume;
    private String takerBuyQuoteAssetVolume;

    public static CandleDto valueOf(Candlestick candlestick) {
        CandleDto dto = new CandleDto();
        dto.setOpenTime(candlestick.getOpenTime());
        dto.setOpen(candlestick.getOpen());
        dto.setHigh(candlestick.getHigh());
        dto.setLow(candlestick.getLow());
        dto.setClose(candlestick.getClose());
        dto.setVolume(candlestick.getVolume());
        dto.setCloseTime(candlestick.getCloseTime());
        dto.setQuoteAssetVolume(candlestick.getQuoteAssetVolume());
        dto.setNumberOfTrades(candlestick.getNumberOfTrades());
        dto.setTakerBuyBaseAssetVolume(candlestick.getTakerBuyBaseAssetVolume());
        dto.setTakerBuyQuoteAssetVolume(candlestick.getTakerBuyQuoteAssetVolume());

        return dto;
    }

    public static CandleDto valueOf(CandlestickEvent candlestick) {
        CandleDto dto = new CandleDto();
        dto.setOpenTime(candlestick.getOpenTime());
        dto.setOpen(candlestick.getOpen());
        dto.setHigh(candlestick.getHigh());
        dto.setLow(candlestick.getLow());
        dto.setClose(candlestick.getClose());
        dto.setVolume(candlestick.getVolume());
        dto.setCloseTime(candlestick.getCloseTime());
        dto.setQuoteAssetVolume(candlestick.getQuoteAssetVolume());
        dto.setNumberOfTrades(candlestick.getNumberOfTrades());
        dto.setTakerBuyBaseAssetVolume(candlestick.getTakerBuyBaseAssetVolume());
        dto.setTakerBuyQuoteAssetVolume(candlestick.getTakerBuyQuoteAssetVolume());

        return dto;
    }

    public static List<CandleDto> valueOf(List<Candlestick> candlestickList) {
        return candlestickList.stream().map(CandleDto::valueOf).collect(Collectors.toList());
    }

}
