package com.cebbus.chart.panel;

import com.cebbus.analysis.TheOracle;
import com.cebbus.chart.ColorPalette;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.num.Num;

import javax.swing.*;
import javax.swing.plaf.basic.BasicIconFactory;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

        Map<String, Object> backtestMap = createCriterionMap(this.backtestRecord);
        backtestMap.forEach((s, o) -> {
            panel.add(createThinLabel(s), createConst(rowNum, 0));
            panel.add(createValueLabel(o, isPercentage(s)), createConst(rowNum, 1));
        });

        //add empty row
        panel.add(new JLabel(" "), createConst(rowNum, 0));
        panel.add(new JLabel(" "), createConst(rowNum, 1));

        panel.add(createTitleLabel("Current Results"), createConst(rowNum, 0));
        panel.add(new JLabel(""), createConst(rowNum, 1));

        Map<String, Object> currentMap = createCriterionMap(this.tradingRecord);
        currentMap.forEach((s, o) -> this.infoLabelMap.put(s, createValueLabel(o, isPercentage(s))));

        this.infoLabelMap.forEach((s, l) -> {
            panel.add(createThinLabel(s), createConst(rowNum, 0));
            panel.add(l, createConst(rowNum, 1));
        });

        return panel;
    }

    public void refresh() {
        if (this.tradingRecord.getPositionCount() > this.lastPositionCount) {
            Map<String, Object> currentMap = createCriterionMap(this.tradingRecord);
            currentMap.forEach((s, o) -> {
                JLabel label = this.infoLabelMap.get(s);
                boolean percent = isPercentage(s);

                updateValueLabel(label, o, percent);
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

    private JLabel createValueLabel(Object value, boolean percentage) {
        JLabel label = createThinLabel();
        updateValueLabel(label, value, percentage);

        return label;
    }

    private void updateValueLabel(JLabel label, Object value, boolean percentage) {
        label.setText(value.toString());

        if (percentage) {
            String val = value.toString().replace(",", "");

            if (isGreaterThanZero(val)) {
                label.setForeground(ColorPalette.GREEN);
            } else if (isLessThanZero(val)) {
                label.setForeground(ColorPalette.RED);
            } else {
                label.setForeground(ColorPalette.DARK_GRAY);
            }
        }
    }

    private Map<String, Object> createCriterionMap(TradingRecord tradingRecord) {
        GrossReturnCriterion returnCriterion = new GrossReturnCriterion();
        Num totalReturn = returnCriterion.calculate(this.series, tradingRecord);

        NumberOfBarsCriterion numberOfBarsCriterion = new NumberOfBarsCriterion();
        Num numOfBars = numberOfBarsCriterion.calculate(this.series, tradingRecord);

        WinningPositionsRatioCriterion winningRatioCriterion = new WinningPositionsRatioCriterion();
        Num winningRatio = winningRatioCriterion.calculate(this.series, tradingRecord);

        BuyAndHoldReturnCriterion buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
        Num buyAndHold = buyAndHoldReturnCriterion.calculate(this.series, tradingRecord);

        VersusBuyAndHoldCriterion versusBuyAndHoldCriterion = new VersusBuyAndHoldCriterion(returnCriterion);
        Num versus = versusBuyAndHoldCriterion.calculate(this.series, tradingRecord);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Number of Pos", tradingRecord.getPositionCount());
        map.put("Number of Bars", numOfBars.intValue());
        map.put("Total Return (%)", DECIMAL_FORMAT.format(calcPercentage(totalReturn)));
        map.put("Hold Return (%)", DECIMAL_FORMAT.format(calcPercentage(buyAndHold)));
        map.put("Custom vs Hold (%)", DECIMAL_FORMAT.format(calcPercentage(versus)));
        map.put("Winning Ratio (%)", DECIMAL_FORMAT.format(calcPercentage(winningRatio)));

        return map;
    }

    private double calcPercentage(Num num) {
        return (num.doubleValue() - 1) * 100;
    }

    private boolean isGreaterThanZero(String value) {
        return Double.parseDouble(value) > 0;
    }

    private boolean isLessThanZero(String value) {
        return Double.parseDouble(value) < 0;
    }

    private boolean isPercentage(String key) {
        return key.endsWith("(%)");
    }
}
