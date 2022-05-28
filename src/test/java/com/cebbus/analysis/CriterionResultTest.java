package com.cebbus.analysis;

import com.cebbus.dto.CriterionResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CriterionResultTest {

    private CriterionResultDto result;

    @BeforeEach
    void setUp() {
        this.result = new CriterionResultDto("label", 1, "1F", Color.BLUE);
    }

    @Test
    void getLabel() {
        assertEquals("label", this.result.getLabel());
    }

    @Test
    void getValue() {
        assertEquals(1, this.result.getValue());
    }

    @Test
    void getFormattedValue() {
        assertEquals("1F", this.result.getFormattedValue());
    }

    @Test
    void getColor() {
        assertEquals(Color.BLUE, this.result.getColor());
    }
}