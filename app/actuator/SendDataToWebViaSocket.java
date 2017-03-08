package actuator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.PushNotificationData;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Roshan Bharath Das on 06/03/2017.
 */
public class SendDataToWebViaSocket implements Actuator {


    private String url;

    private int port;

    private String applicationKey;

    Socket clientSocket;

    ObjectOutputStream out = null;

    public SendDataToWebViaSocket(String url, int port, String applicationKey){

        this.url =url;
        this.applicationKey = applicationKey;
        this.port =port;
        try {
            clientSocket = new Socket(url, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    @Override
    public void sendResult(String field, String token, WSClient ws) {


      /*  PushNotificationData pushNotificationData = new PushNotificationData();
        pushNotificationData.to = token;
        pushNotificationData.data = new PushNotificationData.Data();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(field);
            pushNotificationData.data.field = actualObj;
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonNode pushNotificationJsonData = Json.toJson(pushNotificationData);


        try {
            //out.writeObject(pushNotificationJsonData);
            out.writeBytes(pushNotificationJsonData.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        try {
            out.writeObject(""+field);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
