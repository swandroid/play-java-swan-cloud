package sensors.base;

import org.reflections.Reflections;
import sensors.base.SensorInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

           // System.out.println("Sensor class name :"+ entityInCap);

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



    public static List<SensorInterface> getAllSensors(){


            List<SensorInterface> sensorInterfaces = new ArrayList<SensorInterface>();

            Reflections reflections = new Reflections("sensors.impl");

            Set<Class<? extends AbstractSwanSensor>> allClasses = reflections.getSubTypesOf(AbstractSwanSensor.class);

            for(Class className : allClasses){

                //System.out.println(className.getSimpleName());
                try {
                    Object sensorObject = className.newInstance();

                    if (sensorObject instanceof SensorInterface){

                        sensorInterfaces.add((SensorInterface) sensorObject);

                    }

                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }


        //System.out.println(sensorInterfaces.get(0).getEntity()+sensorInterfaces.get(1).getEntity()+sensorInterfaces.get(2).getEntity()+sensorInterfaces.get(3).getEntity());
        return sensorInterfaces;
    }






}
