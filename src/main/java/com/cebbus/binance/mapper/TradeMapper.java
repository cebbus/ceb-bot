package com.cebbus.binance.mapper;

import com.binance.api.client.domain.account.Trade;
import com.cebbus.dto.TradeDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class TradeMapper {

    private TradeMapper() {

    }

    public static TradeDto tradeToDto(Trade trade) {
        TradeDto dto = new TradeDto();
        dto.setId(trade.getId());
        dto.setPrice(new BigDecimal(trade.getPrice()));
        dto.setQty(new BigDecimal(trade.getQty()));
        dto.setQuoteQty(new BigDecimal(trade.getQuoteQty()));
        dto.setCommission(new BigDecimal(trade.getCommission()));
        dto.setCommissionAsset(trade.getCommissionAsset());
        dto.setTime(trade.getTime());
        dto.setSymbol(trade.getSymbol());
        dto.setBuyer(trade.isBuyer());
        dto.setMaker(trade.isMaker());
        dto.setBestMatch(trade.isBestMatch());
        dto.setOrderId(trade.getOrderId());
        return dto;
    }

    public static List<TradeDto> tradeToDto(List<Trade> tradeList) {
        return tradeList.stream().map(TradeMapper::tradeToDto).collect(Collectors.toList());
    }

}
