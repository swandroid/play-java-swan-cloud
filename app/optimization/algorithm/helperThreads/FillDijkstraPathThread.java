package optimization.algorithm.helperThreads;

import optimization.core.fitness.Fitness;
import optimization.core.population.Individual;
import optimization.core.population.Paths;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class FillDijkstraPathThread extends Fitness implements Callable {

    Individual individual;
    Map<String,Paths> createdPaths;
    private Vertex origin;
    private Vertex destination;
    private HashMap<String,Double> maxSensorValues;
    private HashMap<String,Double> sensorValues;
    private HashMap<String,Boolean> objectives;
    private int numberOfObjectives;
    private HashMap<String,Double> minSensorValues;
    public FillDijkstraPathThread(Individual individual, Map<String,Paths> createdPaths, Vertex origin,
                                  Vertex destination, HashMap<String,Double> maxSensorValues,
                                  HashMap<String,Double> sensorValues, HashMap<String,Double> minSensorValues, HashMap<String,Boolean> objectives){
        this.individual = individual;
        this.createdPaths = createdPaths;
        this.origin = origin;
        this.destination = destination;
        this.sensorValues = sensorValues;
        this.maxSensorValues = maxSensorValues;
        this.objectives = objectives;
        this.numberOfObjectives = objectives.size();
        this.minSensorValues = minSensorValues;
    }

    @Override
    public Individual call() {
        Individual individualNew = fillPath(individual,createdPaths);
        if(individualNew != null)
            checkForCycle(individualNew.getEdgePath());
        return individualNew;
    }

    private Individual fillPath(Individual individual, Map<String,Paths> createdPaths) {
        boolean originInConnectionEdge = false;
        List<Vertex> vertexList = new ArrayList<>();
        List<Edge> edgeList = new ArrayList<>();
        Paths paths;
        String id;
        boolean destinationInConnectionEdge = false;
        if(origin.getId().equals(individual.getChosenConnections().get(0).getOrigin().getId())){
            originInConnectionEdge=true;
        }
        if(destination.getId().equals(individual.getChosenConnections().get(individual.getChosenConnections().size()-1).getDestination().getId())){
            destinationInConnectionEdge=true;
        }
        int numberOfPathEdges=0;
        int num;
        double fitness=0;
        if((originInConnectionEdge)&&(!destinationInConnectionEdge)){
            for(int i=0;i<individual.getChosenConnections().size()-1;i++){
                if(individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    edgeList.add(individual.getChosenConnections().get(i));
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges++;
                }else{
                    id = individual.getChosenConnections().get(i).getDestination().getId()
                           +"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
                    edgeList.add(individual.getChosenConnections().get(i));
                    if(!createdPaths.containsKey(id))
                        return null;
                    
                    edgeList.addAll(createdPaths.get(id).getEdgePath());
                    num = createdPaths.get(id).getEdgePath().size();
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    fitness += createdPaths.get(id).getFitness() * num;
                    numberOfPathEdges += num + 1;
                }
            }

            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size()-1));
            id = individual.getChosenConnections().get(individual.getChosenConnections().size()-1).getDestination().getId()+"."+destination.getId();
            if(!createdPaths.containsKey(id))
                return null;
            
            num = createdPaths.get(id).getEdgePath().size();
            edgeList.addAll(createdPaths.get(id).getEdgePath());
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
            fitness += createdPaths.get(id).getFitness() * num;
            numberOfPathEdges += num + 1;

        }else if((!originInConnectionEdge)&&(!destinationInConnectionEdge)){
            id =  origin.getId()+"."+individual.getChosenConnections().get(0).getOrigin().getId();
            if(!createdPaths.containsKey(id))
                return null;
            
            numberOfPathEdges += createdPaths.get(id).getEdgePath().size();
            edgeList.addAll(createdPaths.get(id).getEdgePath());
            fitness += createdPaths.get(id).getFitness() * numberOfPathEdges;
            for(int i=0;i<individual.getChosenConnections().size()-1;i++){
                if(individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    edgeList.add(individual.getChosenConnections().get(i));
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges++;
                }else{
                    id = individual.getChosenConnections().get(i).getDestination().getId()+"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
                    edgeList.add(individual.getChosenConnections().get(i));
                    if(!createdPaths.containsKey(id))
                        return null;
                        
                    num = createdPaths.get(id).getEdgePath().size();
                    edgeList.addAll(createdPaths.get(id).getEdgePath());
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    fitness += createdPaths.get(id).getFitness() * num;
                    numberOfPathEdges += num + 1;
                }
            }
            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size()-1));
            id = individual.getChosenConnections().get(individual.
                    getChosenConnections().size()-1).getDestination().getId()+"."+destination.getId();
            if(!createdPaths.containsKey(id))
                return null;
            
            num = createdPaths.get(id).getEdgePath().size();
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
                        edgeList.addAll(createdPaths.get(id).getEdgePath());

            fitness += createdPaths.get(id).getFitness() * num;
            numberOfPathEdges += num + 1;
        }else if(originInConnectionEdge){
            if((individual.getChosenConnections().get(0).getOrigin().getId().equals(origin.getId()))
                    &&(individual.getChosenConnections().get(0).getDestination().getId().equals(destination.getId()))){
                vertexList.add(origin);
                vertexList.add(destination);
                edgeList.add(individual.getChosenConnections().get(0));
                paths = new Paths(vertexList.get(0).getId()+"."+vertexList.get(vertexList.size()-1).getId(),vertexList,edgeList);
                paths.setFitness(fitness(individual.getChosenConnections().get(0),maxSensorValues,sensorValues,minSensorValues,objectives));
                individual.setPath(paths.getVertexPath());
                individual.setEdgePath(paths.getEdgePath());
                individual.setFitness(paths.getFitness());
                return individual;
            }
            for(int i=0;i<individual.getChosenConnections().size()-1;i++){
                if(individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    edgeList.add(individual.getChosenConnections().get(i));
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges += 1;
                }else{
                    edgeList.add(individual.getChosenConnections().get(i));
                    fitness+=fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    id = individual.getChosenConnections().get(i).getDestination().getId()
                            +"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
    
                    if(!createdPaths.containsKey(id))
                        return null;
                    
                    num = createdPaths.get(id).getEdgePath().size();
                    edgeList.addAll(createdPaths.get(id).getEdgePath());
                    fitness += createdPaths.get(id).getFitness() * num;
                    numberOfPathEdges += num + 1;
                }
            }
            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size()-1));
            numberOfPathEdges += 1;
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
        }else{
            id=origin.getId()+"."+individual.getChosenConnections().get(0).getOrigin().getId();
            if(!createdPaths.containsKey(id))
                return null;
            edgeList.addAll(createdPaths.get(id).getEdgePath());
            num = createdPaths.get(id).getEdgePath().size();
            fitness += createdPaths.get(id).getFitness() * num;
            numberOfPathEdges += num;
            for(int i=0;i<individual.getChosenConnections().size()-1;i++){
                if(individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i+1).getOrigin().getId())) {
                    edgeList.add(individual.getChosenConnections().get(i));
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    numberOfPathEdges += 1;
                }else{
                    edgeList.add(individual.getChosenConnections().get(i));
                    id = individual.getChosenConnections().get(i).getDestination().getId()+"."+individual.getChosenConnections().get(i+1).getOrigin().getId();
                    if(!createdPaths.containsKey(id))
                        return null;
                    
                    edgeList.addAll(createdPaths.get(id).getEdgePath());
                    num = createdPaths.get(id).getEdgePath().size();
                    numberOfPathEdges += num + 1;
                    fitness += fitness(individual.getChosenConnections().get(i),maxSensorValues,sensorValues,minSensorValues,objectives);
                    fitness += createdPaths.get(id).getFitness() * num;
                }
            }
            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size()-1));
            numberOfPathEdges +=1 ;
            fitness += fitness(individual.getChosenConnections().get(individual.getChosenConnections().size()-1),maxSensorValues,sensorValues,minSensorValues,objectives);
        }
        vertexList = convertToVertexPath(edgeList);
        paths =  new Paths(vertexList,edgeList);
        paths.setFitness(fitness/numberOfPathEdges);
        individual.setPath(paths.getVertexPath());
        individual.setEdgePath(paths.getEdgePath());
        individual.setFitness(paths.getFitness());
        return individual;
    }

    private void checkForCycle(List<Edge> edgeList) {
        int k;
        for(int i=0;i<edgeList.size();i++){
            k=contained(edgeList.get(i),edgeList);
            if(k>1){
                System.out.println("THERE IS A CYCLE for edge: "+edgeList.get(i).getEdge_id());
            }
        }
    }
    
    private int contained(Edge edge, List<Edge> edgePath) {
        int k = 0;
        for(int j=0;j<edgePath.size();j++){
            if(edge.getEdge_id().equals(edgePath.get(j).getEdge_id())){
                k++;
            }
        }
        return k;
    }

    private List<Vertex> convertToVertexPath(List<Edge> edgePath) {
        List<Vertex> verticesPath = new ArrayList<>();
        for(Edge edge:edgePath){
            verticesPath.add(edge.getOrigin());
        }
        verticesPath.add(edgePath.get(edgePath.size()-1).getDestination());
        return verticesPath;
    }
}

