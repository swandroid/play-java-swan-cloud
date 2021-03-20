package optimization.hibernateModels.hibernateUtils;

import com.vividsolutions.jts.geom.GeometryFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class HibernateUtil {


    private static final SessionFactory sessionFactory;
    private static final GeometryFactory geometryFactory;

    static {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml)
            // config file.
            sessionFactory = new Configuration()
                    .configure() // configures settings from hibernate.cfg.xml
                    .buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception.
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    static {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml)
            // config file.
            geometryFactory = new GeometryFactory();
        } catch (Throwable ex) {
            // Log the exception.
            System.err.println("Initial GeometryFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }
}
