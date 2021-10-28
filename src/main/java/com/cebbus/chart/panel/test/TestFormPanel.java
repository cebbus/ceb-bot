package com.cebbus.chart.panel.test;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.cebbus.chart.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;
import static com.cebbus.chart.panel.test.TestTabPanelDataFactory.*;

public class TestFormPanel {

    private final Box panel;
    private final TestTitlePanel title;
    private final List<Consumer<Speculator>> onRunClickListeners = new ArrayList<>();

    public TestFormPanel() {
        this.panel = Box.createVerticalBox();
        this.panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 10, 8));

        this.title = new TestTitlePanel("Test Form");

        createForm();
    }

    private void createForm() {
        JLabel baseLabel = new JLabel("Base: ");
        JComboBox<String> baseBox = new JComboBox<>(getSymbols());
        baseBox.setSelectedItem("BTC");

        JLabel quoteLabel = new JLabel("Quote: ");
        JComboBox<String> quoteBox = new JComboBox<>(getSymbols());
        quoteBox.setSelectedItem("USDT");

        JLabel limitLabel = new JLabel("Bar Size: ");
        JComboBox<Integer> limitBox = new JComboBox<>(getSizes());
        limitBox.setSelectedItem(512);

        JLabel intervalLabel = new JLabel("Interval: ");
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
            CandlestickInterval csInterval = CandlestickInterval.valueOf(interval);

            Symbol symbol = new Symbol(-1, 0, baseVal, quoteVal, null, csInterval, null);
            Speculator speculator = createSpeculator(symbol, limit);

            this.onRunClickListeners.forEach(c -> c.accept(speculator));
        });

        addToForm(baseLabel, baseBox);
        addToForm(quoteLabel, quoteBox);
        addToForm(limitLabel, limitBox);
        addToForm(intervalLabel, intervalBox);
        this.panel.add(startButtonBox);
    }

    private void addToForm(JLabel label, JComponent component) {
        setSize(label, 75, 20);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        setSize(component, WEST_ITEM_WIDTH - 75, 20);
        component.setAlignmentX(Component.RIGHT_ALIGNMENT);

        Box box = Box.createHorizontalBox();
        setSize(box, WEST_ITEM_WIDTH, 20);
        box.add(label);
        box.add(component);

        this.panel.add(box);
        this.panel.add(Box.createVerticalStrut(2));
    }

    private void setSize(JComponent component, int width, int height) {
        component.setMinimumSize(new Dimension(width, height));
        component.setMaximumSize(new Dimension(width, height));
        component.setPreferredSize(new Dimension(width, height));
    }

    private Speculator createSpeculator(Symbol symbol, int limit) {
        Speculator speculator = new Speculator(symbol, limit);
        speculator.loadHistory();

        BarSeries series = new BaseBarSeries(speculator.convertToBarList());
        TheOracle theOracle = new TheOracle(series);
        speculator.setTheOracle(theOracle);

        return speculator;
    }

    public void addRunClickListeners(Consumer<Speculator> operation) {
        this.onRunClickListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }

    public TestTitlePanel getTitle() {
        return title;
    }
}
