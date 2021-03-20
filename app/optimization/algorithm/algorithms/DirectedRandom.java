package optimization.algorithm.algorithms;

import com.vividsolutions.jts.geom.Coordinate;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;
import tyrex.util.ArraySet;

import java.util.*;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

/**
 * The algorithm classifies the neighbors into 2 categories. The first one
 * includes either the destination, or the ones that their angle between
 * the current node and them and the current node and the destination is
 * smaller than a threshold or the distance from the middle point of the
 * edge (current node, neighbor) and the destination is smaller than the distance
 * between current node and destination. The second category includes the rest
 * excluding the previous node already the path. To avoid deadlock we include all
 * the nodes that lead to this in a blacklist and delete them from the path.
 * This algorithm avoids cycles.
 * */

public class DirectedRandom extends AbstractOptimizationAlgorithm
{
 //   private final Set<Vertex> nodes;
    private Set<Edge> edges = new HashSet<>();
    private LinkedList<Vertex> pathVertexes=new LinkedList<>();
    private List<Vertex> neighbors;
    private List<Vertex> directedNeighbors;
    private List<Edge> neighborhood;
    private HashMap<String,Boolean> blacklist = new HashMap<>();
    private float maxAngle;
    private Set<Edge> removedEdges;
    private List<Vertex> previousPath;


    public DirectedRandom(Graph graph, float maxAngle, List<Vertex> previousPath) {
        this.edges.addAll(graph.getEdges());
        this.maxAngle = maxAngle;
        this.previousPath = previousPath;
    }
    
    public void execute(Vertex source, Vertex destination) {
        if(source.getId().equals(destination.getId())){
            return;
        }
        Vertex next = source;
        pathVertexes.add(next);
        Vertex last;
        removedEdges = new ArraySet();
        while (!next.getId().equals(destination.getId())){
            neighbors(next,destination);
            if(directedNeighbors.size()>0){
                next = chooseRandom(directedNeighbors);
                pathVertexes.add(next);
            }else if(neighbors.size()>0){
                next = chooseRandom(neighbors);
                pathVertexes.add(next);
            }
            else {
                if(pathVertexes.size()<= 2){
                    if(pathVertexes.size() == 2){
                        last = pathVertexes.removeLast();
                        blacklist.put(last.getId(), true);
                        next = pathVertexes.getLast();
                        removedEdges = removeEdge(removedEdges,edges,next,last);
                    }else{
                        neighbors(source,destination);
                        pathVertexes.clear();
                        return;
                    }
                }else{
                    //the only neighbor is the origin
                    //the node is a deadlock so we remove the last one from the pathVertexes
                    last = pathVertexes.removeLast();
                    blacklist.put(last.getId(), true);
                    next = pathVertexes.getLast();
                    removedEdges = removeEdge(removedEdges,edges,next,last);
                }
            }
        }
    }
    
    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> allNeighbors = new ArrayList<>();
        neighborhood = new ArrayList<>();
        for (Edge edge : edges) {
            if((edge.getOrigin().getId().equals(node.getId()))){
                if(!contained(edge.getDestination())){
                    allNeighbors.add(edge.getDestination());
                    neighborhood.add(edge);
                }
            }
        }
        return allNeighbors;
    }
    
    private boolean contained(Vertex node) {
        for(Vertex vertex:pathVertexes){
            if(vertex.getId().equals(node.getId())){
                return true;
            }
        }
        return false;
    }

    private void neighbors(Vertex vertex, Vertex destination) {
        List<Vertex> allNeighbors = getNeighbors(vertex);
        directedNeighbors = new ArrayList<>();
        neighbors = new ArrayList<>();
        if(allNeighbors.contains(destination))
        {
        //    neighbors.add(destination);
            directedNeighbors.add(destination);
            return;
        }
        float angle;
        for(Edge e:neighborhood){
                if(!contained(e.getDestination())) {
                    if(!blacklist.containsKey(e.getDestination().getId())){
                        angle = angleBetween2Lines(e.getDestination().getCoordinate()
                                , vertex.getCoordinate(), destination.getCoordinate());
                        if (maxAngle > 0) {
                            if (((angle <= maxAngle) && (angle >= 0)) || (e.getMiddlePoint().distance(destination.getCoordinate())
                                    <= vertex.getCoordinate().distance(destination.getCoordinate()))
                                    || (e.getDestination().getCoordinate().distance(destination.getCoordinate()))
                                    <= vertex.getCoordinate().distance(destination.getCoordinate())) {
                                directedNeighbors.add(e.getDestination());
                            }else if ((e.getDestination().getCoordinate().distance(destination.getCoordinate())
                                    <= vertex.getCoordinate().distance(destination.getCoordinate()))) {
                                directedNeighbors.add(e.getDestination());
                            } else {
                                neighbors.add(e.getDestination());
                            }
                        }else {
                            if ((e.getDestination().getCoordinate().distance(destination.getCoordinate())
                                    <= vertex.getCoordinate().distance(destination.getCoordinate()))) {
                                directedNeighbors.add(e.getDestination());
                            } else {
                                neighbors.add(e.getDestination());
                            }
                        }
                    }
                }
                else {
                    blacklist.put(e.getDestination().getId(),true);
                }
        }
    }
    
    public LinkedList<Vertex> getPath() {
        return this.pathVertexes;
    }
    
    private static float angleBetween2Lines(Coordinate neighbor, Coordinate vertex, Coordinate destination) {
        float angle1 = (float) Math.atan2(neighbor.y - vertex.y, vertex.x - neighbor.x);
        float angle2 = (float) Math.atan2(destination.y - vertex.y, vertex.x - destination.x);
        float calculatedAngle = (float) Math.toDegrees(angle1 - angle2);
        if (calculatedAngle < 0) calculatedAngle += 360;
        if(calculatedAngle>180) calculatedAngle = 360-calculatedAngle;
        return calculatedAngle;
    }
    
    
}