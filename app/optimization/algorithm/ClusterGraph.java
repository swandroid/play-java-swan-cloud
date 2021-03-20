package optimization.algorithm;

import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.LinkedList;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class ClusterGraph {
    private int cluster;
    private Graph graph;
    private LinkedList<Vertex> path;
    private Vertex origin;
    private Vertex destination;
    private String id;

    public ClusterGraph(){

    }

    public ClusterGraph(int cluster){
        this.cluster=cluster;
        this.path=new LinkedList<>();

    }


    public ClusterGraph(int cluster,Graph graph){
        this.cluster=cluster;
        this.graph=graph;
        this.path=new LinkedList<>();

    }

    public ClusterGraph(int cluster,Graph graph,Vertex origin){
        this.cluster=cluster;
        this.graph=graph;
        this.path=new LinkedList<>();
        this.origin=origin;

    }

    public ClusterGraph(int cluster, Graph graph, Vertex origin, Vertex destination){
        this.cluster=cluster;
        this.graph=graph;
        this.path=new LinkedList<>();
        this.origin=origin;
        this.destination=destination;

    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public LinkedList<Vertex> getPath() {
        return path;
    }

    public void setPath(LinkedList<Vertex> path) {
        this.path = path;
    }

    public Vertex getDestination() {
        return destination;
    }

    public void setDestination(Vertex destination) {
        this.destination = destination;
    }

    public Vertex getOrigin() {
        return origin;
    }

    public void setOrigin(Vertex origin) {
        this.origin = origin;
    }

    @Override
    public String toString(){
        return ("cluster: "+String.valueOf(getCluster())+ " ,origin "+getOrigin().getId()+" ,destination "+getDestination().getId()
                +"  , graphid "+getGraph().getGraph_id()+ " , origin cluster "+getOrigin().getCluster()+  "  ,destination cluster "+getDestination().getCluster());
    }

    @Override
    public boolean equals(Object object){
        ClusterGraph clusterGraph = (ClusterGraph) object;
        if(this.getOrigin().getId().equals(clusterGraph.origin.getId())&&(this.getDestination().getId().equals(clusterGraph.getDestination().getId()))) return true;
        return false;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
