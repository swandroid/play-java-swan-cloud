package sensors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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





    class LoraPoller extends Thread {

        private HashMap configuration;
        private String valuePath;
        private String id;

        LoraPoller(String id, String valuePath, HashMap configuration) {
            this.id = id;
            this.configuration = configuration;
            this.valuePath = valuePath;
        }

        public void run() {
            while (!isInterrupted()) {

                //System.out.println("Test poller running");

                long now = System.currentTimeMillis();


                String url;
                if(configuration.containsKey("id")) {

                    url = String.format(BASE_URL, configuration.get("id"));
                }
                else{

                    url = String.format(BASE_URL, "E77E0008");
                }

                String jsonData ="";

                 System.out.println("URL:"+url);
                try {
                    String line;
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    //conn.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
                    //HttpURLConnection.setFollowRedirects(true);
                    conn.setRequestProperty("Content-Type", "application/json");
                    
                    boolean redirect = false;

                    int status = conn.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        if (status == HttpURLConnection.HTTP_MOVED_TEMP
                                || status == HttpURLConnection.HTTP_MOVED_PERM
                                || status == HttpURLConnection.HTTP_SEE_OTHER)
                            redirect = true;
                    }

                    if (redirect) {

                        // get redirect url from "location" header field
                        String newUrl = conn.getHeaderField("Location");
                        // open the new connnection again
                        System.out.println("New URL:"+newUrl);

                        conn = (HttpURLConnection) new URL(newUrl).openConnection();

                    }



                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = r.readLine()) != null) {
                        jsonData += line + "\n";
                    }
                    System.out.println(jsonData);


                    try {
                        JSONArray jsonArray =new JSONArray(jsonData);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        System.out.println(jsonObject);



                        //putValueTrimSize(valuePath, id, now, jsonObject.getJSONObject(valuePath).getDouble(to));

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
}
