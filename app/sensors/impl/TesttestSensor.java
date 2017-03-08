package sensors.impl;

import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roshan Bharath Das on 08/03/2017.
 */
public class TesttestSensor extends AbstractSwanSensor {


    private Map<String, TestPoller> activeThreads = new HashMap<String, TestPoller>();


    public static final String VALUE = "value";


    class TestPoller extends SensorPoller {

        int i=2;


        TestPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }


        public void run() {
            while (!isInterrupted()) {

                //System.out.println("Test poller running");

                long now = System.currentTimeMillis();


                if(i==2){
                        i=3;
                }else{
                    i=2;
                }
                System.out.println("DELAY="+DELAY+ " I value="+i);

                updateResult(TesttestSensor.this,i,now);

                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    break;
                }

            }
        }


    }



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        super.register(id,valuePath,configuration,httpConfiguration);

        /*getValues().put(valuePath,
                Collections.synchronizedList(new ArrayList<TimestampedValue>()));*/
        TestPoller testPoller = new TestPoller(id, valuePath,
                configuration);
        activeThreads.put(id, testPoller);
        testPoller.start();

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

    }


    @Override
    public String[] getValuePaths()  {
        return new String[]{ VALUE};
    }

    @Override
    public String getEntity() {
        return "testtest";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay"};
    }


}
