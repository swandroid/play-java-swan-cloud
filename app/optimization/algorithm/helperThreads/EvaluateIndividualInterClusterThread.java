package optimization.algorithm.helperThreads;

import optimization.core.fitness.Fitness;
import optimization.core.fitness.FitnessMap;
import optimization.core.population.Individual;

import java.util.HashMap;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class EvaluateIndividualInterClusterThread extends Fitness implements Runnable {

    Individual individual;
    private HashMap<String,Double> sensorValues;
    private HashMap<String,Double> maxSensorValues;
    private HashMap<String,Boolean> increaseObjectives;
    private int numberOfObjectives;
    private HashMap<String,Double> minSensorValues;
    
    public EvaluateIndividualInterClusterThread(Individual individual, HashMap<String,Double> map, HashMap<String,Boolean> increaseObjectives,
                                                HashMap<String,Double> maxSensorValues, HashMap<String,Double> minSensorValues) {
        this.individual = individual;
        this.sensorValues = map;
        this.maxSensorValues = maxSensorValues;
        this.increaseObjectives = increaseObjectives;
        this.numberOfObjectives = increaseObjectives.size();
        this.minSensorValues = minSensorValues;
    }


    @Override
    public void run() {
        FitnessMap fitnessMap;
        fitnessMap = fitnessMap(individual.getEdgePath(),sensorValues,increaseObjectives);
        fitnessMap.setTotalFitness(fitness(fitnessMap,individual.getEdgePath().size(),maxSensorValues,minSensorValues
                ,increaseObjectives));
        individual.setFitnessMap(fitnessMap);
        individual.setFitness(fitnessMap.getTotalFitness());
    }
}
