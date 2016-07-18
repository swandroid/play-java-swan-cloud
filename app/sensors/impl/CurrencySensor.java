package sensors.impl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import sensors.base.AbstractSwanSensor;

/**
 * Created by goose on 16/06/16.
 */
public class CurrencySensor extends AbstractSwanSensor {


    private static final String BASE_URL = "http://api.fixer.io/latest";

    private Map<String, CurrencyPoller> activeThreads = new HashMap<String, CurrencyPoller>();


    public class CurrencyPoller extends Thread{

        private HashMap configuration;
        private String valuePath;
        private String id;
        String url;

        CurrencyPoller(String id, String valuePath, HashMap configuration) {
            this.id = id;
            this.configuration = configuration;
            this.valuePath = valuePath;
        }



        public void run() {
            while (!isInterrupted()) {

                long now = System.currentTimeMillis();


                String from = "EUR";
                String to = "INR";

                if(configuration.containsKey("from") && configuration.containsKey("to")) {

                    from = (String) configuration.get("from");
                    to = (String) configuration.get("to");

                }
                else if(configuration.containsKey("from")){

                    from = (String) configuration.get("from");
                }
                else if(configuration.containsKey("to")){

                    to = (String) configuration.get("to");
                }

                url = String.format(BASE_URL + "?base=%s&symbols=%s", from,
                        to);

                String jsonData ="";

                try {
                    String line;
                    URLConnection conn = new URL(url).openConnection();
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = r.readLine()) != null) {
                        jsonData += line + "\n";
                    }


                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        putValueTrimSize(valuePath, id, now, jsonObject.getJSONObject("rates").getDouble(to));

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
                    Thread.sleep(Math.max(
                            0,
                            1000*60)); //need to change
                } catch (InterruptedException e) {
                    break;
                }

            }
        }



    }




    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        super.register(id,valuePath,configuration,httpConfiguration);

        CurrencyPoller currencyPoller = new CurrencyPoller(id, valuePath,
                configuration);
        activeThreads.put(id, currencyPoller);
        currencyPoller.start();

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

    }




    @Override
    public String[] getValuePaths() {
        return new String[]{"exchange"};
    }

    @Override
    public String getEntity() {
        return "currency";
    }

    @Override
    public String[] getConfiguration() {
        return new String[]{"from","to"};
    }


}
