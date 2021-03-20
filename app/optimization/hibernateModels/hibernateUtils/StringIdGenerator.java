package optimization.hibernateModels.hibernateUtils;

import optimization.hibernateModels.*;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class StringIdGenerator implements IdentifierGenerator {
    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        String stringId = object.toString();

        if(object.getClass().equals(Edge.class)) {
            stringId=((Edge) object).getEdge_id();
        }
        if(object.getClass().equals(Vertex.class)) {
            stringId=((Vertex) object).getId();
        }
        if(object.getClass().equals(Sensor.class)) {
            stringId=((Sensor) object).getSensor_id();
        }
        if(object.getClass().equals(Graph.class)) {
            stringId=((Graph) object).getGraph_id();
        }
        if(object.getClass().equals(Cluster.class)) {
            stringId=((Cluster) object).getCluster_id();
        }
        if (stringId!=null){
            return stringId;
        }
        return null;
    }
}

