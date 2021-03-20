package sensors.impl;

import controllers.OptimizationController;
import interdroid.swancore.swansong.TimestampedValue;
import optimization.jsonParser.DublinCityNoiseParser;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Maria Efthymiadou on 15/01/19.
 */
public class SoundSensor extends AbstractSwanSensor {

    //private static final String BASE_URL="http://dublincitynoise.sonitussystems.com/applications/api/dublinnoisedata.php?location=";

    private static final String BASE_URL = "http://dublincitynoise.sonitussystems.com/applications/api/dublinnoisedata.php?location=%s&start=%s&end=%s";

    private final Map<String, List<TimestampedValue>> values = new HashMap<String, List<TimestampedValue>>();

    private Map<String, SensorPoller> activeThreads = new HashMap<String, SensorPoller>();


    public static final String DECIBEL = "decibel";

    public SoundSensor() {
        super();
    }



    class SoundPoller extends SensorPoller {

        protected SoundPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
        }

        List<Double> soundValues = new ArrayList<>();
        
        //update the data from a file
        public void run() {

            DublinCityNoiseParser dublinCityNoiseParser = new DublinCityNoiseParser((String) configuration.get("file"));
            try {
                dublinCityNoiseParser.parseFile();
                soundValues = dublinCityNoiseParser.parseFile();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
           // System.out.println("print the values: ");
            for(Double value:soundValues){
                System.out.println(value);
            }
            //System.out.println("soundValues size: "+soundValues.size());
            long now;
            int i=0;
            while (!isInterrupted()) {
                now = System.currentTimeMillis();
                StringBuffer response = new StringBuffer();
//                    updateMap(sensors.impl.SoundSensor.this,soundValues.get(i),now, OptimizationController.optimizationControllerData.sensorValues);
                if(OptimizationController.optimizationControllerData.maxSensorValues.containsKey("sound")) {
                    if (OptimizationController.optimizationControllerData.maxSensorValues.get("sound") < soundValues.get(i))
                        OptimizationController.optimizationControllerData.maxSensorValues.put("sound", soundValues.get(i));
                }
                updateMap(sensors.impl.SoundSensor.this,soundValues.get(i),now, OptimizationController.optimizationControllerData.sensorValues);


                try {
                    Thread.sleep(DELAY);
                    i++;
                    if(i==soundValues.size())
                        i=0;
                } catch (InterruptedException e) {
                    break;
                }

            }
            
            //update the data from an endpoint

//            while (!isInterrupted()) {
//                String[] jsonValues = new String[0];
//                now = System.currentTimeMillis();
//                long unixTimestamp = Instant.now().getEpochSecond();
//                String url = String.format(BASE_URL, this.getID(), unixTimestamp - 300, unixTimestamp);
//                //System.out.println("URL: "+url);
//                StringBuffer response = new StringBuffer();
//
//                try {
//                    URLConnection conn = new URL(url).openConnection();
//                    BufferedReader r = new BufferedReader(new InputStreamReader(
//                            conn.getInputStream()));
//                    String line;
//                    while ((line = r.readLine()) != null) {
//                          response.append(line);
//                          //System.out.println("line: "+ line);
//                      }
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
//                    updateMap(sensors.impl.SoundSensor.this,Double.parseDouble(jsonValues[jsonValues.length-1].substring(1,6)),now, OptimizationController.optimizationControllerData.sensorValues);
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
//
//            }
        }


    }
    

    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

       // System.out.println("id= "+id);
        super.register(id,valuePath,configuration,httpConfiguration);
        SoundSensor.SoundPoller soundPoller = new SoundSensor.SoundPoller(id, valuePath,
                configuration);
        activeThreads.put(id, soundPoller);
        soundPoller.start();
    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        activeThreads.remove(id).interrupt();
    }

    @Override
    public String[] getValuePaths() {
        return new String[]{ DECIBEL};
    }

    @Override
    public String getEntity() {
        return "sound";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay","file"};
    }
}
