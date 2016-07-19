package sensors.impl;

import sensors.base.AbstractSwanSensor;
import interdroid.swancore.swansong.TimestampedValue;
import sensors.base.SensorPoller;

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
public class RainSensor extends AbstractSwanSensor {

    private static final String BASE_URL = "http://gps.buienradar.nl/getrr.php?lat=%s&lon=%s";

    private final Map<String, List<TimestampedValue>> values = new HashMap<String, List<TimestampedValue>>();

    private Map<String, RainPoller> activeThreads = new HashMap<String, RainPoller>();

    public static final String EXPECTED_MM = "expected_mm";


    class RainPoller extends SensorPoller {


        protected RainPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }


        public void run() {
            while (!isInterrupted()) {


                long now = System.currentTimeMillis();

                String url = String.format(BASE_URL, configuration.get("latitude"),
                        configuration.get("longitude"));


                try {
                    URLConnection conn = new URL(url).openConnection();
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String line = r.readLine();

                    updateResult(RainSensor.this,convertValueToMMPerHr(Integer.parseInt(line.substring(0, 3))),now);

                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                //System.out.println("Rain poller before sleep");
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    break;
                }
                //System.out.println("Rain poller sleep done");
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



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        super.register(id,valuePath,configuration,httpConfiguration);

           RainPoller rainPoller = new RainPoller(id, valuePath,
                configuration);
        activeThreads.put(id, rainPoller);
        rainPoller.start();

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);

        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();


    }


    @Override
    public String[] getValuePaths() {
        return new String[]{ EXPECTED_MM};
    }

    @Override
    public String getEntity() {
        return "rain";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay","latitude","longitude"};
    }


}