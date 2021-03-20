package optimization.core.population;

import optimization.core.fitness.FitnessMap;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class Paths {
    private List<Vertex> vertexPath;
    private List<Edge> edgePath;
    private double fitness=0;
    private String pathID;
    private FitnessMap fitnessMap;

    private HashMap<String,HashMap<String,Float>> edgeValues= new HashMap<>();

    public Paths(){}

    public Paths(List<Vertex> vertexPath, List<Edge> edgePath){
        this.vertexPath=Collections.unmodifiableList(new ArrayList<>(vertexPath));
        this.edgePath= Collections.unmodifiableList(new ArrayList<>(edgePath));
        this.edgeValues=new HashMap<>();
    }

    public Paths(List<Vertex> vertexPath, List<Edge> edgePath, double fitness){
        this.vertexPath=Collections.unmodifiableList(new ArrayList<>(vertexPath));
        this.edgePath= Collections.unmodifiableList(new ArrayList<>(edgePath));
        this.edgeValues=new HashMap<>();
        this.fitness=fitness;
    }

    public Paths(String s, List<Vertex> vertexList, List<Edge> edgeList) {
        this.vertexPath=Collections.unmodifiableList(new ArrayList<>(vertexPath));
        this.edgePath= Collections.unmodifiableList(new ArrayList<>(edgePath));
        this.edgeValues=new HashMap<>();
    }



    public List<Vertex> getVertexPath() {
        return vertexPath;
    }

    public List<Edge> getEdgePath() {
        return edgePath;
    }

    public HashMap<String, HashMap<String, Float>> getEdgeValues() {
        return edgeValues;
    }

    public void setEdgeValues(HashMap<String, HashMap<String, Float>> edgeValues) {
        this.edgeValues = edgeValues;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public String getPathID() {
        return pathID;
    }

    public void setPathID(String pathID) {
        this.pathID = pathID;
    }
    
    public FitnessMap getFitnessMap() {
        return fitnessMap;
    }
    
    public void setFitnessMap(FitnessMap fitnessMap) {
        this.fitnessMap = fitnessMap;
    }
}
