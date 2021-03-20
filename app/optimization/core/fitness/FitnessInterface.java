package optimization.core.fitness;

import optimization.core.population.Individual;
import optimization.hibernateModels.Edge;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */


public interface FitnessInterface {

    double fitness(List<Edge> edgeList, HashMap<String, Double> maxSensorValues, HashMap<String,
		                                                                                        Double> sensorValues, HashMap<String, Double> minSensorValues, HashMap<String, Boolean> objectives);

    double fitness(Edge edge, HashMap<String, Double> maxSensorValues, HashMap<String, Double> sensorValues
		    , HashMap<String, Double> minSensorValues, HashMap<String, Boolean> objectives);

    double fitness(Edge edge, HashMap<String, Double> maxSensorValues, HashMap<String, Double> sensorValues
		    , HashMap<String, Boolean> objectives);
    
    double fitness(FitnessMap fitnessMap, int pathSize, HashMap<String, Double> maxSensorValues
		    , HashMap<String, Double> minSensorValues, HashMap<String, Boolean> objectives);
    
    FitnessMap fitnessMap(List<Edge> edgeList, HashMap<String, Double> sensorValues, HashMap<String, Boolean> objectives);
    
    double fitness(HashMap<String, Double> maxSensorValues, HashMap<String, Double> minSensorValues
		    , HashMap<String, Boolean> objectives, int pathSize, Individual individual);
        
        
        
    }
