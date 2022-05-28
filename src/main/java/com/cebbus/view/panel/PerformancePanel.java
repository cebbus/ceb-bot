package com.cebbus.view.panel;

import com.cebbus.dto.CriterionResultDto;
import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PerformancePanel extends FormFieldSet {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.0000");

    private final Symbol symbol;
    private final TheOracle theOracle;

    private final Map<String, JLabel> infoLabelMap = new LinkedHashMap<>();
    private final Map<String, JLabel> tradingLabelMap = new LinkedHashMap<>();
    private final Map<String, JLabel> backtestLabelMap = new LinkedHashMap<>();

    public PerformancePanel(Speculator speculator) {
        this.symbol = speculator.getSymbol();
        this.theOracle = speculator.getTheOracle();
    }

    public JPanel create() {
        AtomicInteger rowNum = new AtomicInteger(0);

        JPanel panel = new JPanel(new GridBagLayout());

        addInformationPart(panel, rowNum);
        addEmptyRow(panel, rowNum);
        addBacktestPart(panel, rowNum);
        addEmptyRow(panel, rowNum);
        addTradingPart(panel, rowNum);

        return panel;
    }

    public void refresh() {
        List<CriterionResultDto> backtestList = this.theOracle.getCriterionResultList(true);
        backtestList.forEach(r -> updateValueLabel(this.backtestLabelMap.get(r.getLabel()), r));

        List<CriterionResultDto> resultList = this.theOracle.getCriterionResultList(false);
        resultList.forEach(r -> updateValueLabel(this.tradingLabelMap.get(r.getLabel()), r));
    }

    private void addEmptyRow(JPanel panel, AtomicInteger rowNum) {
        panel.add(new JLabel(" "), createConst(rowNum, 0));
        panel.add(new JLabel(" "), createConst(rowNum, 1));
    }

    private void addInformationPart(JPanel panel, AtomicInteger rowNum) {
        panel.add(createTitleLabel("Symbol Informations"), createConst(rowNum, 0));
        panel.add(new JLabel(""), createConst(rowNum, 1));

        String name = capitalizeWord(this.symbol.getBase()) + "/" + capitalizeWord(this.symbol.getQuote());

        this.infoLabelMap.put("Symbol", createThinLabel(name));
        this.infoLabelMap.put("Strategy", createThinLabel(this.symbol.getStrategy().replace("Strategy", "")));
        this.infoLabelMap.put("Interval", createThinLabel(snakeCaseToCapitalWord(this.symbol.getInterval().name())));
        this.infoLabelMap.put("Weight (%)", createThinLabel(DECIMAL_FORMAT.format(this.symbol.getWeight() * 100)));

        this.infoLabelMap.forEach((s, l) -> {
            panel.add(createThinLabel(s), createConst(rowNum, 0));
            panel.add(l, createConst(rowNum, 1));
        });
    }

    private void addBacktestPart(JPanel panel, AtomicInteger rowNum) {
        panel.add(createTitleLabel("Backtest Results"), createConst(rowNum, 0));
        panel.add(new JLabel(""), createConst(rowNum, 1));

        List<CriterionResultDto> backtestResultList = this.theOracle.getCriterionResultList(true);
        backtestResultList.forEach(r -> this.backtestLabelMap.put(r.getLabel(), createValueLabel(r)));

        this.backtestLabelMap.forEach((s, l) -> {
            panel.add(createThinLabel(s), createConst(rowNum, 0));
            panel.add(l, createConst(rowNum, 1));
        });
    }

    private void addTradingPart(JPanel panel, AtomicInteger rowNum) {
        panel.add(createTitleLabel("Trade Results"), createConst(rowNum, 0));
        panel.add(new JLabel(""), createConst(rowNum, 1));

        List<CriterionResultDto> currentResultList = this.theOracle.getCriterionResultList(false);
        currentResultList.forEach(r -> this.tradingLabelMap.put(r.getLabel(), createValueLabel(r)));

        this.tradingLabelMap.forEach((s, l) -> {
            panel.add(createThinLabel(s), createConst(rowNum, 0));
            panel.add(l, createConst(rowNum, 1));
        });
    }

    private GridBagConstraints createConst(AtomicInteger rowNum, int columnNum) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columnNum;
        c.gridy = columnNum == 0 ? rowNum.get() : rowNum.getAndIncrement();
        c.anchor = columnNum == 0 ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
        c.weightx = 0.5;

        return c;
    }

    private JLabel createValueLabel(CriterionResultDto result) {
        JLabel label = createThinLabel();
        updateValueLabel(label, result);
        return label;
    }

    private void updateValueLabel(JLabel label, CriterionResultDto result) {
        label.setText(result.getFormattedValue());
        label.setForeground(result.getColor());
    }

    private String snakeCaseToCapitalWord(String value) {
        return Arrays.stream(value.split("_")).map(this::capitalizeWord).collect(Collectors.joining(" "));
    }

    private String capitalizeWord(String value) {
        return StringUtils.capitalize(value.toLowerCase(Locale.ROOT));
    }
}
