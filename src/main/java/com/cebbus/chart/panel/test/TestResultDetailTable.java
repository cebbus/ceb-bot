package com.cebbus.chart.panel.test;

import com.cebbus.analysis.OptimizeTask;
import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.chart.panel.WaitDialog;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.cebbus.chart.panel.test.CryptoTestTabPanel.RESULT_FORMAT;
import static com.cebbus.chart.panel.test.CryptoTestTabPanel.WEST_ITEM_WIDTH;

public class TestResultDetailTable {

    private final Box panel;
    private final JTable table;
    private final JButton optBtn;
    private final TestTitlePanel title;
    private final List<Consumer<Speculator>> onOptimizeClickListeners = new ArrayList<>();

    private Speculator speculator;

    public TestResultDetailTable() {
        this.panel = Box.createVerticalBox();
        this.panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 10, 8));

        this.table = createTable();
        this.optBtn = createOptimizeButton();
        this.title = new TestTitlePanel("Result Details", false);
    }

    private JTable createTable() {
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Metric");
        tableModel.addColumn("Value");

        JTable jTable = new JTable(tableModel);
        jTable.setFillsViewportHeight(true);

        TableColumn valueColumn = jTable.getColumnModel().getColumn(1);
        valueColumn.setWidth(10);
        valueColumn.setMinWidth(10);
        valueColumn.setPreferredWidth(10);

        JScrollPane scrollPane = new JScrollPane(jTable);
        scrollPane.setMinimumSize(new Dimension(WEST_ITEM_WIDTH, 150));
        scrollPane.setMaximumSize(new Dimension(WEST_ITEM_WIDTH, 150));
        scrollPane.setPreferredSize(new Dimension(WEST_ITEM_WIDTH, 150));

        this.panel.add(scrollPane);

        return jTable;
    }

    private JButton createOptimizeButton() {
        JButton jButton = new JButton("Optimize");
        setSize(jButton, WEST_ITEM_WIDTH, 20);
        jButton.setEnabled(false);

        jButton.addActionListener(e -> {
            OptimizeTask task = new OptimizeTask(this.speculator);

            WaitDialog waitDialog = new WaitDialog(el -> task.cancel());

            task.addOnDoneListener(this.onOptimizeClickListeners);
            task.addOnDoneListener(s -> waitDialog.hide());

            Thread thread = new Thread(task);
            thread.start();
            waitDialog.show();
        });

        Box showButtonBox = Box.createHorizontalBox();
        setSize(showButtonBox, WEST_ITEM_WIDTH, 20);
        showButtonBox.add(jButton);

        this.panel.add(Box.createVerticalStrut(2));
        this.panel.add(showButtonBox);

        return jButton;
    }

    private void setSize(JComponent component, int width, int height) {
        component.setMinimumSize(new Dimension(width, height));
        component.setMaximumSize(new Dimension(width, height));
        component.setPreferredSize(new Dimension(width, height));
    }

    public void reload(Speculator speculator) {
        this.speculator = speculator;

        this.optBtn.setEnabled(true);

        DefaultTableModel model = (DefaultTableModel) this.table.getModel();

        model.setRowCount(0);

        TheOracle theOracle = speculator.getTheOracle();
        BarSeries series = theOracle.getSeries();
        TradingRecord tradingRecord = theOracle.getBacktestRecord();

        model.addRow(new Object[]{"Number of Pos", tradingRecord.getPositionCount()});

        NumberOfBarsCriterion numberOfBarsCriterion = new NumberOfBarsCriterion();
        int numOfBars = numberOfBarsCriterion.calculate(series, tradingRecord).intValue();
        model.addRow(new Object[]{"Number of Bars", numOfBars});

        GrossReturnCriterion returnCriterion = new GrossReturnCriterion();
        double totalReturn = returnCriterion.calculate(series, tradingRecord).doubleValue();
        model.addRow(new Object[]{"Strategy Return", RESULT_FORMAT.format(totalReturn)});

        BuyAndHoldReturnCriterion buyAndHoldReturnCriterion = new BuyAndHoldReturnCriterion();
        double buyAndHold = buyAndHoldReturnCriterion.calculate(series, tradingRecord).doubleValue();
        model.addRow(new Object[]{"Buy and Hold Return", RESULT_FORMAT.format(buyAndHold)});

        VersusBuyAndHoldCriterion versusBuyAndHoldCriterion = new VersusBuyAndHoldCriterion(returnCriterion);
        double versus = versusBuyAndHoldCriterion.calculate(series, tradingRecord).doubleValue();
        model.addRow(new Object[]{"Strategy vs Hold (%)", RESULT_FORMAT.format(versus * 100)});

        WinningPositionsRatioCriterion winningRatioCriterion = new WinningPositionsRatioCriterion();
        double winningRatio = winningRatioCriterion.calculate(series, tradingRecord).doubleValue();
        model.addRow(new Object[]{"Strategy Winning Ratio (%)", RESULT_FORMAT.format(winningRatio * 100)});
    }

    public void addOptimizeClickListener(Consumer<Speculator> operation) {
        this.onOptimizeClickListeners.add(operation);
    }

    public Box getPanel() {
        return panel;
    }

    public TestTitlePanel getTitle() {
        return title;
    }
}
