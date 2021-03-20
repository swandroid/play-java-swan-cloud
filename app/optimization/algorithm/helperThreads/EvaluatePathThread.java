package optimization.algorithm.helperThreads;

import optimization.core.fitness.Fitness;
import optimization.core.fitness.FitnessMap;
import optimization.core.population.Paths;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class EvaluatePathThread extends Fitness implements Callable<Paths> {

    Paths paths;
    private HashMap<String,Double> sensorValues;
    private HashMap<String,Double> maxSensorValues;
    private HashMap<String,Boolean> increaseObjectives;
    private int numberOfObjectives;
    private HashMap<String,Double> minSensorValues;
    public EvaluatePathThread(Paths paths, HashMap<String,Double> map, HashMap<String,Boolean> increaseObjectives,
                              HashMap<String,Double> maxSensorValues, HashMap<String,Double> minSensorValues) {
        this.paths=paths;
        this.sensorValues = map;
        this.maxSensorValues = maxSensorValues;
        this.increaseObjectives = increaseObjectives;
        this.numberOfObjectives = increaseObjectives.size();
        this.minSensorValues = minSensorValues;
    }

    @Override
    public Paths call(){
        FitnessMap fitnessMap;
        if(paths.getEdgePath()==null){
            System.out.println("EvaluatePathThread: the path is null");
        }
        fitnessMap = fitnessMap(paths.getEdgePath(),sensorValues,increaseObjectives);
        fitnessMap.setTotalFitness(fitness(fitnessMap,paths.getEdgePath().size(),maxSensorValues,minSensorValues
                ,increaseObjectives));
        paths.setFitnessMap(fitnessMap);
        return paths;
    }
}
