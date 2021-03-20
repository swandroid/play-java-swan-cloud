package optimization.hibernateModels;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

@Entity
@Table(name="SENSOR")
public class Sensor implements Serializable {
    private static final long serialVersionUID = -7226183275304749281L;
    public Sensor(){}


    @Id
    @GenericGenerator(name = "string_sequence", strategy = "optimization.hibernateModels.hibernateUtils.StringIdGenerator")
    @GeneratedValue(generator = "string_sequence")
    @Column(name = "sensor_id", nullable = false ,updatable = false)
    private String sensor_id;

    @Column(name = "SensorType")
    private String sensorType;

    private String sensorUrl;

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getSensorUrl() {
        return sensorUrl;
    }

    public void setSensorUrl(String sensorUrl) {
        this.sensorUrl = sensorUrl;
    }

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }
    
}
