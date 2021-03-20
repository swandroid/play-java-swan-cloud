package optimization.algorithm;

import com.vividsolutions.jts.geom.LineString;
import optimization.core.population.Individual;
import optimization.hibernateModels.Graph;
import optimization.hibernateModels.Vertex;

import java.util.HashMap;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class UserOptimizationData {

    private LineString route;
    private HashMap<String,Boolean> increaseObjectives;
    private Vertex origin;
    private Vertex destination;
    private long optimizationTime;
    private String token;
    private String id;
    private Graph userSkeletonGraph;
    private Individual elitist;

    public UserOptimizationData(Vertex origin, Vertex destination, LineString route, HashMap<String,Boolean> increaseObjectives, long optimizationTime, String token, String id){
        this.route=null;
        this.increaseObjectives=increaseObjectives;
        this.origin =origin;
        this.destination=destination;
        this.optimizationTime= optimizationTime;
        this.token=token;
        this.id=id;
    }

    public LineString getRoute() {
        return route;
    }

    public void setRoute(LineString route) {
        this.route = route;
    }

    public Vertex getOrigin() {
        return origin;
    }

    public void setOrigin(Vertex origin) {
        this.origin = origin;
    }

    public Vertex getDestination() {
        return destination;
    }

    public void setDestination(Vertex destination) {
        this.destination = destination;
    }

    public long getOptimizationTime() {
        return optimizationTime;
    }

    public void setOptimizationTime(long optimizationTime) {
        this.optimizationTime = optimizationTime;
    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, Boolean> getIncreaseObjectives() {
        return increaseObjectives;
    }

    public void setIncreaseObjectives(HashMap<String, Boolean> increaseObjectives) {
        this.increaseObjectives = increaseObjectives;
    }

    public Graph getUserSkeletonGraph() {
        return userSkeletonGraph;
    }

    public void setUserSkeletonGraph(Graph userSkeletonGraph) {
        this.userSkeletonGraph = userSkeletonGraph;
    }
    
    public Individual getElitist() {
        return elitist;
    }
    
    public void setElitist(Individual elitist) {
        this.elitist = elitist;
    }
}
