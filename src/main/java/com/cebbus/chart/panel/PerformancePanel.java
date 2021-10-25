package com.cebbus.chart.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.chart.ColorPalette;
import lombok.Data;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;

import javax.swing.*;
import javax.swing.plaf.basic.BasicIconFactory;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.cebbus.chart.ColorPalette.*;

public class PerformancePanel {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.0000");

    private final BarSeries series;
    private final TradingRecord tradingRecord;
    private final TradingRecord backtestRecord;
    private final Map<String, JLabel> infoLabelMap = new LinkedHashMap<>();

    private int lastPositionCount;

    public PerformancePanel(TheOracle theOracle) {
        this.series = theOracle.getSeries();
        this.tradingRecord = theOracle.getTradingRecord();
        this.backtestRecord = theOracle.backtest();
    }

    public JPanel create() {
        AtomicInteger rowNum = new AtomicInteger(0);

        JPanel panel = new JPanel(new GridBagLayout());

        panel.add(createTitleLabel("Backtest Results"), createConst(rowNum, 0));
        panel.add(new JLabel(""), createConst(rowNum, 1));

        List<CriterionResult> backtestResultList = createCriterionMap(this.backtestRecord);
        backtestResultList.forEach(r -> {
            panel.add(createThinLabel(r.getLabel()), createConst(rowNum, 0));
            panel.add(createValueLabel(r), createConst(rowNum, 1));
        });

        //add empty row
        panel.add(new JLabel(" "), createConst(rowNum, 0));
        panel.add(new JLabel(" "), createConst(rowNum, 1));

        panel.add(createTitleLabel("Current Results"), createConst(rowNum, 0));
        panel.add(new JLabel(""), createConst(rowNum, 1));

        List<CriterionResult> currentResultList = createCriterionMap(this.tradingRecord);
        currentResultList.forEach(r -> this.infoLabelMap.put(r.getLabel(), createValueLabel(r)));

        this.infoLabelMap.forEach((s, l) -> {
            panel.add(createThinLabel(s), createConst(rowNum, 0));
            panel.add(l, createConst(rowNum, 1));
        });

        return panel;
    }

    public void refresh() {
        if (this.tradingRecord.getPositionCount() > this.lastPositionCount) {
            List<CriterionResult> currentResultList = createCriterionMap(this.tradingRecord);
            currentResultList.forEach(r -> {
                JLabel label = this.infoLabelMap.get(r.getLabel());
                updateValueLabel(label, r);
            });

            this.lastPositionCount = this.tradingRecord.getPositionCount();
        }
    }

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ColorPalette.BLUE);
        label.setIcon(BasicIconFactory.getMenuArrowIcon());

        return label;
    }

    private GridBagConstraints createConst(AtomicInteger rowNum, int columnNum) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columnNum;
        c.gridy = columnNum == 0 ? rowNum.get() : rowNum.getAndIncrement();
        c.anchor = columnNum == 0 ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
        c.weightx = 0.5;

        return c;
    }

    private JLabel createThinLabel() {
        JLabel label = new JLabel();

        Font f = label.getFont();
        label.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
        return label;
    }

    private JLabel createThinLabel(String text) {
        JLabel label = createThinLabel();
        label.setText(text);

        return label;
    }

    private JLabel createValueLabel(CriterionResult result) {
        JLabel label = createThinLabel();
        updateValueLabel(label, result);
        return label;
    }

    private void updateValueLabel(JLabel label, CriterionResult result) {
        label.setText(result.getValue());
        label.setForeground(result.getColor());
    }

    private List<CriterionResult> createCriterionMap(TradingRecord tradingRecord) {
        List<CriterionResult> resultList = new ArrayList<>();

        int positionCount = tradingRecord.getPositionCount();
        resultList.add(new CriterionResult("Number of Pos", Integer.toString(positionCount), DARK_GRAY));

        NumberOfBarsCriterion numberOfBarsCriterion = new NumberOfBarsCriterion();
        int numOfBars = numberOfBarsCriterion.calculate(this.series, tradingRecord).intValue();
        resultList.add(new CriterionResult("Number of Bars", Integer.toString(numOfBars), DARK_GRAY));

        GrossReturnCriterion returnCriterion = new GrossReturnCriterion();
        double totalReturn = returnCriterion.calculate(this.series, tradingRecord).doubleValue();
        resultList.add(new CriterionResult("Strategy Return",
                DECIMAL_FORMAT.format(totalReturn),
                totalReturn > 0 ? GREEN : RED));

        BuyAndHoldReturnCriterion buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
        double buyAndHold = buyAndHoldReturnCriterion.calculate(this.series, tradingRecord).doubleValue();
        resultList.add(new CriterionResult("Buy and Hold Return",
                DECIMAL_FORMAT.format(buyAndHold),
                buyAndHold > 0 ? GREEN : RED));

        VersusBuyAndHoldCriterion versusBuyAndHoldCriterion = new VersusBuyAndHoldCriterion(returnCriterion);
        double versus = versusBuyAndHoldCriterion.calculate(this.series, tradingRecord).doubleValue();
        resultList.add(new CriterionResult("Strategy vs Hold (%)",
                DECIMAL_FORMAT.format(versus * 100),
                versus > 1 ? GREEN : RED));

        WinningPositionsRatioCriterion winningRatioCriterion = new WinningPositionsRatioCriterion();
        double winningRatio = winningRatioCriterion.calculate(this.series, tradingRecord).doubleValue();
        resultList.add(new CriterionResult("Strategy Winning Ratio (%)",
                DECIMAL_FORMAT.format(winningRatio * 100),
                winningRatio > 0.75 ? GREEN : RED));

        return resultList;
    }

    @Data
    private static class CriterionResult {
        private final String label;
        private final String value;
        private final Color color;
    }
}
