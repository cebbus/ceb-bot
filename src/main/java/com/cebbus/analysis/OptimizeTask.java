package com.cebbus.analysis;

import com.cebbus.binance.Speculator;
import com.cebbus.exception.OptimizationException;
import lombok.extern.slf4j.Slf4j;
import org.jgap.*;
import org.jgap.impl.DefaultConfiguration;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class OptimizeTask implements Runnable {

    private final Speculator speculator;
    private final List<Consumer<Speculator>> optimizeDoneListeners = new ArrayList<>();

    private boolean cancelled;

    public OptimizeTask(Speculator speculator) {
        this.speculator = speculator;
    }

    @Override
    public void run() {
        optimize();
    }

    public void optimize() {
        TheOracle theOracle = this.speculator.getTheOracle();

        Configuration.reset();
        Configuration conf = new DefaultConfiguration();

        try {
            conf.setPopulationSize(128);
            conf.setFitnessFunction(createFitnessFunc());
            conf.setSampleChromosome(theOracle.getProphesyOmen(conf));

            Genotype genotype = Genotype.randomInitialGenotype(conf);
            for (int i = 0; i < 1024; i++) {
                if (this.cancelled) {
                    return;
                }

                genotype.evolve();
            }

            IChromosome best = genotype.getFittestChromosome();
            this.speculator.changeParameters(chromosomeToParameters(best));

            this.optimizeDoneListeners.forEach(c -> c.accept(this.speculator));
        } catch (InvalidConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new OptimizationException();
        }
    }

    public void addOnDoneListener(List<Consumer<Speculator>> operations) {
        this.optimizeDoneListeners.addAll(operations);
    }

    public void addOnDoneListener(Consumer<Speculator> operation) {
        this.optimizeDoneListeners.add(operation);
    }

    public void cancel() {
        this.cancelled = true;
    }

    private FitnessFunction createFitnessFunc() {
        return new FitnessFunction() {
            @Override
            protected double evaluate(IChromosome chromosome) {
                TheOracle theOracle = speculator.getTheOracle();

                Number[] parameters = chromosomeToParameters(chromosome);
                theOracle.changeProphesyParameters(parameters);

                Num result = theOracle.backtestStrategyReturn();
                chromosome.setFitnessValue(result.doubleValue());

                return result.doubleValue();
            }
        };
    }

    private Number[] chromosomeToParameters(IChromosome chromosome) {
        Number[] parameters = new Number[chromosome.size()];
        for (int i = 0; i < chromosome.size(); i++) {
            parameters[i] = (Number) chromosome.getGene(i).getAllele();
        }

        return parameters;
    }
}
