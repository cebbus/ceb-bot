package com.cebbus.binance.mapper;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;

public class CandlestickMapper {

    private CandlestickMapper() {
    }

    public static Candlestick valueOf(CandlestickEvent event) {
        Candlestick candlestick = new Candlestick();

        candlestick.setOpenTime(event.getOpenTime());
        candlestick.setOpen(event.getOpen());
        candlestick.setLow(event.getLow());
        candlestick.setHigh(event.getHigh());
        candlestick.setClose(event.getClose());
        candlestick.setCloseTime(event.getCloseTime());
        candlestick.setVolume(event.getVolume());
        candlestick.setNumberOfTrades(event.getNumberOfTrades());
        candlestick.setQuoteAssetVolume(event.getQuoteAssetVolume());
        candlestick.setTakerBuyQuoteAssetVolume(event.getTakerBuyQuoteAssetVolume());
        candlestick.setTakerBuyBaseAssetVolume(event.getTakerBuyQuoteAssetVolume());

        return candlestick;
    }

}
