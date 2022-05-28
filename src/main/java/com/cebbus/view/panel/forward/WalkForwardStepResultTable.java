package com.cebbus.view.panel.forward;

import com.cebbus.analysis.WalkForwardTask.StepResult;
import com.cebbus.binance.Speculator;
import org.ta4j.core.Bar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class WalkForwardStepResultTable {

    private static final DecimalFormat RESULT_FORMAT = new DecimalFormat("#,###.0000");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final JTable table;
    private boolean completed;

    public WalkForwardStepResultTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Strategy");
        model.addColumn("Optimize Start");
        model.addColumn("Optimize End");
        model.addColumn("Optimize Before Result");
        model.addColumn("Optimize After Result");
        model.addColumn("Optimize HODL");
        model.addColumn("Test Start");
        model.addColumn("Test End");
        model.addColumn("Test Default Result");
        model.addColumn("Test Optimize Result");
        model.addColumn("Test HODL");
        model.addColumn("Parameters");

        this.table = new JTable(model);
        this.table.setFillsViewportHeight(true);
    }

    public void complete(Speculator speculator) {
        this.completed = true;
    }

    public void addResult(StepResult result) {
        if (this.completed) {
            clear();
        }

        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.addRow(resultToRow(result));
    }

    private Object[] resultToRow(StepResult result) {
        Object[] row = new Object[12];
        row[0] = result.getStrategy();
        row[1] = barToTime(result.getTrainStartBar());
        row[2] = barToTime(result.getTrainEndBar());
        row[3] = numToFormattedResult(result.getTrainDefaultResult());
        row[4] = numToFormattedResult(result.getTrainResult());
        row[5] = numToFormattedResult(result.getTrainBuyAndHoldResult());
        row[6] = barToTime(result.getTestStartBar());
        row[7] = barToTime(result.getTestEndBar());
        row[8] = numToFormattedResult(result.getTestDefaultResult());
        row[9] = numToFormattedResult(result.getTestResult());
        row[10] = numToFormattedResult(result.getTestBuyAndHoldResult());
        row[11] = Arrays.toString(result.getParameters());

        return row;
    }

    private String barToTime(Bar bar) {
        return bar == null ? null : bar.getEndTime().toLocalDateTime().format(TIME_FORMATTER);
    }

    private String numToFormattedResult(Number num) {
        return num == null ? null : RESULT_FORMAT.format(num);
    }

    private void clear() {
        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setRowCount(0);

        this.completed = false;
    }

    public JTable getTable() {
        return table;
    }
}
