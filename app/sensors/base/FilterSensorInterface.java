package sensors.base;

import java.util.ArrayList;

/**
 * Created by Maria Efthymiadou on 15/01/19.
 */
public interface FilterSensorInterface {


    Object filterSensorValueByDistance(String valuePath, String id, long start, Object previousValue, Object newValue, Object distance);

    Object filterSensorValueByCategory(String valuePath, String id, long start, ArrayList categories, Object previousValue, Object newValue);

    Object distance(Object previousValue, Object newValue);

}

