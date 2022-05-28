package com.cebbus.dto;

import lombok.Data;

import java.awt.*;

@Data
public class CriterionResultDto {
    private final String label;
    private final Object value;
    private final String formattedValue;
    private final Color color;
}
