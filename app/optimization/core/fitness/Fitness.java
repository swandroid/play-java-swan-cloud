package optimization.core.fitness;

import optimization.algortihmsConditions.AlgorithmSettings;
import optimization.core.population.Individual;
import optimization.hibernateModels.Edge;

import java.util.HashMap;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class Fitness implements FitnessInterface {

    @Override
    public double fitness(List<Edge> edgeList, HashMap<String,Double> maxSensorValues, HashMap<String,
            Double> sensorValues, HashMap<String,Double> minSensorValues, HashMap<String,Boolean> objectives){
        double fitness=0;
        for(Edge edge:edgeList){
            fitness+=fitness(edge,maxSensorValues,sensorValues,minSensorValues,objectives);
        }
        return fitness;
    }
        
        @Override
    public double fitness(Edge edge, HashMap<String,Double> maxSensorValues, HashMap<String,Double> sensorValues, HashMap<String,Double> minSensorValues, HashMap<String,Boolean> objectives){
        double fitness = 0;
        double objectiveFitness;
        for(String objective:objectives.keySet()){
            if(objectives.get(objective)){
                objectiveFitness = (maxSensorValues.get(objective)-sensorValues.get(edge.getEdge_id()+objective))/maxSensorValues.get(objective);
            }else {
                if(objective.equals("distance")){
                    objectiveFitness = (edge.getDistance() - minSensorValues.get(objective))/maxSensorValues.get(objective);
                }else{
                    objectiveFitness = (sensorValues.get(edge.getEdge_id()+objective) - minSensorValues.get(objective))/maxSensorValues.get(objective);
                }
            }
            fitness+= getObjectiveFitness(objectiveFitness,objective);
        }
        return fitness;
    }
    
    @Override
    public double fitness(HashMap<String,Double> maxSensorValues,HashMap<String,Double> minSensorValues,HashMap<String,Boolean> objectives,int pathSize, Individual individual){
        double fitness = 0;
        double objectiveFitness;
        for(String objective:objectives.keySet()){
            if(objectives.get(objective))
                objectiveFitness = (maxSensorValues.get(objective)-(individual.getFitnessMap().getValues().get(objective)/pathSize))/maxSensorValues.get(objective);
            else
            {
                objectiveFitness = ((individual.getFitnessMap().getValues().get(objective)/pathSize) - minSensorValues.get(objective))/maxSensorValues.get(objective);
            }
            fitness += getObjectiveFitness(objectiveFitness,objective);
        }
        return fitness;
    }
    
    
    
    @Override
    public double fitness(Edge edge, HashMap<String, Double> maxSensorValues, HashMap<String, Double> sensorValues, HashMap<String, Boolean> objectives) {
        return 0;
    }
    
    @Override
    public double fitness(FitnessMap fitnessMap, int pathSize, HashMap<String,Double> maxSensorValues, HashMap<String,Double> minSensorValues, HashMap<String,Boolean> objectives){
        double fitness = 0;
        double objectiveFitness;
        for(String objective:objectives.keySet()){
            if(objectives.get(objective)){
                objectiveFitness = (maxSensorValues.get(objective)-(fitnessMap.getValues().get(objective)/pathSize))/maxSensorValues.get(objective);
            }else {
                    objectiveFitness = ((fitnessMap.getValues().get(objective)/pathSize) - minSensorValues.get(objective))/maxSensorValues.get(objective);
            }
            fitness += getObjectiveFitness(objectiveFitness,objective);
        }
        return fitness;
    }
    
    

    
    @Override
    public FitnessMap fitnessMap(List<Edge> edgeList, HashMap<String,Double> sensorValues, HashMap<String,Boolean> objectives){
        FitnessMap fitnessMap = new FitnessMap();
        fitnessMap.init(objectives.keySet());
        for(Edge edge:edgeList) {
            for (String objective : objectives.keySet()) {
                if (objective.equals("distance")) {
                    fitnessMap.getValues().put(objective, fitnessMap.getValues().get(objective) + edge.getDistance());
                } else {
                    fitnessMap.getValues().put(objective, fitnessMap.getValues().get(objective) + sensorValues.get(edge.getEdge_id() + objective));
                }
            }
        }
        return fitnessMap;
    }
    
    private double getObjectiveFitness(double objectiveFitness, String objective) {
        switch (objective) {
            case "distance":
                objectiveFitness = objectiveFitness * AlgorithmSettings.DISTANCE_WEIGHT;
                break;
            case "sound":
                objectiveFitness = objectiveFitness * AlgorithmSettings.SOUND_WEIGHT;
                break;
            case "air":
                objectiveFitness = objectiveFitness * AlgorithmSettings.AIR_WEIGHT;
                break;
            default:
                System.out.println("THE OBJECTIVE IS WRONG");
                exit(1);
        }
        return objectiveFitness;
    }
    
    
    

}
