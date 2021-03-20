package optimization.hibernateModels.hibernateUtils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class IntIdGenerator implements  IdentifierGenerator{
    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        System.out.println("OBJECT: "+object.toString());
        String stringId = object.toString();
        if (stringId!=""){
            return stringId;
        }
        return null;
    }
}

