package actuator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.PushNotificationData;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static credentials.Firebase.APPLICATION_API_KEY;
import static credentials.Firebase.FIREBASE_URL;

/**
 * Created by Roshan Bharath Das on 28/02/2017.
 */
public class SendDataToWeb implements Actuator{

    private String url;

    private String applicationKey;

    public SendDataToWeb(String url, String applicationKey){

        this.url =url;
        this.applicationKey = applicationKey;
    }

    @Override
    public void sendResult(String field, String token, WSClient ws) {

        WSRequest request = ws.url(url);
        PushNotificationData pushNotificationData = new PushNotificationData();
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
        System.out.println(pushNotificationJsonData.toString());
        request.setHeader("Authorization", "key=" + applicationKey);
        CompletionStage<JsonNode> jsonPromise = request.post(pushNotificationJsonData).thenApply(WSResponse::asJson);


    }


}
