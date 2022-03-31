package com.cebbus.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CriterionResultTest {

    private CriterionResult result;

    @BeforeEach
    void setUp() {
        this.result = new CriterionResult("label", 1, "1F", Color.BLUE);
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