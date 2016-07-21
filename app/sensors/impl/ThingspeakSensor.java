package sensors.impl;

import org.json.JSONArray;
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
public class ThingspeakSensor extends AbstractSwanSensor {

    private Map<String, ThingspeakSensor.ThingspeakPoller> activeThreads = new HashMap<String, ThingspeakPoller>();


    private static final String BASE_URL = "https://thingspeak.com/channels/%s/field/%s.json";


    class ThingspeakPoller extends SensorPoller {

        protected ThingspeakPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }

        String url;


        public void run() {
            while (!isInterrupted()) {

                long now = System.currentTimeMillis();

                String modifiedValuePath =null;

                if(configuration.containsKey("id") && configuration.containsKey("field")) {

                    url = String.format(BASE_URL, configuration.get("id"),configuration.get("field"));
                }
                else{

                    url = String.format(BASE_URL, "45572", "1");
                }



                if(valuePath.contentEquals("field")){
                    if(configuration.containsKey("field")) {
                        modifiedValuePath = "field" + configuration.get("field");
                    }
                    else{
                        modifiedValuePath = "field" +"1";
                    }
                }

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

                        if(valuePath.contains("field")) {

                            length = jsonObject.getJSONArray("feeds").length();
                            result = jsonObject.getJSONArray("feeds").getJSONObject(length-1).get(modifiedValuePath);
                        }
                        else{
                            result = jsonObject.getJSONObject("channel").get(valuePath);
                        }

                        System.out.println("dataaaaa "+result+ "length"+length);

                        updateResult(ThingspeakSensor.this,result,now);


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


        ThingspeakSensor.ThingspeakPoller thingspeakPoller = new ThingspeakSensor.ThingspeakPoller(id, valuePath,
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
        return new String[] {"field","created_at","updated_at","name","description"};
    }




    @Override
    public String getEntity() {
        return "thingspeak";
    }

    @Override
    public String[] getConfiguration() {
        return new   String[] {"id","field"};

    }
}
