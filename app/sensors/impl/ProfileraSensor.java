package sensors.impl;

import org.json.JSONException;
import org.json.JSONObject;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roshan Bharath Das on 07/10/16.
 */
public class ProfileraSensor extends AbstractSwanSensor{
    //Test case is if x>1 caseScenario 0 is worst and caseScenario 1 is best

    private Map<String, ProfileraSensor.ProfilerPoller> activeThreads = new HashMap<String, ProfileraSensor.ProfilerPoller>();


    public static final String VALUE = "value";


    class ProfilerPoller extends SensorPoller {

        int i = 0;

        int[] intArray = new int[4];
        int caseScenario = 0;

        //private static final String BASE_URL = "http://gps.buienradar.nl/getrr.php?lat=52.3&lon=4.87";
        private static final String BASE_URL = "https://thingspeak.com/channels/45572/field/3.json/";

        ProfilerPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }


        public void run() {

            intArray[0] = 0;
            intArray[1] = 1;
            intArray[2] = 2;
            intArray[3] = 3;

            if (configuration.containsKey("case")) {
                caseScenario = Integer.parseInt((String) configuration.get("case"));
            }

            while (!isInterrupted()) {

                long now = System.currentTimeMillis();

                String jsonData = "";

                try {
                    String line;
                    URLConnection conn = new URL(BASE_URL).openConnection();
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    // String line = r.readLine();

                    while ((line = r.readLine()) != null) {
                        jsonData += line + "\n";
                    }


                    JSONObject jsonObject = new JSONObject(jsonData);
                    Object result = null;
                    int length = 0;

                    length = jsonObject.getJSONArray("feeds").length();
                    result = jsonObject.getJSONArray("feeds").getJSONObject(length - 1).get("field3");


                    if (caseScenario == 0) {
                        if (i == 0) {
                            i = 2;
                        } else {
                            i = 0;
                        }
                    } else {
                        if (i == 0) {
                            i = 1;
                        } else {
                            i = 0;
                        }

                    }

                    System.out.println("DELAY=" + DELAY + " I value=" + i);

                    updateResult(ProfileraSensor.this, i, now);

                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

    }
    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        super.register(id,valuePath,configuration,httpConfiguration);

        ProfileraSensor.ProfilerPoller profilerPoller = new ProfileraSensor.ProfilerPoller(id, valuePath,
                configuration);
        activeThreads.put(id, profilerPoller);
        profilerPoller.start();

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
        return "profilera";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay","case"};
    }


}