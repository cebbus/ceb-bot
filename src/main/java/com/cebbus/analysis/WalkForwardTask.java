package com.cebbus.analysis;

import com.cebbus.binance.Speculator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class WalkForwardTask implements Runnable {

    private final Symbol symbol;
    private final int limitValue;
    private final int optimizationValue;
    private final int stepValue;
    private final int trainingValue;
    private final List<String> strategyList;
    private final List<Consumer<Speculator>> optimizeDoneListeners = new ArrayList<>();

    private boolean cancelled;
    private OptimizeTask optimizeTask;

    public WalkForwardTask(
            Symbol symbol,
            int limitValue,
            int optimizationValue,
            int stepValue,
            int trainingValue,
            List<String> strategyList) {
        this.symbol = symbol;
        this.limitValue = limitValue;
        this.optimizationValue = optimizationValue;
        this.stepValue = stepValue;
        this.trainingValue = trainingValue;
        this.strategyList = strategyList;
    }

    @Override
    public void run() {
        Speculator speculator = new Speculator(this.symbol, this.limitValue);
        speculator.loadHistory();

        List<Bar> barList = speculator.convertToBarList();

        int barSize = barList.size();
        int optBarSize = barSize * this.optimizationValue / 100;
        List<Bar> optimizationBarList = barList.subList(0, optBarSize);
        List<Bar> backtestBarList = barList.subList(optBarSize, barSize);

        int step = 0;
        int slice = optBarSize * this.stepValue / 100;
        int split = slice * this.trainingValue / 100;
        int stepSize = slice - split;
        boolean completed = false;
        List<Pair<String, Number[]>> bestStrategyList = new ArrayList<>();

        while (!completed && !this.cancelled) {
            int barStart = step;
            int remainBarSize = optBarSize - (step + slice);
            int barEnd = remainBarSize < stepSize ? optBarSize : (step + slice);

            List<Bar> stepBarList = optimizationBarList.subList(barStart, barEnd);
            List<Bar> trainBarList = stepBarList.subList(0, split);
            List<Bar> testBarList = stepBarList.subList(split, stepBarList.size());

            Pair<String, Number[]> bestStrategy = chooseBestOnStep(trainBarList, testBarList);
            bestStrategyList.add(bestStrategy);

            if (barEnd == optBarSize) {
                completed = true;
            } else {
                step += stepSize;
            }
        }

        for (Pair<String, Number[]> strategyParameterPair : bestStrategyList) {
            //TODO
        }
    }

    private Pair<String, Number[]> chooseBestOnStep(List<Bar> trainBarList, List<Bar> testBarList) {
        String bestResultStrategy = null;
        Number[] bestResultParameters = null;
        Num bestResult = DecimalNum.valueOf(0.0);

        Speculator optimizeSpec = new Speculator(this.symbol, trainBarList.size());
        BarSeries trainSeries = new BaseBarSeries(trainBarList);
        BarSeries testSeries = new BaseBarSeries(testBarList);

        for (String strategy : this.strategyList) {
            TheOracle trainOracle = new TheOracle(trainSeries, strategy);
            optimizeSpec.setTheOracle(trainOracle);

            this.optimizeTask = new OptimizeTask(optimizeSpec);
            this.optimizeTask.optimize();

            Num trainResult = trainOracle.calculateProfit();
            log.info("Step strategy: {} Train result: {}", strategy, trainResult);

            Number[] parameters = new Number[]{1, 2}; //TODO get parameters from optimized oracle

            TheOracle testOracle = new TheOracle(testSeries, strategy);
            testOracle.changeProphesyParameters(parameters);
            Num testResult = testOracle.calculateProfit();

            log.info("Step strategy: {} Test result: {}", strategy, testResult);

            if (testResult.isGreaterThan(bestResult)) {
                bestResult = testResult;
                bestResultStrategy = strategy;
                bestResultParameters = parameters;
            }
        }

        log.info("Best step strategy: {} Best result: {}", bestResultStrategy, bestResult);

        return Pair.of(bestResultStrategy, bestResultParameters);
    }

    public void addOnDoneListener(List<Consumer<Speculator>> operations) {
        this.optimizeDoneListeners.addAll(operations);
    }

    public void addOnDoneListener(Consumer<Speculator> operation) {
        this.optimizeDoneListeners.add(operation);
    }

    public void cancel() {
        this.cancelled = true;

        if (this.optimizeTask != null) {
            this.optimizeTask.cancel();
        }
    }
}
