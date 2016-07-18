package sensors.impl;

import sensors.base.AbstractSwanSensor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by goose on 13/06/16.
 */
public class TestSensor extends AbstractSwanSensor {


    private Map<String, TestPoller> activeThreads = new HashMap<String, TestPoller>();



    public static final String VALUE = "value";


    class TestPoller extends Thread {

        private HashMap configuration;
        private String valuePath;
        private String id;

        TestPoller(String id, String valuePath, HashMap configuration) {
            this.id = id;
            this.configuration = configuration;
            this.valuePath = valuePath;
        }

        public void run() {
            while (!isInterrupted()) {

                //System.out.println("Test poller running");

                long now = System.currentTimeMillis();


                putValueTrimSize(valuePath,id,now, ThreadLocalRandom.current().nextInt(0, 1 + 1));


                //System.out.println("test poller before sleep");
                try {
                    Thread.sleep(Math.max(
                            0,
                            3000)); //need to change
                } catch (InterruptedException e) {
                    break;
                }
                //System.out.println("test poller sleep done");
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
        return "test";
    }

    @Override
    public String[] getConfiguration() {
        return null;
    }


}
