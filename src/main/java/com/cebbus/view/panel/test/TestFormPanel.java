package com.cebbus.view.panel.test;

import com.cebbus.properties.Symbol;
import com.cebbus.binance.Speculator;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.view.panel.FormFieldSet;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.cebbus.view.panel.ConstantDataFactory.*;
import static com.cebbus.view.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;

public class TestFormPanel extends FormFieldSet {

    private final Box panel;
    private final List<Consumer<Speculator>> onRunClickListeners = new ArrayList<>();

    public TestFormPanel() {
        this.panel = Box.createVerticalBox();
        createForm();
    }

    private void createForm() {
        Box inputFormBox = createTitleLabelBox("Input Form", WEST_ITEM_WIDTH, 20);

        JLabel baseLabel = createThinLabel("Base: ");
        JComboBox<String> baseBox = new JComboBox<>(getSymbols());
        baseBox.setSelectedItem("BTC");

        JLabel quoteLabel = createThinLabel("Quote: ");
        JComboBox<String> quoteBox = new JComboBox<>(getSymbols());
        quoteBox.setSelectedItem("USDT");

        JLabel limitLabel = createThinLabel("Bar Size: ");
        JComboBox<Integer> limitBox = new JComboBox<>(getSizes());
        limitBox.setSelectedItem(512);

        JLabel intervalLabel = createThinLabel("Interval: ");
        JComboBox<String> intervalBox = new JComboBox<>(getIntervals());
        intervalBox.setSelectedItem("DAILY");

        JButton startButton = new JButton("Run");
        setSize(startButton, WEST_ITEM_WIDTH, 20);

        Box startButtonBox = Box.createHorizontalBox();
        setSize(startButtonBox, WEST_ITEM_WIDTH, 20);
        startButtonBox.add(startButton);

        startButton.addActionListener(e -> {
            String baseVal = baseBox.getItemAt(baseBox.getSelectedIndex());
            String quoteVal = quoteBox.getItemAt(quoteBox.getSelectedIndex());
            Integer limit = limitBox.getItemAt(limitBox.getSelectedIndex());

            String interval = intervalBox.getItemAt(intervalBox.getSelectedIndex());
            CsIntervalAdapter csInterval = CsIntervalAdapter.valueOf(interval);

            Symbol symbol = new Symbol(-1, 0, baseVal, quoteVal, "JunkStrategy", csInterval, null);
            Speculator speculator = new Speculator(symbol, limit, true);

            this.onRunClickListeners.forEach(c -> c.accept(speculator));
        });

        this.panel.add(inputFormBox);
        addToForm(this.panel, baseLabel, baseBox, WEST_ITEM_WIDTH);
        addToForm(this.panel, quoteLabel, quoteBox, WEST_ITEM_WIDTH);
        addToForm(this.panel, limitLabel, limitBox, WEST_ITEM_WIDTH);
        addToForm(this.panel, intervalLabel, intervalBox, WEST_ITEM_WIDTH);
        this.panel.add(startButtonBox);
    }

    public void addRunClickListeners(Consumer<Speculator> operation) {
        this.onRunClickListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }
}
