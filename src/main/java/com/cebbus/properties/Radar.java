package com.cebbus.properties;

import com.cebbus.dto.CsIntervalAdapter;
import lombok.Data;

@Data
public class Radar {
    private final boolean active;
    private final String quote;
    private final CsIntervalAdapter interval;
}