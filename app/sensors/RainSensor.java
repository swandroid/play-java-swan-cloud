package sensors;

import credentials.Firebase;
import engine.EvaluationEngineService;
import services.WebService;
import swansong.TimestampedValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by goose on 01/06/16.
 */
public class RainSensor implements SensorInterface {

    private static final String BASE_URL = "http://gps.buienradar.nl/getrr.php?lat=%s&lon=%s";

    WebService webService;

    private final Map<String, List<TimestampedValue>> values = new HashMap<String, List<TimestampedValue>>();

    private Map<String, RainPoller> activeThreads = new HashMap<String, RainPoller>();

    public static final String EXPECTED_MM = "expected_mm";


    String valuePath;

    class RainPoller extends Thread {

        private HashMap configuration;
        private String valuePath;
        private String id;

        RainPoller(String id, String valuePath, HashMap configuration) {
            this.id = id;
            this.configuration = configuration;
            this.valuePath = valuePath;
        }

        public void run() {
            while (!isInterrupted()) {

                System.out.println("Rain poller running");

                long start = System.currentTimeMillis();

                String url = String.format(BASE_URL, configuration.get("latitude"),
                        configuration.get("longitude"));


                try {
                    URLConnection conn = new URL(url).openConnection();
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String line = r.readLine();
                    float value = convertValueToMMPerHr(Integer.parseInt(line.substring(0, 3)));
                    putValueTrimSize(valuePath, id, start, value);

                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                System.out.println("Rain poller before sleep");
                try {
                    Thread.sleep(Math.max(
                            0,
                            5000)); //need to change
                } catch (InterruptedException e) {
                    break;
                }
                System.out.println("Rain poller sleep done");
            }
        }

        private float convertValueToMMPerHr(int value) {
            float result = (float) (Math.pow(10, (value - 109) / 32.0));
            return result;
        }

    }

    private float convertValueToMMPerHr(int value) {
        float result = (float) (Math.pow(10, (value - 109) / 32.0));
        return result;
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



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {


        this.valuePath = valuePath;
        getValues().put(valuePath,
                Collections.synchronizedList(new ArrayList<TimestampedValue>()));
        RainPoller rainPoller = new RainPoller(id, valuePath,
                configuration);
        activeThreads.put(id, rainPoller);
        rainPoller.start();

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
    public String[] getValuePaths() {
        return new String[]{ EXPECTED_MM};
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






}