package optimization.core;

import optimization.core.fitness.FitnessInterface;
import optimization.core.population.Individual;
import optimization.core.population.Population;

import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public interface GeneticAlgorithm extends FitnessInterface {

    Population createInitialPopulation() throws InterruptedException;

    List<Individual> crossover(Individual individual, Individual individual1);

    Individual mutation(Individual individual);

    void evaluate(Population population);



}
