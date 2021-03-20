package optimization.algorithm.algorithms;

import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.*;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

/**
 *This algorithm is the random walk but excludes the previous in the path.
 * It does not produce paths with cycles.
 * */
public class RandomAcyclic extends AbstractOptimizationAlgorithm {
    
    private Set<Edge> edges;
    private LinkedList<Vertex> pathVertexes = new LinkedList<>();
    protected List<Vertex> neighbors;
    private List<Edge> neighborhood;
    private Vertex previous;
    private HashMap<Vertex,Boolean> blacklist = new HashMap<>();
    private Set<Edge> initialEdges;

    public RandomAcyclic(Graph graph){
        this.edges = graph.getEdges();
    }

    public void execute(Vertex source, Vertex destination) {
        Vertex next = source;
        pathVertexes.add(next);
        Vertex last;
        previous = new Vertex("00000000000000000000");
        Vertex temp;
        initialEdges = edges;
        while (!next.getId().equals(destination.getId())){
            neighbors(next,destination);
            if(neighbors.size()>0){
                temp = next;
                previous = temp;
                next = chooseRandom(neighbors);
                pathVertexes.add(next);
            }
            else {
                if(pathVertexes.size()<= 2){
                    blacklist.clear();
                    next = source;
                    previous = new Vertex("00000000000000000000");
                    edges = initialEdges;
                    pathVertexes.clear();
                    pathVertexes.add(next);
                }else {
                    last = pathVertexes.removeLast();
                    blacklist.put(last, true);
                    next = pathVertexes.getLast();
                    previous = pathVertexes.get(pathVertexes.size()-2);
                    Iterator<Edge> edgie = edges.iterator();
                    while (edgie.hasNext()) {
                        Edge e = edgie.next();
                        if (((e.getDestination().getId().equals(last.getId())) && (e.getOrigin().getId().equals(next.getId())))
                                ||((e.getDestination().getId().equals(next.getId())) && (e.getOrigin().getId().equals(last.getId())))) {
                            edgie.remove();
                        }
                    }
                }
            }
        }
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> allNeighbors = new ArrayList<>();
        neighborhood = new ArrayList<>();
        for (Edge edge : edges) {
            if((edge.getOrigin().getId().equals(node.getId()))){
                allNeighbors.add(edge.getDestination());
                neighborhood.add(edge);
            }
        }
        return allNeighbors;
    }

    private void neighbors(Vertex vertex, Vertex destination) {
        List<Vertex> allNeighbors = getNeighbors(vertex);
        neighbors = new ArrayList<>();
        if(allNeighbors.contains(destination))
        {
            neighbors.add(destination);
            return;
        }
        for(Edge e:neighborhood){
            if(!pathVertexes.contains(e.getDestination())) {
                if(!blacklist.containsKey(e.getDestination()))
                    neighbors.add(e.getDestination());
                }else {
                    blacklist.put(e.getDestination(),true);
            }
        }
    }

    public LinkedList<Vertex> getPath() {
        return this.pathVertexes;
    }
}
