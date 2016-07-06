package sensors;

import interdroid.swancore.swansong.Expression;
import interdroid.swancore.swansong.SensorValueExpression;
import interdroid.swancore.swansong.ValueExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by goose on 15/06/16.
 */

public class SensorFactory {

    private static HashMap<String,SensorInterface> sensorList = new HashMap<String, SensorInterface>();

    public static SensorInterface getSensor(String entity){

        if(entity == null){
            return null;
        }

        if(sensorList.containsKey(entity)){
            return sensorList.get(entity);
        }

        else{

            String entityInCap = entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase();

            System.out.println("Sensor class name :"+ entityInCap);

            try {
                Class SensorClass = Class.forName("sensors."+entityInCap+"Sensor");

                Object sensorObject = SensorClass.newInstance();

                if (sensorObject instanceof SensorInterface){

                    sensorList.put(entity, (SensorInterface) sensorObject);
                    return (SensorInterface) sensorObject;

                }

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }




}
