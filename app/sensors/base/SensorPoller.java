package sensors.base;

import java.util.HashMap;

/**
 * Created by Roshan Bharath Das on 19/07/16.
 */
public class SensorPoller extends Thread {


    protected HashMap configuration;
    protected String valuePath;
    private String id;
    String url;

    Object previousValue=null;
    Object currentValue;

    protected long DELAY = 1000;


    protected SensorPoller(String id, String valuePath, HashMap configuration){

        this.id = id;
        this.configuration = configuration;
        this.valuePath = valuePath;

        if(configuration.containsKey("delay")) {
            DELAY = Long.parseLong((String) configuration.get("delay"));
        }

    }




    public void updateResult(AbstractSwanSensor abstractSwanSensor, Object currentValue, long now){


        if(valueChange(previousValue,currentValue)) {

            abstractSwanSensor.putValueTrimSize(valuePath, id, now, currentValue);
        }
        previousValue = currentValue;

    }


    protected boolean valueChange(Object previousValue, Object value){

        if(previousValue==null || !previousValue.equals(value)){
            return true;

        }

        return false;
    }


}
