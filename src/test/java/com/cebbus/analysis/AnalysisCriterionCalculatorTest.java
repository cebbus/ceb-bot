package com.cebbus.analysis;

import com.cebbus.dto.CriterionResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;

import java.util.ArrayList;
import java.util.List;

import static com.cebbus.view.chart.ColorPalette.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class AnalysisCriterionCalculatorTest {

    private AnalysisCriterionCalculator calculator;

    @BeforeEach
    void setUp() {
        BarSeries series = DataGenerator.generateSeries();

        TradingRecord tradingRecord = DataGenerator.generateRecord();
        tradingRecord.exit(1, DecimalNum.valueOf("3.5"), DecimalNum.valueOf("1"));

        TradingRecord backtesetRecord = DataGenerator.generateRecord();
        backtesetRecord.exit(1, DecimalNum.valueOf("5.5"), DecimalNum.valueOf("1"));

        this.calculator = new AnalysisCriterionCalculator(series, tradingRecord, backtesetRecord);
    }

    @Test
    void backtestStrategyReturn() {
        assertEquals(DecimalNum.valueOf("5.5"), this.calculator.backtestStrategyReturn());
    }

    @Test
    void backtestBuyAndHold() {
        assertEquals(DecimalNum.valueOf("11"), this.calculator.backtestBuyAndHold());
    }

    @Test
    void getCriterionResultList() {
        TradingRecord rec = createTradeRecord();
        this.calculator.setTradingRecord(rec);

        List<CriterionResultDto> expected = createResultList();
        List<CriterionResultDto> actual = this.calculator.getCriterionResultList(false);
        assertIterableEquals(expected, actual);
    }

    @Test
    void getCriterionResultListBacktest() {
        TradingRecord rec = createTradeRecord();
        this.calculator.setBacktestRecord(rec);

        List<CriterionResultDto> expected = createResultList();
        List<CriterionResultDto> actual = this.calculator.getCriterionResultList(true);
        assertIterableEquals(expected, actual);
    }

    private TradingRecord createTradeRecord() {
        TradingRecord rec = new BaseTradingRecord();

        // first position - 10x
        rec.enter(0, DecimalNum.valueOf("1"), DecimalNum.valueOf("1"));
        rec.exit(1, DecimalNum.valueOf("10"), DecimalNum.valueOf("1"));

        // second position - 0.5x
        rec.enter(2, DecimalNum.valueOf("8"), DecimalNum.valueOf("1"));
        rec.exit(3, DecimalNum.valueOf("4"), DecimalNum.valueOf("1"));

        return rec;
    }

    private List<CriterionResultDto> createResultList() {
        List<CriterionResultDto> resultList = new ArrayList<>();
        resultList.add(new CriterionResultDto("Number of Pos", 2, "2", DARK_GRAY));
        resultList.add(new CriterionResultDto("Number of Bars", 4, "4", DARK_GRAY));
        resultList.add(new CriterionResultDto("Strategy Return", 5.0, "5.0000", GREEN));
        resultList.add(new CriterionResultDto("Buy and Hold Return", 11.0, "11.0000", GREEN));
        resultList.add(new CriterionResultDto("Strategy vs Hold (%)", 0.45454545454545453, "45.4545", RED));
        resultList.add(new CriterionResultDto("Strategy Winning Ratio (%)", 0.5, "50.0000", RED));

        return resultList;
    }
}