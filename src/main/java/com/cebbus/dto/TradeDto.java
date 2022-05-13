package com.cebbus.dto;

import com.binance.api.client.domain.account.Trade;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class TradeDto {

    private Long id;
    private String price;
    private String qty;
    private String quoteQty;
    private String commission;
    private String commissionAsset;
    private long time;
    private String symbol;
    private boolean buyer;
    private boolean maker;
    private boolean bestMatch;
    private String orderId;

    public static TradeDto valueOf(Trade trade) {
        TradeDto dto = new TradeDto();
        dto.setId(trade.getId());
        dto.setPrice(trade.getPrice());
        dto.setQty(trade.getQty());
        dto.setQuoteQty(trade.getQuoteQty());
        dto.setCommission(trade.getCommission());
        dto.setCommissionAsset(trade.getCommissionAsset());
        dto.setTime(trade.getTime());
        dto.setSymbol(trade.getSymbol());
        dto.setBuyer(trade.isBuyer());
        dto.setMaker(trade.isMaker());
        dto.setBestMatch(trade.isBestMatch());
        dto.setOrderId(trade.getOrderId());
        return dto;
    }

    public static List<TradeDto> valueOf(List<Trade> tradeList) {
        return tradeList.stream().map(TradeDto::valueOf).collect(Collectors.toList());
    }

}
