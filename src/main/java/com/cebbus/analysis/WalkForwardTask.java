package com.cebbus.analysis;

import com.cebbus.analysis.mapper.BarMapper;
import com.cebbus.analysis.strategy.CebStrategy;
import com.cebbus.analysis.strategy.StrategyFactory;
import com.cebbus.binance.Speculator;
import com.cebbus.dto.CandleDto;
import com.cebbus.dto.CsIntervalAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

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
    private final List<Consumer<StepResult>> stepDoneListeners = new ArrayList<>();
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
        Speculator loader = new Speculator(this.symbol, this.limitValue);
        List<CandleDto> stickList = loader.loadBarHistory();

        CsIntervalAdapter interval = symbol.getInterval();
        List<Bar> barList = BarMapper.dtoToBar(stickList, interval);

        int barSize = barList.size();
        int optBarSize = barSize * this.optimizationValue / 100;
        List<Bar> optimizationBarList = barList.subList(0, optBarSize);
        List<Bar> backtestBarList = barList.subList(optBarSize, barSize);

        log.info("optimization part: {}, backtest part: {}",
                optimizationBarList.size(), backtestBarList.size());

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

            log.info("bar end: {}, remain: {}, step size: {}, train size: {}, test size: {}", barEnd,
                    remainBarSize, stepBarList.size(), trainBarList.size(), testBarList.size());

            Pair<String, Number[]> bestStrategy = chooseBestOnStep(trainBarList, testBarList);
            bestStrategyList.add(bestStrategy);

            if (barEnd == optBarSize) {
                completed = true;
            } else {
                step += stepSize;
            }
        }

        if (!this.cancelled) {
            Speculator speculator = chooseBest(backtestBarList, bestStrategyList);
            this.optimizeDoneListeners.forEach(l -> l.accept(speculator));
        }
    }

    private Speculator chooseBest(List<Bar> backtestBarList, List<Pair<String, Number[]>> bestStrategyList) {
        Number bestResult = 0d;

        TheOracle theOracle = null;
        BarSeries backtestSeries = new BaseBarSeries(backtestBarList);

        for (Pair<String, Number[]> strategyParameterPair : bestStrategyList) {
            String strategy = strategyParameterPair.getKey();
            Number[] parameters = strategyParameterPair.getValue();

            CebStrategy cebStrategy = StrategyFactory.create(backtestSeries, strategy);
            TheOracle backtestOracle = new TheOracle(this.symbol, cebStrategy);
            Number defaultResult = backtestOracle.backtestStrategyReturn();
            Number buyAndHoldResult = backtestOracle.backtestBuyAndHold();

            backtestOracle.changeProphesyParameters(parameters);
            Number result = backtestOracle.backtestStrategyReturn();

            StepResult stepResult = new StepResult();
            stepResult.setStrategy(strategy);
            stepResult.setTestStartBar(backtestSeries.getFirstBar());
            stepResult.setTestEndBar(backtestSeries.getLastBar());
            stepResult.setTestDefaultResult(defaultResult);
            stepResult.setTestResult(result);
            stepResult.setTestBuyAndHoldResult(buyAndHoldResult);
            stepResult.setParameters(parameters);
            this.stepDoneListeners.forEach(l -> l.accept(stepResult));

            if (result.doubleValue() > bestResult.doubleValue()) {
                bestResult = result;
                theOracle = backtestOracle;
            }
        }

        Speculator speculator = new Speculator(this.symbol, backtestBarList.size());
        speculator.setTheOracle(theOracle);

        return speculator;
    }

    private Pair<String, Number[]> chooseBestOnStep(List<Bar> trainBarList, List<Bar> testBarList) {
        String bestStrategy = null;
        Number[] bestParameters = null;
        Number bestResult = 0d;

        BarSeries trainSeries = new BaseBarSeries(trainBarList);
        BarSeries testSeries = new BaseBarSeries(testBarList);

        for (String strategy : this.strategyList) {
            CebStrategy trainsStrategy = StrategyFactory.create(trainSeries, strategy);
            TheOracle trainOracle = new TheOracle(this.symbol, trainsStrategy);
            Number trainBuyAndHold = trainOracle.backtestBuyAndHold();
            Number trainDefaultResult = trainOracle.backtestStrategyReturn();

            optimize(trainOracle);
            Number trainResult = trainOracle.backtestStrategyReturn();

            CebStrategy testStrategy = StrategyFactory.create(testSeries, strategy);
            TheOracle testOracle = new TheOracle(this.symbol, testStrategy);
            Number[] testDefaultParameters = testOracle.getProphesyParameters();
            Number testBuyAndHold = testOracle.backtestBuyAndHold();
            Number testDefaultResult = testOracle.backtestStrategyReturn();

            Number[] testParameters = trainOracle.getProphesyParameters();
            testOracle.changeProphesyParameters(testParameters);
            Number testResult = testOracle.backtestStrategyReturn();

            Number result = Math.max(testResult.doubleValue(), testDefaultResult.doubleValue());
            Number[] parameters = result.equals(testResult) ? testParameters : testDefaultParameters;

            if (result.doubleValue() > bestResult.doubleValue()) {
                bestResult = result;
                bestStrategy = strategy;
                bestParameters = parameters;
            }

            StepResult stepResult = new StepResult();
            stepResult.setStrategy(strategy);
            stepResult.setTrainStartBar(trainSeries.getFirstBar());
            stepResult.setTrainEndBar(trainSeries.getLastBar());
            stepResult.setTestStartBar(testSeries.getFirstBar());
            stepResult.setTestEndBar(testSeries.getLastBar());
            stepResult.setTrainDefaultResult(trainDefaultResult);
            stepResult.setTrainResult(trainResult);
            stepResult.setTrainBuyAndHoldResult(trainBuyAndHold);
            stepResult.setTestDefaultResult(testDefaultResult);
            stepResult.setTestResult(testResult);
            stepResult.setTestBuyAndHoldResult(testBuyAndHold);
            stepResult.setParameters(parameters);
            this.stepDoneListeners.forEach(l -> l.accept(stepResult));

            if (this.cancelled) {
                return Pair.of("", new Number[0]);
            }
        }

        log.info("best step strategy: {} best result: {}", bestStrategy, bestResult);

        return Pair.of(bestStrategy, bestParameters);
    }

    private void optimize(TheOracle trainOracle) {
        Speculator spec = new Speculator(this.symbol);
        spec.setTheOracle(trainOracle);

        this.optimizeTask = new OptimizeTask(spec);
        this.optimizeTask.optimize();
    }

    public void addOnDoneListener(List<Consumer<Speculator>> operations) {
        this.optimizeDoneListeners.addAll(operations);
    }

    public void addOnDoneListener(Consumer<Speculator> operation) {
        this.optimizeDoneListeners.add(operation);
    }

    public void addOnStepDoneListener(List<Consumer<StepResult>> operation) {
        this.stepDoneListeners.addAll(operation);
    }

    public void cancel() {
        this.cancelled = true;

        if (this.optimizeTask != null) {
            this.optimizeTask.cancel();
        }
    }

    @Data
    public static final class StepResult {
        private String strategy;
        private Bar trainStartBar;
        private Bar trainEndBar;
        private Bar testStartBar;
        private Bar testEndBar;
        private Number trainDefaultResult;
        private Number trainResult;
        private Number trainBuyAndHoldResult;
        private Number testDefaultResult;
        private Number testResult;
        private Number testBuyAndHoldResult;
        private Number[] parameters;
    }
}
