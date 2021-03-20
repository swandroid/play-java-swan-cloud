package optimization.hibernateModels;


import com.vividsolutions.jts.geom.Coordinate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

@Entity
@Table(name="CLUSTER")
public class Cluster {

    public Cluster(){}


    @Id
    @GenericGenerator(name = "string_sequence", strategy = "optimization.hibernateModels.hibernateUtils.StringIdGenerator")
    @GeneratedValue(generator = "string_sequence")
    @Column(name = "cluster_id", nullable = false ,updatable = false)
    private String cluster_id;

    @Column(name = "sensors")
    private Set<String> clusterSensors =new HashSet<>();

    @Type(type = "org.hibernate.spatial.GeometryType")
    private Coordinate east;

    @Type(type = "org.hibernate.spatial.GeometryType")
    private Coordinate west;

    @Type(type = "org.hibernate.spatial.GeometryType")
    private Coordinate north;

    @Type(type = "org.hibernate.spatial.GeometryType")
    private Coordinate south;


    public String getCluster_id() {
        return cluster_id;
    }

    public void setCluster_id(String cluster_id) {
        this.cluster_id = cluster_id;
    }


    public Set<String> getClusterSensors() {
        return clusterSensors;
    }

    public void setClusterSensors(Set<String> clusterSensors) {
        this.clusterSensors = clusterSensors;
    }


    public Coordinate getEast() {
        return east;
    }

    public void setEast(Coordinate east) {
        this.east = east;
    }

    public Coordinate getWest() {
        return west;
    }

    public void setWest(Coordinate west) {
        this.west = west;
    }

    public Coordinate getNorth() {
        return north;
    }

    public void setNorth(Coordinate north) {
        this.north = north;
    }

    public Coordinate getSouth() {
        return south;
    }

    public void setSouth(Coordinate south) {
        this.south = south;
    }
}
