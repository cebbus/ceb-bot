package com.cebbus.dto;

import lombok.Data;

@Data
public class TradePointDto {
    private final boolean buy;
    private final boolean backtest;
    private final Long tradeTime;
}
