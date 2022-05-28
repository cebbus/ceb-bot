package com.cebbus.dto;

import lombok.Data;

@Data
public class TradeDto {

    private Long id;
    private Long time;

    private Number price;
    private Number qty;
    private Number quoteQty;
    private Number commission;

    private boolean buyer;
    private boolean maker;
    private boolean bestMatch;

    private String symbol;
    private String orderId;
    private String commissionAsset;
}
