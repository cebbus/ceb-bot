package com.cebbus.analysis;

import lombok.Data;

import java.awt.*;

@Data
public class CriterionResult {
    private final String label;
    private final Object value;
    private final String formattedValue;
    private final Color color;
}
