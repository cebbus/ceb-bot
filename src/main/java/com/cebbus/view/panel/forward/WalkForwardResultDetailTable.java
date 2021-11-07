package com.cebbus.view.panel.forward;

import com.cebbus.analysis.TheOracle;
import com.cebbus.binance.Speculator;
import com.cebbus.view.panel.FormFieldSet;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.BuyAndHoldReturnCriterion;
import org.ta4j.core.analysis.criteria.NumberOfBarsCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.analysis.criteria.WinningPositionsRatioCriterion;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import static com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel.RESULT_FORMAT;
import static com.cebbus.view.panel.forward.CryptoWalkForwardTabPanel.WEST_ITEM_WIDTH;

public class WalkForwardResultDetailTable extends FormFieldSet {

    private final Box panel;
    private final JTable table;

    public WalkForwardResultDetailTable() {
        this.panel = Box.createVerticalBox();
        this.table = createTable();
    }

    private JTable createTable() {
        Box resultDetailLabel = createTitleLabelBox("Result Details", WEST_ITEM_WIDTH, 20);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Metric");
        tableModel.addColumn("Value");

        JTable jTable = new JTable(tableModel);
        jTable.setFillsViewportHeight(true);

        TableColumnModel columnModel = jTable.getColumnModel();
        setColumnSize(columnModel.getColumn(1), 75);

        JScrollPane scrollPane = new JScrollPane(jTable);
        setSize(scrollPane, WEST_ITEM_WIDTH, 150);

        this.panel.add(resultDetailLabel);
        this.panel.add(scrollPane);

        return jTable;
    }

    public void reload(Speculator speculator) {
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

    public Box getPanel() {
        return panel;
    }
}
