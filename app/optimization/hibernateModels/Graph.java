package optimization.hibernateModels;

import com.google.common.collect.ImmutableSet;
import optimization.hibernateModels.saxParser.OsmHandler;
import org.hibernate.annotations.GenericGenerator;
import org.xml.sax.SAXException;

import javax.persistence.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

@Entity
@Table(name="Graph")
public class Graph implements Serializable{

    private Set<Vertex> vertexes = new HashSet<>();

    private Set<Edge> edges = new HashSet<>();

    @Id
    @GenericGenerator(name = "string_sequence", strategy = "hibernateUtils.StringIdGenerator")
    @GeneratedValue(generator = "string_sequence")
    @Column(name = "graph_id", nullable = false,updatable = false)
    private String graph_id;


    private HashMap<String,Edge> edgeHashMap = new HashMap<>();

    private HashMap<String,Vertex> vertexHashMap = new HashMap<>();


    public Graph(){

    }


    public Graph(String graph_id, Set<Vertex> vertexes, Set<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
        this.graph_id=graph_id;
    }

    public Graph(Set<Vertex> vertexes, Set<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public Graph(String s, HashMap<String, Vertex> vertexSet, HashMap<String, Edge> edgeSet) {
        this.graph_id=s;
        this.edgeHashMap = edgeSet;
        this.vertexHashMap = vertexSet;
    }

    public void addEdge(Edge e){
        this.edges.add(e);
    }

    public void addVertex(Vertex v) {this.vertexes.add(v);}

    public Set<Vertex> getVertexes() {
        return vertexes;
    }

    public Set<Edge> getEdges() {
        return edges;
    }


    public void setEdges(Set<Edge> edges) {
        this.edges=edges;
    }

    public void setVertexes(Set<Vertex> vertexes){ this.vertexes=vertexes;}

    public String getGraph_id() {
        return graph_id;
    }

    public void setGraph_id(String graph_id) {
        this.graph_id = graph_id;
    }

    public Graph(File inputFile) throws IOException, ParserConfigurationException, SAXException {
        this.edges = new HashSet<>();

        this.vertexes = new HashSet<>();


        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        if(!inputFile.exists() || !inputFile.isFile()) {
            throw new FileNotFoundException();
        }

        if(!inputFile.canRead()) {
            throw new IOException("IOException");
        }

        System.out.println("START FILE PARSING");

        OsmHandler handler = new OsmHandler();
        saxParser.parse(inputFile, handler);
        this.vertexes=ImmutableSet.copyOf(handler.getVertexes());
        this.edges=ImmutableSet.copyOf(handler.getEdges());
        HashMap<String, HashMap<Vertex, Vertex>> map=handler.getEdgesMap();

        for(Edge e:this.edges){
            e.setDistance((double)1);
            Edge edge = new Edge();
            edge.setEdge_id(e.getEdge_id()+e.getDestination()+e.getOrigin());
            //edge.setArea_id(e.getArea_id());
            edge.setDestination(e.getOrigin());
            edge.setOrigin(e.getDestination());
            edge.setMiddlePoint(e.getMiddlePoint());
            edge.setDistance(e.getDistance());
            this.edges.add(edge);
        }
    }


    public boolean equalsGraphs(Graph graph, Graph graph1){
        Boolean equal=false;
        if((graph.getVertexes().equals(graph1.getVertexes()))&&(graph.getEdges().equals(graph1.getEdges()))){
            return true;
        }
        return false;
    }

    public static boolean equals(Set<?> set1, Set<?> set2){

        if(set1 == null || set2 ==null){
            return false;
        }

        if(set1.size()!=set2.size()){
            return false;
        }

        return set1.containsAll(set2);

    }

    public HashMap<String, Edge> getEdgeHashMap() {
        return edgeHashMap;
    }

    public void setEdgeHashMap(HashMap<String, Edge> edgeHashMap) {
        this.edgeHashMap = edgeHashMap;
    }

    public HashMap<String, Vertex> getVertexHashMap() {
        return vertexHashMap;
    }

    public void setVertexHashMap(HashMap<String, Vertex> vertexHashMap) {
        this.vertexHashMap = vertexHashMap;
    }
}


