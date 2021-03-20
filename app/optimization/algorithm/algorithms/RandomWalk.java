package optimization.algorithm.algorithms;

import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.*;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */


/**
 * The algorithm is the random walk which may produce cycles
 * */

public class RandomWalk{
    private final Set<Vertex> nodes;
    private final Set<Edge> edges;
    private LinkedList<Vertex> pathVertexes;

    public RandomWalk(Graph graph) {
        this.nodes = graph.getVertexes();
        this.edges = graph.getEdges();
    }

    public void execute(Vertex source, Vertex destination) {
        pathVertexes = new LinkedList<>();
        Vertex vertex = source;
        Vertex next;
        pathVertexes.add(vertex);
        while (vertex!=destination){
            next = chooseRandomNeighbor(vertex,destination);
            pathVertexes.add(next);
            vertex = next;
        }
        if(vertex.getId().equals(destination.getId())){
            pathVertexes.add(destination);
        }
    }

    private Vertex chooseRandomNeighbor(Vertex vertex, Vertex destination) {
        Vertex randomNeighbour;
        List<Vertex> neighbors = getNeighbors(vertex);
        if(containsId(neighbors,destination))
            return destination;
        Random random = new Random();
        int position = random.nextInt((neighbors.size()));
        randomNeighbour = neighbors.get(position);
        return  randomNeighbour;
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<>();
        for (Edge edge : edges) {
            if(edge.getOrigin().getId().equals(node.getId())){
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;

    }

    public LinkedList<Vertex> getPath() {
        return this.pathVertexes;
    }

    private boolean containsId(List<Vertex> neighbors, Vertex vertex){
        for(Vertex v:neighbors){
            if(v.getId().equals(vertex.getId())){
                return true;
            }
        }
        return false;
    }

}
