package optimization.algorithm.helperThreads;

import optimization.algorithm.ClusterGraph;
import optimization.algorithm.algorithms.Dijkstra;
import optimization.core.population.Paths;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;



/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class DijkstraRunnable implements Callable {
    private Graph graph;
    private Vertex origin;
    private Vertex destination;
    private HashMap<String,Boolean> increaseObjectives;
    private  HashMap<String,Double> sensorValues;
    private HashMap<String,Double> maxObjectivesValues;
    private HashMap<String, Double> minSensorValues;
    private List<Vertex> previousPath;


    public DijkstraRunnable(ClusterGraph clusterGraph, HashMap<String,Boolean> increaseObjectives, Vertex origin,
                            Vertex destination,HashMap sensorValues,HashMap maxObjectivesValues,HashMap minSensorValues
            ,List<Vertex> previousPath) {
        this.sensorValues = sensorValues;
        this.graph = clusterGraph.getGraph();
        this.origin = origin;
        this.destination = destination;
        this.increaseObjectives = increaseObjectives;
        this.maxObjectivesValues = maxObjectivesValues;
        this.minSensorValues = minSensorValues;
        this.previousPath = previousPath;
    }

    @Override
    public Paths call() {
//       System.out.println("graph "+graph.getGraph_id() + "  "+ "origin cluster "+origin.getCluster()+ " destination cluster "+destination.getCluster());
        Dijkstra dijkstra = new Dijkstra(graph,increaseObjectives, sensorValues,maxObjectivesValues,minSensorValues,previousPath);
        dijkstra.execute(origin);
        List<Vertex> path = dijkstra.getPath(destination);
        if(path == null){
            System.out.println(" DijkstraRunnable: the path is null for "+"graph "+graph.getGraph_id() + "  "+ "origin cluster "+origin.getCluster()+ " destination cluster "+destination.getCluster());
            return null;
        }else {
            List<Edge> edgePath = dijkstra.getEdgesPath(path,graph.getEdges());
            double fitness = dijkstra.getFitness(edgePath);
            Paths paths = new Paths(path,edgePath,fitness);
            paths.setPathID(path.get(0).getId()+"."+path.get(path.size()-1).getId());
            return paths;
        }
    }
}
