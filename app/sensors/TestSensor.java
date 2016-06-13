package sensors;

import engine.EvaluationEngineService;
import swansong.TimestampedValue;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by goose on 13/06/16.
 */
public class TestSensor implements SensorInterface {


    private Map<String, TestPoller> activeThreads = new HashMap<String, TestPoller>();

    public static final String VALUE = "value";

    private final Map<String, List<TimestampedValue>> values = new HashMap<String, List<TimestampedValue>>();

    String valuePath;

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

                System.out.println("Test poller running");

                long now = System.currentTimeMillis();


                putValueTrimSize(valuePath,id,now, ThreadLocalRandom.current().nextInt(0, 1 + 1));


                System.out.println("test poller before sleep");
                try {
                    Thread.sleep(Math.max(
                            0,
                            1000)); //need to change
                } catch (InterruptedException e) {
                    break;
                }
                System.out.println("test poller sleep done");
            }
        }


    }



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        this.valuePath = valuePath;
        getValues().put(valuePath,
                Collections.synchronizedList(new ArrayList<TimestampedValue>()));
        TestPoller testPoller = new TestPoller(id, valuePath,
                configuration);
        activeThreads.put(id, testPoller);
        testPoller.start();

    }

    @Override
    public void unregister(String id) {

        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

    }

    @Override
    public List<TimestampedValue> getValues(String id, long now, long timespan) {
        if(valuePath!=null) {

            //System.out.println("Timestamped value rain sensor: "+getValues().get(valuePath));
            return getValues().get(valuePath);
        }
        return null;
    }

    public final Map<String, List<TimestampedValue>> getValues() {
        return values;
    }

    @Override
    public String[] getValuePaths()  {
        return new String[]{ VALUE};
    }

    @Override
    public void onDestroySensor() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public long getStartUpTime(String id) {
        return 0;
    }

    @Override
    public double getAverageSensingRate() {
        return 0;
    }



    protected final void putValueTrimSize(final String valuePath,
                                          final String id, final long now, final Object value /*, final int historySize*/) {


        System.out.println("putValueTrimSize:"+value);
        try {
            getValues().get(valuePath).add(new TimestampedValue(value, now));
        } catch (OutOfMemoryError e) {
            onDestroySensor();
        }


        if (id != null) {
            notifyDataChangedForId(id);
        }// else {
        //    notifyDataChanged(valuePath);
        // }



    }


    protected final void notifyDataChangedForId(final String... ids) {
        // Intent notifyIntent = new Intent(ACTION_NOTIFY);
        //notifyIntent.putExtra("expressionIds", ids);
        //sendBroadcast(notifyIntent);

        EvaluationEngineService instance = EvaluationEngineService.getInstance();
        instance.doNotify(ids);

    }




}
