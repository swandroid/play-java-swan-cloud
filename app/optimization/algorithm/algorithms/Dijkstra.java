package optimization.algorithm.algorithms;

import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.*;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */
public class Dijkstra extends AbstractOptimizationAlgorithm {
    private final List<Vertex> nodes;
    private final List<Edge> edges;
    private HashMap<String,Boolean> increaseObjectives;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Double> distance;
    private HashMap<String, Double> maxSensorValues;
    private HashMap<String,Double> sensorValues;
    private HashMap<String,Double> minSensorValues;
    private HashMap<String,Double> averageValues;
    private HashMap<String,HashMap<String,Double>> fitnessValues = new HashMap<>();
    private HashMap<String,Double> fitValues = new HashMap<>();
    private int numberOfObjectives;
    private List<Vertex> previousPath;
    
    public Dijkstra(Graph graph, HashMap<String, Boolean> increaseObjectives, HashMap<String, Double> sensorValues,
                    HashMap<String, Double> maxSensorValues, HashMap<String, Double> minSensorValues, List<Vertex> previousPath) {
        this.nodes = new ArrayList<>(graph.getVertexes());
        this.edges = new ArrayList<>(graph.getEdges());
        this.increaseObjectives = increaseObjectives;
        this.sensorValues = sensorValues;
        this.maxSensorValues = maxSensorValues;
        this.numberOfObjectives = increaseObjectives.size();
        this.minSensorValues = minSensorValues;
        this.previousPath = previousPath;
    }
    
    public void execute(Vertex source) {
        settledNodes = new HashSet<>();
        unSettledNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();
        distance.put(source, (double)0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            Vertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }
    
    private void findMinimalDistances(Vertex node) {
        List<Vertex> adjacentNodes = getNeighbors(node);
        for (Vertex target : adjacentNodes) {
            if((increaseObjectives!=null) &&(!increaseObjectives.isEmpty())){
                double tempFitness = getDistance(node, target);
                if (getShortestDistance(target) > getShortestDistance(node) + tempFitness) {
                    fitValues.put(node.getId()+target.getId(), tempFitness);
                    distance.put(target, tempFitness);
                    predecessors.put(target, node);
                    unSettledNodes.add(target);
                }
            }

        }
    }

    private double getDistance(Vertex node, Vertex target) {
        double fitness;
        for (Edge edge : edges) {
            if (edge.getOrigin().getId().equals(node.getId())
                    && edge.getDestination().getId().equals(target.getId())) {
                if((increaseObjectives!=null) &&(!increaseObjectives.isEmpty())){
                    fitness = fitness(edge,maxSensorValues,sensorValues,minSensorValues,increaseObjectives);
                    fitValues.put(edge.getEdge_id(),fitness);
                    return fitness;
                }
            }
        }
        throw new RuntimeException("Should not happen");
    }
    
    private Double penalty( String edgeId,String objective) {
        return (sensorValues.get(edgeId+objective)-averageValues.get(objective))/maxSensorValues.get(objective);
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getOrigin().getId().equals(node.getId())
                    && !isSettled(edge.getDestination())) {
                if(!contained(edge.getDestination()))
                    neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    public List<Vertex> getPath(Vertex target) {
        List<Vertex> path = new ArrayList<>();
        Vertex step = target;
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        Collections.reverse(path);
        return path;
    }
    
    private boolean contained(Vertex destination) {
        for(Vertex vertex:previousPath){
            if(destination.getId().equals(vertex.getId()))
                return true;
        }
        return false;
    }
    
    private Vertex getMinimum(Set<Vertex> vertexes) {
        Vertex minimum = null;
        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(Vertex vertex) {
        return settledNodes.contains(vertex);
    }

    private Double getShortestDistance(Vertex destination) {
        Double d = distance.get(destination);
        if (d == null) {
            return Double.MAX_VALUE;
        } else {
            return d;
        }
    }

    public double getFitness(List<Edge> path){
        double fit = 0;
        for(Edge edge:path){
            fit += fitValues.get(edge.getOrigin().getId()+edge.getDestination().getId());
        }
        return fit/path.size();
    }
    
}
