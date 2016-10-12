package sensors.impl;

import org.json.JSONException;
import org.json.JSONObject;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roshan Bharath Das on 20/07/16.
 */
public class ThingspeaktestSensor extends AbstractSwanSensor {

    private Map<String, ThingspeaktestSensor.ThingspeakPoller> activeThreads = new HashMap<String, ThingspeakPoller>();


    //private static final String BASE_URL = "https://thingspeak.com/channels/%s/field/1.json";
    private static final String BASE_URL = "http://fs0.das5.cs.vu.nl:3000/channels/%s/field/1.json";

    class ThingspeakPoller extends SensorPoller {

        String url;
        protected ThingspeakPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);

            int ascii = (int)valuePath.charAt(0);
            int channel_id = ascii - 91;
            if(valuePath.contentEquals("n")){
                channel_id = 13;
            }

            url = String.format(BASE_URL, String.valueOf(channel_id));
//            switch (valuePath) {
//                case "a":
//                    break;
//
//            }

        }




        public void run() {
            while (!isInterrupted()) {

                long now = System.currentTimeMillis();

                String jsonData ="";

                try {
                    String line;
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");



                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = r.readLine()) != null) {
                        jsonData += line + "\n";
                    }


                    System.out.println(jsonData);

                    try {

                        JSONObject jsonObject = new JSONObject(jsonData);

                        Object result = null;
                        int length=0;


                        length = jsonObject.getJSONArray("feeds").length();
                        result = jsonObject.getJSONArray("feeds").getJSONObject(length-1).get("field1");


                        System.out.println("dataaaaa "+result+ "length"+length +"valuepath"+valuePath);

                        updateResult(ThingspeaktestSensor.this,Float.parseFloat((String) result),now);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

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


        ThingspeaktestSensor.ThingspeakPoller thingspeakPoller = new ThingspeaktestSensor.ThingspeakPoller(id, valuePath,
                configuration);
        activeThreads.put(id, thingspeakPoller);
        thingspeakPoller.start();

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

    }




    @Override
    public String[] getValuePaths() {
        return new String[] {"34247","6","a","b","c","d","e","f","g","n","i","j","k"};
    }




    @Override
    public String getEntity() {
        return "thingspeaktest";
    }

    @Override
    public String[] getConfiguration() {
        return new   String[] {"delay"};

    }
}
