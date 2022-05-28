package com.cebbus.dto;

import lombok.Data;

@Data
public class CandleDto {
    private Long openTime;
    private Long closeTime;
    private Number open;
    private Number high;
    private Number low;
    private Number close;
    private Number volume;
    private Number quoteAssetVolume;
    private Number takerBuyBaseAssetVolume;
    private Number takerBuyQuoteAssetVolume;
    private Long numberOfTrades;
}
