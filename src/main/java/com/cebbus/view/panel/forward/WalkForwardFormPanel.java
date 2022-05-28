package com.cebbus.view.panel.forward;

import com.cebbus.analysis.Symbol;
import com.cebbus.analysis.WalkForwardTask;
import com.cebbus.analysis.WalkForwardTask.StepResult;
import com.cebbus.binance.Speculator;
import com.cebbus.dto.CsIntervalAdapter;
import com.cebbus.util.ReflectionUtil;
import com.cebbus.view.panel.FormFieldSet;
import com.cebbus.view.panel.WaitDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cebbus.view.panel.ConstantDataFactory.getIntervals;
import static com.cebbus.view.panel.ConstantDataFactory.getSymbols;
import static com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel.WEST_ITEM_WIDTH;

public class WalkForwardFormPanel extends FormFieldSet {

    private final Box panel;
    private final List<Consumer<StepResult>> stepDoneListeners = new ArrayList<>();
    private final List<Consumer<Speculator>> onRunClickListeners = new ArrayList<>();

    public WalkForwardFormPanel() {
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

        JLabel intervalLabel = createThinLabel("Interval: ");
        JComboBox<String> intervalBox = new JComboBox<>(getIntervals());
        intervalBox.setSelectedItem("DAILY");

        JLabel limitLabel = createThinLabel("Limit (1000): ");
        JSlider limitSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 1000, 1000);
        limitSlider.addChangeListener(e -> limitLabel.setText(String.format("Limit (%s):", limitSlider.getValue())));

        JLabel optimizationPartLabel = createThinLabel("Optim (60%):");
        JSlider optimizationSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 90, 60);
        optimizationSlider.addChangeListener(e -> optimizationPartLabel.setText(String.format("Optim (%s%%):", optimizationSlider.getValue())));

        JLabel stepLabel = createThinLabel("Step (25%): ");
        JSlider stepSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 100, 25);
        stepSlider.addChangeListener(e -> stepLabel.setText(String.format("Step (%s%%):", stepSlider.getValue())));

        JLabel trainingPartLabel = createThinLabel("Train (80%):");
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
            CsIntervalAdapter csInterval = CsIntervalAdapter.valueOf(interval);
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
            task.addOnStepDoneListener(this.stepDoneListeners);

            Thread thread = new Thread(task);
            thread.start();
            waitDialog.show();
        });

        this.panel.add(inputFormBox);
        addToForm(this.panel, baseLabel, baseBox, WEST_ITEM_WIDTH);
        addToForm(this.panel, quoteLabel, quoteBox, WEST_ITEM_WIDTH);
        addToForm(this.panel, intervalLabel, intervalBox, WEST_ITEM_WIDTH);
        addToForm(this.panel, limitLabel, limitSlider, WEST_ITEM_WIDTH);
        addToForm(this.panel, optimizationPartLabel, optimizationSlider, WEST_ITEM_WIDTH);
        addToForm(this.panel, stepLabel, stepSlider, WEST_ITEM_WIDTH);
        addToForm(this.panel, trainingPartLabel, trainingSlider, WEST_ITEM_WIDTH);
        this.panel.add(strategiesPane);
        this.panel.add(Box.createVerticalStrut(2));
        this.panel.add(startButtonBox);
    }

    public void addRunClickListeners(Consumer<Speculator> operation) {
        this.onRunClickListeners.add(operation);
    }

    public void addOnStepDoneListener(Consumer<StepResult> operation) {
        this.stepDoneListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }

}
