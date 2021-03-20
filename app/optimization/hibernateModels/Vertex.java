package optimization.hibernateModels;

import com.vividsolutions.jts.geom.Coordinate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

@Entity
@Table(name="Vertex")
public class Vertex implements Serializable{

    private static final long serialVersionUID = 8772730344495138933L;

    @Id
    @GenericGenerator(name = "string_sequence", strategy = "hibernateUtils.StringIdGenerator")
    @GeneratedValue(generator = "string_sequence")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @Column(name = "vertex_id", nullable = false)
    private String vertex_id;

    private String location;

    @Type(type = "org.hibernate.spatial.GeometryType")
    private Coordinate coordinate;
    
    private Integer cluster;

    public Vertex(String id) {
        this.vertex_id = id;
    }


    public Vertex(String id,Coordinate coordinate) {
        this.vertex_id = id;
        this.location = null;
        this.coordinate=coordinate;
    }



    public Vertex(String id, String location,Coordinate coordinate) {
        this.vertex_id = id;
        this.location = location;
        this.coordinate = coordinate;
    }

    public Vertex(String id, String location,Coordinate coordinate,Integer cluster) {
        this.vertex_id = id;
        this.location = location;
        this.coordinate = coordinate;
        this.cluster = cluster;
    }
    public Vertex() {

    }
    
    
    public String getVertex_id() {
        return vertex_id;
    }
    
    public void setVertex_id(String vertex_id) {
        this.vertex_id = vertex_id;
    }


    public String getId() {
        return vertex_id;
    }

    public void setLocation(String location){
        this.location=location;
    }

    public String getLocation(){
        return  this.location;
    }

    public void setId(String id) {
        this.vertex_id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vertex_id == null) ? 0 : vertex_id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex) obj;
        if (vertex_id == null) {
            if (other.vertex_id != null)
                return false;
        } else if (!vertex_id.equals(other.vertex_id))
            return false;
        return true;
    }



    public String toString(){
        //return (this.id+" "+this.location.toString());
        return (this.vertex_id);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }


    public Integer getCluster() {
        return cluster;
    }

    public void setCluster(Integer cluster) {
        this.cluster = cluster;
    }

}

