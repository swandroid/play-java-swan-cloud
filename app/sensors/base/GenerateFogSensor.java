package sensors.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Created by Roshan Bharath Das on 07/03/2017.
 */
public class GenerateFogSensor {

    public static final String VALUEPATH = "valuepath";
    public static final String ENTITY = "entity";
    public static final String CONFIGURATION = "configuration";
    public static final String PORT = "port";

    public static void generateSensor(JSONObject schema) {

        String fileName=null;


        try {
            String sensorName = schema.getString(ENTITY).substring(0, 1).toUpperCase() + schema.getString(ENTITY).substring(1).toLowerCase();
            fileName = sensorName+"Sensor";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        File file = new File("./app/sensors/impl/"+fileName+".java");

        OutputStream outputStream = makeFile(file);

        StringBuffer contents = new StringBuffer();


            contents.append("package sensors.impl; ");
            contents.append("\n\n");

            contents.append("\nimport org.json.JSONException; ");
            contents.append("\nimport org.json.JSONObject;");
            contents.append("\nimport sensors.base.AbstractSwanSensor;");
            contents.append("\nimport sensors.base.SensorPoller;");
            contents.append("\nimport java.io.IOException;");
            contents.append("\nimport java.io.ObjectInputStream;");
            contents.append("\nimport java.net.ServerSocket;");
            contents.append("\nimport java.net.Socket;");
            contents.append("\nimport java.util.HashMap;");
            contents.append("\nimport java.util.Map;");
            contents.append("\n\n");


            contents.append("\npublic class "+fileName+" extends AbstractSwanSensor {");
            contents.append("\n\n\tprivate Map<String, FogPoller> activeThreads = new HashMap<String, FogPoller>();");

        try {
            contents.append("\n\tpublic static final String[] VALUEPATH = {\""+schema.getString(VALUEPATH)+"\"};");
            contents.append("\n\tpublic static final String[] CONFIGURATION = {\""+schema.getString(CONFIGURATION)+"\"};");
            contents.append("\n\tpublic static final String ENTITY = \""+schema.getString(ENTITY)+"\";");
            contents.append("\n\tpublic static final int PORT = "+schema.getInt(PORT)+";");
        } catch (JSONException e) {
            e.printStackTrace();
        }


            contents.append("\n\n");

            contents.append("\n\tclass FogPoller extends SensorPoller {");

            contents.append("\n\n\t\tServerSocket server;");
            contents.append("\n\t\tSocket socket;");
            contents.append("\n\n\t\tObjectInputStream ois;");

            contents.append("\n\t\tFogPoller(String id, String valuePath, HashMap configuration) {");
            contents.append("\n\t\t\tsuper(id, valuePath, configuration);");
            contents.append("\n\t\t\ttry {");
            contents.append("\n\t\t\t\tserver = new ServerSocket(PORT);");
            contents.append("\n\t\t\t\tsocket = server.accept();");
            contents.append("\n\t\t\t\tois = new ObjectInputStream(socket.getInputStream());");
            contents.append("\n\t\t\t} catch (IOException e) {");
            contents.append("\n\t\t\t\te.printStackTrace();");
            contents.append("\n\t\t\t}");
            contents.append("\n\t\t}");


            contents.append("\n\t\tpublic void run() {");
            contents.append("\n\t\t\twhile (!isInterrupted()) {");

            contents.append("\n\t\t\t\ttry {");

            contents.append("\n\t\t\t\t\tString message = (String) ois.readObject();");

            contents.append("\n\t\t\t\t\ttry {");
            contents.append("\n\t\t\t\t\t\tJSONObject json = new JSONObject(message);");

            contents.append("\n\t\t\t\t\t\tupdateResult("+fileName+".this,json.get(\"data\"),json.getLong(\"time\"));");


            contents.append("\n\t\t\t\t\t } catch (JSONException e) {");
            contents.append("\n\t\t\t\t\t\te.printStackTrace();");
            contents.append("\n\t\t\t\t\t}");

            contents.append("\n\t\t\t\t} catch (IOException e) {");
            contents.append("\n\t\t\t\t\te.printStackTrace();");
            contents.append("\n\t\t\t\t}  catch (ClassNotFoundException e) {");
            contents.append("\n\t\t\t\t\te.printStackTrace();");
            contents.append("\n\t\t\t\t}");

            contents.append("\n\t\t\t}");


            contents.append("\n\t\t}");


            contents.append("\n\t}");


            contents.append("\n\n\t@Override");
            contents.append("\n\tpublic void register(String id, String valuePath, HashMap configuration, HashMap httpConfiguration) {");

            contents.append("\n\t\tsuper.register(id,valuePath,configuration, httpConfiguration);");

            contents.append("\n\t\tFogPoller fogPoller = new FogPoller(id, valuePath,");
            contents.append("\n\t\t\tconfiguration);");
            contents.append("\n\t\tactiveThreads.put(id, fogPoller);");
            contents.append("\n\t\tfogPoller.start();");

            contents.append("\n\t}");

            contents.append("\n\n\t@Override");
            contents.append("\n\tpublic void unregister(String id) {");

            contents.append("\n\t\tsuper.unregister(id);");
            contents.append("\n\t\tSystem.out.println(\"Unregister sensor called\");");
            contents.append("\n\t\tactiveThreads.remove(id).interrupt();");

            contents.append("\n\t}");


            contents.append("\n\n\t@Override");
            contents.append("\n\tpublic String[] getValuePaths()  {");
            contents.append("\n\t\treturn VALUEPATH;");
            contents.append("\n\t}");

            contents.append("\n\n\t@Override");
            contents.append("\n\tpublic String getEntity() {");
            contents.append("\n\t\treturn ENTITY;");
            contents.append("\n\t}");

            contents.append("\n\n\t@Override");
            contents.append("\n\tpublic String[] getConfiguration() {");
            contents.append("\n\t\treturn CONFIGURATION;");
            contents.append("\n\t}");


            contents.append("\n\n}");

        try {
            outputStream.write(contents.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    private static OutputStream makeFile(File file) {


        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
        // Not reachable.
        return out;
    }




}
