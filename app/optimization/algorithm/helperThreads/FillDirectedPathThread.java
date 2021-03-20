package optimization.algorithm.helperThreads;

import optimization.core.population.Individual;
import optimization.core.population.Paths;
import optimization.hibernateModels.Edge;
import optimization.hibernateModels.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class FillDirectedPathThread implements Callable {
    
    private Individual individual;
    private Map<String, Paths> createdPaths;
    private Vertex origin;
    private Vertex destination;
    
    public FillDirectedPathThread(Individual individual, Map<String, Paths> createdPaths, Vertex origin,
                                  Vertex destination) {
        this.individual = individual;
        this.createdPaths = createdPaths;
        this.origin = origin;
        this.destination = destination;
    }
    
    @Override
    public Individual call() {
        return fillPath();
    }
    
    private Individual fillPath() {
        
            boolean originInConnectionEdge = false;
        List<Vertex> vertexList = new ArrayList<>();
        List<Edge> edgeList = new ArrayList<>();
        Paths paths;
        String id;
        boolean destinationInConnectionEdge = false;
        if (origin.getId().equals(individual.getChosenConnections().get(0).getOrigin().getId())) {
            originInConnectionEdge = true;
        }
        if (destination.getId().equals(individual.getChosenConnections().get(individual.getChosenConnections().size() - 1).getDestination().getId())) {
            destinationInConnectionEdge = true;
        }
        if ((originInConnectionEdge) && (!destinationInConnectionEdge)) {
            for (int i = 0; i < individual.getChosenConnections().size() - 1; i++) {
                if (individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i + 1).getOrigin().getId())) {
                    edgeList.add(individual.getChosenConnections().get(i));
                } else {
                    id = String.valueOf(individual.getId()) + "." + individual.getChosenConnections().get(i).getDestination().getId()
                                 + "." + individual.getChosenConnections().get(i + 1).getOrigin().getId();
                    edgeList.add(individual.getChosenConnections().get(i));
                    edgeList.addAll(createdPaths.get(id).getEdgePath());
                }
            }
    
            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size() - 1));
            id = String.valueOf(individual.getId()) + "." + individual.getChosenConnections().get(individual.getChosenConnections().size() - 1).getDestination().getId() + "." + destination.getId();
            edgeList.addAll(createdPaths.get(id).getEdgePath());
        } else if ((!originInConnectionEdge) && (!destinationInConnectionEdge)) {
            id = String.valueOf(individual.getId()) + "." + origin.getId() + "." + individual.getChosenConnections().get(0).getOrigin().getId();
            edgeList.addAll(createdPaths.get(id).getEdgePath());
            for (int i = 0; i < individual.getChosenConnections().size() - 1; i++) {
    
                if (individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i + 1).getOrigin().getId())) {
                    edgeList.add(individual.getChosenConnections().get(i));
        
                } else {
                    id = String.valueOf(individual.getId()) + "." + individual.getChosenConnections().get(i).getDestination().getId() + "." + individual.getChosenConnections().get(i + 1).getOrigin().getId();
                    edgeList.add(individual.getChosenConnections().get(i));
                    if (!createdPaths.containsKey(id)) {
                        System.out.println("id does not exist in created paths " + id);
                    }
                    edgeList.addAll(createdPaths.get(id).getEdgePath());
                }
            }
            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size() - 1));
            id = String.valueOf(individual.getId()) + "." + individual.getChosenConnections().get(individual.
                                                                                                                    getChosenConnections().size() - 1).getDestination().getId() + "." + destination.getId();
            edgeList.addAll(createdPaths.get(id).getEdgePath());
        } else if (originInConnectionEdge) {
            if ((individual.getChosenConnections().get(0).getOrigin().getId().equals(origin.getId()))
                        && (individual.getChosenConnections().get(0).getDestination().getId().equals(destination.getId()))) {
                vertexList.add(origin);
                vertexList.add(destination);
                edgeList.add(individual.getChosenConnections().get(0));
                paths = new Paths(vertexList, edgeList);
                individual.setPath(paths.getVertexPath());
                individual.setEdgePath(paths.getEdgePath());
                return individual;
            }
            edgeList = addChosenConnections(individual, createdPaths, edgeList);
            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size() - 1));
        } else {
            id = String.valueOf(individual.getId()) + "." + origin.getId() + "." + individual.getChosenConnections().get(0).getOrigin().getId();
            edgeList.addAll(createdPaths.get(id).getEdgePath());
            edgeList = addChosenConnections(individual, createdPaths, edgeList);
            edgeList.add(individual.getChosenConnections().get(individual.getChosenConnections().size() - 1));
        }
        vertexList = convertToVertexPath(edgeList);
        paths = new Paths(vertexList, edgeList);
        individual.setPath(paths.getVertexPath());
        individual.setEdgePath(paths.getEdgePath());
        return individual;
    }
    
    private List<Edge> addChosenConnections(Individual individual, Map<String, Paths> createdPaths, List<Edge> edgeList) {
        String id;
        for (int i = 0; i < individual.getChosenConnections().size() - 1; i++) {
            if (individual.getChosenConnections().get(i).getDestination().getId().equals(individual.getChosenConnections().get(i + 1).getOrigin().getId())) {
                edgeList.add(individual.getChosenConnections().get(i));
            } else {
                edgeList.add(individual.getChosenConnections().get(i));
                id = String.valueOf(individual.getId()) + "." + individual.getChosenConnections().get(i).getDestination().getId()
                             + "." + individual.getChosenConnections().get(i + 1).getOrigin().getId();
                edgeList.addAll(createdPaths.get(id).getEdgePath());
            }
        }
        return edgeList;
    }

    private List<Vertex> convertToVertexPath(List<Edge> edgePath) {
        List<Vertex> verticesPath = new ArrayList<>();
        for(Edge edge:edgePath){
            verticesPath.add(edge.getOrigin());
        }
        verticesPath.add(edgePath.get(edgePath.size()-1).getDestination());
        return verticesPath;
    }

}
