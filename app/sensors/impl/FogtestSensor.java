package sensors.impl;

import org.json.JSONException;
import org.json.JSONObject;
import sensors.base.AbstractSwanSensor;
import sensors.base.SensorPoller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roshan Bharath Das on 06/03/2017.
 */
public class FogtestSensor extends AbstractSwanSensor {


    private Map<String, FogtestSensor.FogTestPoller> activeThreads = new HashMap<String, FogtestSensor.FogTestPoller>();


    public static final String VALUE = "value";


    class FogTestPoller extends SensorPoller {

        int i=0;
        ServerSocket server;
        Socket socket;
        ObjectInputStream ois;
        int port;

        FogTestPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);

                port =7784;
                if(configuration.containsKey("port")){
                    port = Integer.parseInt((String) configuration.get("port"));

                }

        }


        public void run() {

            try {
                server = new ServerSocket(port);
                socket = server.accept();
                ois = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }


            while (!isInterrupted()) {

                try {

                    //String message = (String) ois.readObject();

                    Object o = ois.readObject();
                    System.out.println("Read object: "+o);

                    // JSONObject json = new JSONObject(message);

                    // updateResult(FogtestSensor.this,json.get("data"),json.getLong("time"));

                    updateResult(FogtestSensor.this,(long)(Math.random()*3000),System.currentTimeMillis());


                } catch (IOException e) {
                    e.printStackTrace();
                }  catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }


        }


    }



    @Override
    public void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {

        super.register(id,valuePath,configuration,httpConfiguration);

        /*getValues().put(valuePath,
                Collections.synchronizedList(new ArrayList<TimestampedValue>()));*/
        FogtestSensor.FogTestPoller fogTestPoller = new FogtestSensor.FogTestPoller(id, valuePath,
                configuration);
        activeThreads.put(id, fogTestPoller);
        fogTestPoller.start();

    }

    @Override
    public void unregister(String id) {

        super.unregister(id);
        System.out.println("Unregister sensor called");
        activeThreads.remove(id).interrupt();

    }


    @Override
    public String[] getValuePaths()  {
        return new String[]{ "value","value0","value1"};
    }

    @Override
    public String getEntity() {
        return "fogtest";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay","port"};
    }


}
