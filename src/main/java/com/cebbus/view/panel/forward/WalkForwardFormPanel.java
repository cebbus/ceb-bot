package com.cebbus.view.panel.forward;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.TheOracle;
import com.cebbus.analysis.WalkForwardTask;
import com.cebbus.binance.Speculator;
import com.cebbus.util.ReflectionUtil;
import com.cebbus.view.panel.BoxTitlePanel;
import com.cebbus.view.panel.WaitDialog;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cebbus.view.panel.ConstantDataFactory.getIntervals;
import static com.cebbus.view.panel.ConstantDataFactory.getSymbols;
import static com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel.WEST_ITEM_WIDTH;

public class WalkForwardFormPanel {

    private final Box panel;
    private final BoxTitlePanel title;
    private final List<Consumer<Speculator>> onRunClickListeners = new ArrayList<>();

    public WalkForwardFormPanel() {
        this.panel = Box.createVerticalBox();
        this.panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 10, 8));

        this.title = new BoxTitlePanel("Walk Forward Form");

        createForm();
    }

    private void createForm() {
        JLabel baseLabel = new JLabel("Base: ");
        JComboBox<String> baseBox = new JComboBox<>(getSymbols());
        baseBox.setSelectedItem("BTC");

        JLabel quoteLabel = new JLabel("Quote: ");
        JComboBox<String> quoteBox = new JComboBox<>(getSymbols());
        quoteBox.setSelectedItem("USDT");

        JLabel intervalLabel = new JLabel("Interval: ");
        JComboBox<String> intervalBox = new JComboBox<>(getIntervals());
        intervalBox.setSelectedItem("DAILY");

        JLabel limitLabel = new JLabel("Limit (1000): ");
        JSlider limitSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 1000, 1000);
        limitSlider.addChangeListener(e -> limitLabel.setText(String.format("Limit (%s):", limitSlider.getValue())));

        JLabel optimizationPartLabel = new JLabel("Optim (60%):");
        JSlider optimizationSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 90, 60);
        optimizationSlider.addChangeListener(e -> optimizationPartLabel.setText(String.format("Optim (%s%%):", optimizationSlider.getValue())));

        JLabel stepLabel = new JLabel("Step (25%): ");
        JSlider stepSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 100, 25);
        stepSlider.addChangeListener(e -> stepLabel.setText(String.format("Step (%s%%):", stepSlider.getValue())));

        JLabel trainingPartLabel = new JLabel("Train (80%):");
        JSlider trainingSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 90, 80);
        trainingSlider.addChangeListener(e -> trainingPartLabel.setText(String.format("Train (%s%%):", trainingSlider.getValue())));


        Object[] strategies = ReflectionUtil.listStrategyClasses().stream()
                .map(Class::getSimpleName)
                .toArray();

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Strategy", strategies);

        JTable strategiesTable = new JTable(tableModel);
        strategiesTable.setFillsViewportHeight(true);
        strategiesTable.setRowSelectionInterval(0, 0);

        JScrollPane strategiesPane = new JScrollPane(strategiesTable);
        setSize(strategiesPane, WEST_ITEM_WIDTH, 150);

        JButton startButton = new JButton("Run");
        setSize(startButton, WEST_ITEM_WIDTH, 20);

        Box startButtonBox = Box.createHorizontalBox();
        setSize(startButtonBox, WEST_ITEM_WIDTH, 20);
        startButtonBox.add(startButton);

        startButton.addActionListener(e -> {
            String baseVal = baseBox.getItemAt(baseBox.getSelectedIndex());
            String quoteVal = quoteBox.getItemAt(quoteBox.getSelectedIndex());
            String interval = intervalBox.getItemAt(intervalBox.getSelectedIndex());
            CandlestickInterval csInterval = CandlestickInterval.valueOf(interval);
            Symbol symbol = new Symbol(-1, 0, baseVal, quoteVal, null, csInterval, null);

            int limitValue = limitSlider.getValue();
            int optimizationValue = optimizationSlider.getValue();
            int stepValue = stepSlider.getValue();
            int trainingValue = trainingSlider.getValue();

            int[] selectedIndices = strategiesTable.getSelectedRows();
            List<String> strategyList = Arrays.stream(selectedIndices)
                    .mapToObj(value -> tableModel.getValueAt(value, 0).toString())
                    .collect(Collectors.toList());

            WalkForwardTask task = new WalkForwardTask(symbol, limitValue,
                    optimizationValue, stepValue, trainingValue, strategyList);

            WaitDialog waitDialog = new WaitDialog(el -> task.cancel());

            task.addOnDoneListener(this.onRunClickListeners);
            task.addOnDoneListener(s -> waitDialog.hide());

            Thread thread = new Thread(task);
            thread.start();
            waitDialog.show();
        });

        addToForm(baseLabel, baseBox);
        addToForm(quoteLabel, quoteBox);
        addToForm(intervalLabel, intervalBox);
        addToForm(limitLabel, limitSlider);
        addToForm(optimizationPartLabel, optimizationSlider);
        addToForm(stepLabel, stepSlider);
        addToForm(trainingPartLabel, trainingSlider);
        this.panel.add(strategiesPane);
        this.panel.add(Box.createVerticalStrut(2));
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
        TheOracle theOracle = new TheOracle(series, "JunkStrategy");
        speculator.setTheOracle(theOracle);

        return speculator;
    }

    public void addRunClickListeners(Consumer<Speculator> operation) {
        this.onRunClickListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }

    public JPanel getTitlePanel() {
        return title.getPanel();
    }

}
