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

        FogTestPoller(String id, String valuePath, HashMap configuration) {
            super(id, valuePath, configuration);
            try {
                server = new ServerSocket(6789);
                socket = server.accept();
                ois = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void run() {
            while (!isInterrupted()) {

                System.out.println("Starting fog test sensor");

                try {

                    System.out.println("Accept is done");

                    String message = (String) ois.readObject();

                    try {
                        JSONObject json = new JSONObject(message);

                        //System.out.println("message json: "+json.getString("valuepath"));
                        //System.out.println("message json: "+message);

                        updateResult(FogtestSensor.this,json.get("data"),json.getLong("time"));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }





                } catch (IOException e) {
                    e.printStackTrace();
                }  catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                //System.out.println("Test poller running");

               // long now = System.currentTimeMillis();

                //i ^= 1;

                //System.out.println("DELAY="+DELAY+ " I value="+i);

                //updateResult(FogtestSensor.this,i,now);

              /*  try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    break;
                }
               */
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
        return new String[]{ VALUE};
    }

    @Override
    public String getEntity() {
        return "fogtest";
    }

    @Override
    public String[] getConfiguration() {
        return new String[] {"delay"};
    }


}
