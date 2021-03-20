package optimization.algorithm.helperThreads;

import optimization.algorithm.ClusterGraph;
import optimization.algorithm.algorithms.DirectedRandom;
import optimization.algortihmsConditions.AlgorithmSettings;
import optimization.core.population.Paths;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class DirectedRandomCallable implements Callable{

    private Graph graph;
    private Vertex origin;
    private Vertex destination;
    private HashMap<String,Boolean> increaseObjectives;
    HashMap<String,Double> sensorValues;
    HashMap<String,Double> maxObjectivesValues;
    private List<Vertex> previousPath;
    private String id;

    public DirectedRandomCallable(ClusterGraph clusterGraph, HashMap<String,Boolean> increaseObjectives, Vertex origin,
                                  Vertex destination, HashMap sensorValues, HashMap maxObjectivesValues,
                                  List<Vertex> previousPath) {
        this.sensorValues = sensorValues;
        this.graph = clusterGraph.getGraph();
        this.origin = origin;
        this.destination = destination;
        this.increaseObjectives = increaseObjectives;
        this.maxObjectivesValues = maxObjectivesValues;
        this.previousPath = previousPath;
        this.id = clusterGraph.getId();
    }
    
    @Override
    public Paths call() {
        DirectedRandom directedRandom = new DirectedRandom(graph, AlgorithmSettings.ANGLE,previousPath);
//        System.out.println("DirectedRandom origin "+origin.getId()+" cluster "+origin.getCluster()+" destination "+destination.getId()
//                                   +" D cluster "+destination.getCluster()+ " graph "+graph.getGraph_id());
        directedRandom.execute(origin,destination);
        LinkedList<Vertex> path = directedRandom.getPath();
        if(path == null){
            System.out.println("DirectedRandomCallable: the path is null with origin: "+origin.getId()+"and destination "+destination.getId());
            return null;
        }else {
            LinkedList<Edge> ePath = directedRandom.getEdgesPath(path,graph.getEdges());
            Paths paths = new Paths(path,ePath,0);
            paths.setPathID(id);
            return paths;
        }
    }
}
