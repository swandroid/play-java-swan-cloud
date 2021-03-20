package optimization.hibernateModels;

import com.vividsolutions.jts.geom.Coordinate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

@Entity
@Table(name="Edge")
public class Edge implements Serializable {

    @Id
    @GenericGenerator(name = "string_sequence", strategy = "hibernateUtils.StringIdGenerator")
    @GeneratedValue(generator = "string_sequence")
    @Column(name = "edge_id", nullable = false ,updatable = false)
    private String edge_id;

   private Map<String,Double> weights = new HashMap<>();

    public Edge() {
    }

    @ManyToOne
    @JoinColumn(name ="sensor",referencedColumnName="sensor_id")
    private Sensor sensor;

    private Integer cluster;

    public String getEdge_id() {
        return edge_id;
    }

    public void setEdge_id(String edge_id) {
        this.edge_id = edge_id;
    }

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @JoinColumn(name ="origin",referencedColumnName="vertex_id")
    private Vertex origin;

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @JoinColumn(name ="destination",referencedColumnName="vertex_id")
    private Vertex destination;

    private Double distance;

    private Coordinate middlePoint;


    public Edge(String idd) {
        this.edge_id = idd;
        this.distance = Double.valueOf(0);
    }

    public Edge(String s, Vertex origin, Vertex destination, double i) {
        this.edge_id = s;
        this.distance = i;
        this.origin=origin;
        this.destination=destination;
    }

    public Edge(String s, Vertex origin, Vertex destination, double i, Integer edgeCluster) {
        this.edge_id = s;
        this.distance =  origin.getCoordinate().distance(destination.getCoordinate());
        this.origin=origin;
        this.destination=destination;
        this.cluster=cluster;
    }

    public Edge(String s, Vertex origin, Vertex destination, HashMap<String,Double> weights, Integer edgeCluster) {
        this.edge_id = s;
        this.origin=origin;
        this.destination=destination;
        this.cluster=cluster;
        this.weights=weights;
        this.distance =  origin.getCoordinate().distance(destination.getCoordinate());

    }

    public Vertex getDestination() {
        return this.destination;
    }

    public Vertex getOrigin() {
        return origin;
    }

    public void setOrigin(Vertex origin) {
        this.origin = origin;
    }

    public void setDestination(Vertex destination) {
        this.destination = destination;
    }

    public Coordinate getMiddlePoint() {
        return middlePoint;
    }

    public void setMiddlePoint(Coordinate middlePoint) {
        this.middlePoint = middlePoint;
    }


    public double getWeight(String objective) {
        if(getWeights().containsKey(objective))
            return getWeights().get(objective);
        return 0;
    }

    public void setWeight(String objective,Double value) {
        getWeights().put(objective,value);
    }

    public Map<String, Double> getWeights() {
        return weights;
    }

    public void setWeights(Map<String, Double> weights) {
        this.weights = weights;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if (edge_id == null) {
            if (other.edge_id != null)
                return false;
        } else if (!edge_id.equals(other.edge_id))
            return false;
        return true;
    }
    
    public Integer getCluster() {
        return cluster;
    }
    
    public void setCluster(Integer cluster) {
        this.cluster = cluster;
    }
}
