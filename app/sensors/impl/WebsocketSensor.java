package sensors.impl;

import akka.stream.javadsl.Flow;
import play.mvc.WebSocket;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Roshan Bharath Das on 26/05/2017.
 */
public class WebsocketSensor extends AbstractSwanSensor {



    public static final String VALUE = "value";

    String id;
    String valuePath;
    long now;


    public void updateResult(String a){

        System.out.println("Result:"+a);

        now = System.currentTimeMillis();

        putValueTrimSize(valuePath, id, now, a);

    }



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        super.register(id,valuePath,configuration,httpConfiguration);

        this.id = id;
        this.valuePath =valuePath;
        /*getValues().put(valuePath,
                Collections.synchronizedList(new ArrayList<TimestampedValue>()));*/

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");

    }


    @Override
    public String[] getValuePaths()  {
        return new String[]{ VALUE};
    }

    @Override
    public String getEntity() {
        return "websocket";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay"};
    }


}
