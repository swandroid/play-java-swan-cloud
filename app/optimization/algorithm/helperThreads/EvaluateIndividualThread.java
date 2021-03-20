package optimization.algorithm.helperThreads;

import optimization.core.fitness.Fitness;
import optimization.core.population.Individual;
import optimization.core.population.Paths;
import optimization.hibernateModels.Vertex;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class EvaluateIndividualThread extends Fitness implements Runnable{

    Individual individual;
    ConcurrentHashMap<String,Paths> createdPaths;
    private Vertex origin;
    private Vertex destination;
    private HashMap<String,Double> maxSensorValues;
    private HashMap<String,Double> sensorValues;
    private HashMap<String,Double> minSensorValues;
    private HashMap<String,Boolean> objectives;
    private int numberOfObjectives;
    public EvaluateIndividualThread(Individual individual, ConcurrentHashMap<String, Paths> createdPaths, Vertex origin,
                                    Vertex destination, HashMap<String,Double> maxSensorValues,
                                    HashMap<String,Double> sensorValues, HashMap<String,Double> minSensorValues, HashMap<String,Boolean> objectives){
        this.individual = individual;
        this.origin = origin;
        this.destination = destination;
        this.sensorValues = sensorValues;
        this.maxSensorValues = maxSensorValues;
        this.objectives = objectives;
        this.numberOfObjectives = objectives.size();
        this.createdPaths = createdPaths;
        this.minSensorValues = minSensorValues;
    }
    
    private void evaluatePath(ConcurrentHashMap<String, Paths> createdPaths) {
        boolean originInConnectionEdge = false;
        String id;
        boolean destinationInConnectionEdge = false;
        if(origin.getId().equals(individual.getChosenConnections().get(0).getOrigin().getId())){
            originInConnectionEdge = true;
        }
        if(destination.getId().equals(individual.getChosenConnections().get(individual.getChosenConnections().size()-1).getDestination().getId())){
            destinationInConnectionEdge = true;
        }
        int numberOfPathEdges = 0;
        int num;
        double fitness = 0;
        if((originInConnectionEdge)&&(!destinationInConnectionEdge)){
            for(int i = 0;i<individual.getChosenConnections().size()-1;i++){
                if(individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges++;

                }else{
                    id = individual.getChosenConnections().get(i).getDestination().getId()
                            +"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
                    num = createdPaths.get(id).getEdgePath().size();
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    fitness = fitness+(createdPaths.get(id).getFitness()) * num;
                    numberOfPathEdges = numberOfPathEdges + num + 1;
                }
            }
            id = individual.getChosenConnections().get(individual.getChosenConnections().size()-1).getDestination().getId()+"."+destination.getId();
            num = createdPaths.get(id).getEdgePath().size();
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
            fitness = fitness+(createdPaths.get(id).getFitness())*num;
            numberOfPathEdges = numberOfPathEdges + num + 1;

        }else if((!originInConnectionEdge)&&(!destinationInConnectionEdge)){

            id = origin.getId()+"."+individual.getChosenConnections().get(0).getOrigin().getId();
            numberOfPathEdges += createdPaths.get(id).getEdgePath().size();
            fitness += (createdPaths.get(id).getFitness()) * numberOfPathEdges;
            for(int i = 0;i<individual.getChosenConnections().size()-1;i++){

                if(individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges++;

                }else{
                    id = individual.getChosenConnections().get(i).getDestination().getId()+"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
    
                    if(!createdPaths.containsKey(id))
                        System.out.println("the created paths do not contain key "+id);
                    num = createdPaths.get(id).getEdgePath().size();
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    fitness += createdPaths.get(id).getFitness() * num;
                    numberOfPathEdges = numberOfPathEdges + num + 1;
                }
            }

            id = individual.getChosenConnections().get(individual.
                    getChosenConnections().size()-1).getDestination().getId()+"."+destination.getId();
            num = createdPaths.get(id).getEdgePath().size();
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
            fitness += createdPaths.get(id).getFitness() * num;
            numberOfPathEdges = numberOfPathEdges + num + 1;
        }else if(originInConnectionEdge){
            if((individual.getChosenConnections().get(0).getOrigin().getId().equals(origin.getId()))
                    &&(individual.getChosenConnections().get(0).getDestination().getId().equals(destination.getId()))){
                individual.setFitness(fitness(individual.getChosenConnections().get(0),maxSensorValues,sensorValues,minSensorValues,objectives));
            }
            for(int i = 0;i<individual.getChosenConnections().size()-1;i++){
                if(individual.getChosenConnections().get(i).getDestination().getId()
                        .equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges += 1;
                }else{
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    id = individual.getChosenConnections().get(i).getDestination().getId()
                            +"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
                    num = createdPaths.get(id).getEdgePath().size();
                    fitness += createdPaths.get(id).getFitness() * num;
                    numberOfPathEdges = numberOfPathEdges + num + 1;
                }

            }
            numberOfPathEdges += 1;
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
        }else {
            id = origin.getId()+"."+individual.getChosenConnections().get(0).getOrigin().getId();
            num = createdPaths.get(id).getEdgePath().size();
            fitness += createdPaths.get(id).getFitness() * num;
            numberOfPathEdges += num;
            for(int i = 0;i<individual.getChosenConnections().size()-1;i++){
                if(individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges += 1;
                }else{
                    id = individual.getChosenConnections().get(i).getDestination().getId()+"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
                    num = createdPaths.get(id).getEdgePath().size();
                    numberOfPathEdges = numberOfPathEdges + num + 1;
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    fitness += createdPaths.get(id).getFitness()*num;
                }
            }
            numberOfPathEdges += 1;
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
        }
        individual.setFitness(fitness/numberOfPathEdges);
    }
    
    @Override
    public void run() {
        evaluatePath(createdPaths);
    }
}
