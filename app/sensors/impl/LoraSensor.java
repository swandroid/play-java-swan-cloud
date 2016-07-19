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
 * Created by Roshan Bharath Das on 15/07/16.
 */
public class LoraSensor extends AbstractSwanSensor {

    private Map<String, LoraSensor.LoraPoller> activeThreads = new HashMap<String, LoraSensor.LoraPoller>();



    private static final String BASE_URL = "https://www.thethingsnetwork.org/api/v0/nodes/%s/";

    public static final String[] VALUEPATH = { "data",
                                            "gateway_eui",
                                            "frequency",
                                            "rssi",
                                            "time",
                                            "data_raw",
                                            "datarate",
                                            "node_eui",
                                            "snr",
                                            "data_plain",
                                            "data_json" };


    class LoraPoller extends SensorPoller {

        protected LoraPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }

        String url;


        public void run() {
            while (!isInterrupted()) {

                //System.out.println("Test poller running");

                long now = System.currentTimeMillis();



                if(configuration.containsKey("id")) {

                    url = String.format(BASE_URL, configuration.get("id"));
                }
                else{

                    url = String.format(BASE_URL, "09984508");
                }

                String jsonData ="";

                 //System.out.println("URL:"+url);
                try {
                    String line;
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    //conn.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
                    //HttpURLConnection.setFollowRedirects(true);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");


                    boolean redirect = false;



                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = r.readLine()) != null) {
                        jsonData += line + "\n";
                    }

                    try {
                        JSONArray jsonArray =new JSONArray(jsonData);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        //System.out.println(jsonObject);

                        updateResult(LoraSensor.this,jsonObject.get(valuePath),now);


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


         LoraPoller loraPoller = new LoraPoller(id, valuePath,
                configuration);
        activeThreads.put(id, loraPoller);
        loraPoller.start();

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

    }


    @Override
    public String[] getValuePaths() {
        return VALUEPATH;
    }

    @Override
    public String getEntity() {
        return "lora";
    }

    @Override
    public String[] getConfiguration() {
        return new String[]{"delay","id"};
    }
}
