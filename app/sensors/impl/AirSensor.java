package sensors.impl;

import controllers.OptimizationController;
import interdroid.swancore.swansong.TimestampedValue;
import optimization.jsonParser.AirPollutionDublinParser;
import org.json.JSONException;
import org.json.JSONObject;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Maria Efthymiadou on 15/01/19.
 */
public class AirSensor extends AbstractSwanSensor {
    
    private static final String BASE_URL="https://api.breezometer.com/forecast/?lat=%s&lon=%s&hours=48&key=API_KEY";

    // private static final String URL ="https://api.breezometer.com/baqi/?lat=53.312702&lon=-6.282871&hours=248&start_datetime=2018-11-01T10:00:00&end_datetime=2018-11-14T10:00:00&key=API_KEY";

    private final Map<String, List<TimestampedValue>> values = new HashMap<String, List<TimestampedValue>>();

    private Map<String, AirPoller> activeThreads = new HashMap<String, AirPoller>();
    
    public static final String POLLUTION = "pollution";

    public AirSensor() {
        super();
    }

    
    class AirPoller extends SensorPoller {

        protected AirPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);

        }

        //update the data from a file
        public void run() {
            String file = (String)configuration.get("file");
            AirPollutionDublinParser airPollutionDublinParser = new AirPollutionDublinParser(file);
            List<Double> concentration = null;
            try {
                airPollutionDublinParser.parse();
                concentration=airPollutionDublinParser.getConcentration();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                airPollutionDublinParser.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int i=0;
            while (!isInterrupted()) {
                long now = System.currentTimeMillis();
                if(OptimizationController.optimizationControllerData.maxSensorValues.containsKey("air")){
                    if(OptimizationController.optimizationControllerData.maxSensorValues.get("air")<concentration.get(i))

                        OptimizationController.optimizationControllerData.maxSensorValues.put("air",concentration.get(i));
                }
                updateMap(sensors.impl.AirSensor.this,concentration.get(i),now, OptimizationController.optimizationControllerData.sensorValues);
                try {
                    Thread.sleep(DELAY);
                    i++;
                    if(i==concentration.size())
                        i=0;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    
    
        //update the data from an endpoint
        
//        public void run() {
//            while (!isInterrupted()) {
//
//                long now = System.currentTimeMillis();
//                String[] jsonValues = new String[0];
//                String url = String.format(BASE_URL,configuration.get("latitude"),configuration.get("longitude"));
//                //System.out.println("URL: "+url);
//                StringBuffer response = new StringBuffer();
//
//                try {
//                    URLConnection conn = new URL(url).openConnection();
//                    BufferedReader r = new BufferedReader(new InputStreamReader(
//                            conn.getInputStream()));
//                    String line;
//                    while ((line = r.readLine()) != null) {
//                        response.append(line);
//                        //System.out.println("line: "+ line);
//                    }
//                    JSONObject jsonObject = null;
//                    try {
//                        jsonObject =new JSONObject(response.toString());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        jsonValues = jsonObject.get("aleq").toString().split(",");
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
////                    updateMap(sensors.impl.AirSensor.this,Double.parseDouble(jsonValues[jsonValues.length-1].substring(1,6)),now, OptimizationController.optimizationControllerData.sensorValues);
//                    updateResult(sensors.impl.AirSensor.this,Double.parseDouble(jsonValues[jsonValues.length-1].substring(1,6)),now);
//
//                } catch (MalformedURLException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                } catch (IOException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//
//                try {
//                    Thread.sleep(DELAY);
//                } catch (InterruptedException e) {
//                    break;
//                }
//            }
//        }

    }


    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {
        
        super.register(id,valuePath,configuration,httpConfiguration);
        AirSensor.AirPoller airPoller = new AirSensor.AirPoller(id, valuePath,
                configuration);
        activeThreads.put(id, airPoller);
        airPoller.start();
    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        activeThreads.remove(id).interrupt();
    }

    @Override
    public String[] getValuePaths() {
        return new String[]{ POLLUTION};
    }

    @Override
    public String getEntity() {
        return "air";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay","latitude","longitude","file"};
    }

}
