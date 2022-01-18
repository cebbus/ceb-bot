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

    public static CandlestickEvent valueOf(Candlestick candlestick, String symbol) {
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
