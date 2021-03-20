package optimization.core.population;

import optimization.algorithm.ClusterGraph;
import optimization.core.fitness.FitnessMap;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class Individual{
    private List<Vertex> path;
    private List<Edge> EdgePath;
    private List<Vertex> skeleton;
    private List<Edge> chosenConnections;
    private Vertex origin;
    private Vertex destination;
    private HashMap<Integer, ClusterGraph> clustersMap;
    private double fitness=Double.MAX_VALUE;
    private int id;
    private FitnessMap fitnessMap;



    public Individual(List<Vertex> skeleton){
        this.skeleton=skeleton;
    }

    public Individual(){
    }

    public Individual(int id){
        this.id=id;
    }

    public List<Vertex> getSkeleton() {
        return skeleton;
    }

    public void setSkeleton(List<Vertex> skeleton) {
        this.skeleton = skeleton;
    }

    public List<Edge> getChosenConnections() {
        return chosenConnections;
    }

    public void setChosenConnections(List<Edge> chosenConnections) {
        this.chosenConnections = chosenConnections;
    }

    public HashMap<Integer, ClusterGraph> getClustersMap() {
        return clustersMap;
    }

    public void setClustersMap(HashMap<Integer, ClusterGraph> clustersMap) {
        this.clustersMap = clustersMap;
    }

    public List<Vertex> getPath() {
        return path;
    }

    public void setPath(List<Vertex> path) {
        this.path = path;
    }

    public Vertex getOrigin() {
        return origin;
    }

    public void setOrigin(Vertex origin) {
        this.origin = origin;
    }

    public Vertex getDestination() {
        return destination;
    }

    public void setDestination(Vertex destination) {
        this.destination = destination;
    }

    public double getFitness() {
        return fitness;
    }
    

    public void setFitness(double fitness) {
        this.fitness=fitness;
    }


    public List<Edge> getEdgePath() {
        return EdgePath;
    }

    public void setEdgePath(List<Edge> edgePath) {
        EdgePath = edgePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
    public synchronized Individual copy(){
        Individual individual = new Individual();
        List<Edge> edgeList = new ArrayList<>();
        List<Vertex> vertexList = new ArrayList<>();
    
        for(Edge edge:this.getEdgePath()){
            edgeList.add(edge);
        }
        for(Vertex vertex:this.getPath()){
            vertexList.add(vertex);
        }
        individual.setEdgePath(edgeList);
        individual.setPath(vertexList);
        individual.setFitness(this.getFitness());
        individual.setId(this.getId());
        individual.setOrigin(this.getOrigin());
        individual.setDestination(this.getDestination());
        return individual;
    }
    
    public FitnessMap getFitnessMap() {
        return fitnessMap;
    }
    
    public void setFitnessMap(FitnessMap fitnessMap) {
        this.fitnessMap = fitnessMap;
    }
}


