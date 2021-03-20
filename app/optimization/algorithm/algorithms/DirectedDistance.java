package optimization.algorithm.algorithms;

import com.vividsolutions.jts.geom.Coordinate;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.*;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

/**
 * The distance is the metric to choose the next neighbour to the path
 * In case there is none closer, the algorithm chooses one randomly
 * with the previous in the path included. This algorithm might
 * produce a path with cycles.
 * */


public class DirectedDistance{

    private final Set<Vertex> nodes;
    private final Set<Edge> edges;
    private ArrayList<Vertex> pathVertexes;
    private Vertex destinationVertex;
    
    public DirectedDistance(Graph graph) {
        this.nodes = graph.getVertexes();
        this.edges = graph.getEdges();
    }
    
    public void execute(Vertex source, Vertex destination) {
        this.destinationVertex = destination;
        pathVertexes = new ArrayList<>();
        Vertex vertex = source;
        Vertex next;
        pathVertexes.add(vertex);
        while (!vertex.getId().equals(destinationVertex.getId())){
            next = chooseDirectedNeighbor(vertex,destinationVertex);
            pathVertexes.add(next);
            vertex = next;
        }
        if(vertex.getId().equals(destinationVertex.getId())){
            pathVertexes.add(destinationVertex);
        }
    }

    private double calculateDistance(Coordinate coordinate, Coordinate coordinate1){
        return coordinate.distance(coordinate1);
    }
    private Vertex chooseDirectedNeighbor(Vertex vertex, Vertex destination) {
        Vertex neighbour = null;
        HashMap<Vertex,Double> neighbors = getNeighbors(vertex);
        List<Vertex> neighborhood = new ArrayList<>();
        if(containsId(neighbors,destination))
            return destination;
        int size = neighbors.size();
        for(Vertex v:neighbors.keySet()){
            neighborhood.add(v);
        }
        double minDistance = Double.MAX_VALUE;
        for(Vertex v:neighbors.keySet()){
            if(size == 1)
                return v;
            else {
                if(!contains(v)) {
                    if (neighbors.get(v) < minDistance) {
                        neighbour = v;
                        minDistance = neighbors.get(v);
                    }
                }
            }
        }
        if(neighbour == null){
            Random rand = new Random();
            int position;
            position = rand.nextInt((neighborhood.size()));
            neighbour = neighborhood.get(position);
        }
        return neighbour;
    }
    
    private HashMap<Vertex, Double> getNeighbors(Vertex node) {
        HashMap<Vertex,Double> directNeighbors = new HashMap<>();
        for (Edge edge : edges) {
            if(edge.getOrigin().getId().equals(node.getId())){
                directNeighbors.put(edge.getDestination(),calculateDistance(edge.getDestination().getCoordinate()
                        , node.getCoordinate()));
            }
        }
        return directNeighbors;
    }
    
    private boolean contains(Vertex node) {
        for(Vertex vertex:pathVertexes){
            if(vertex.getId().equals(node.getId())){
                return true;
            }
        }
        return false;
    }
    
    public List<Vertex> getPath() {
        return this.pathVertexes;
    }
    
    private boolean containsId(HashMap<Vertex,Double> neighbors, Vertex vertex){
        for(Vertex v:neighbors.keySet()){
            if(v.getId().equals(vertex.getId())){
                return true;
            }
        }
        return false;
    }

}
