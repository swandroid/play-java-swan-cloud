package optimization.jsonParser;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Maria Efthymiadou on Jun, 2019
 */

public class DublinCityNoiseParser {
    HashMap<Object,Object> results;
    String url=null;
    String fileName;
    public DublinCityNoiseParser(String string){
        if(string.contains("http")){
            this.url=string;
            results = new HashMap<>();
        }else this.fileName=string;

    }


    public JSONObject parse() throws JSONException {
        JSONObject json = null;
        try {
            InputStream is = new URL(this.url).openStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String jsonText = readAll(r);
            json = new JSONObject(jsonText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;

    }


    public List<Double> parseFile() throws IOException, JSONException {
        List<Double> values = new ArrayList<>();
        String[] jsonValues = new String[0];
        File file = new File(fileName);
        if (file.exists()){
            InputStream is = new FileInputStream(fileName);
            String jsonTxt = IOUtils.toString(is, "UTF-8");
//            System.out.println(jsonTxt);
            JSONObject json = new JSONObject(jsonTxt);
            JSONArray jsonArray = (JSONArray) json.get("aleq");
            for (int i=0;i<jsonArray.length();i++){
                values.add(Double.parseDouble((String) jsonArray.get(i)));
            }
        }
        return values;
    }

    private String readAll(BufferedReader r) throws IOException {
            StringBuilder sb = new StringBuilder();
            String line;// = r.readLine();
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
    }

}
