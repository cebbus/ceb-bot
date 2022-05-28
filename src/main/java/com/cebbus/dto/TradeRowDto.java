package com.cebbus.dto;

import lombok.Data;

@Data
public class TradeRowDto {
    private final int id;
    private final boolean buy;
    private final Number amount;
    private final Number price;
    private final Long tradeTime;
}
