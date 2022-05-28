package com.cebbus.dto;

import lombok.Data;

@Data
public class IndicatorValueDto {
    private final int index;
    private final Number value;
    private final Long dateTime;
}
