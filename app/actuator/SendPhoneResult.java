package actuator;

import com.fasterxml.jackson.databind.JsonNode;
import models.PushNotificationData;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import static credentials.Firebase.APPLICATION_API_KEY;
import static credentials.Firebase.FIREBASE_URL;


/**
 * Created by Roshan Bharath Das on 29/06/16.
 */
public class SendPhoneResult {

    public void sendResult(Object field, String token, WSClient ws){


        WSRequest request = ws.url(FIREBASE_URL);
        PushNotificationData pushNotificationData = new PushNotificationData();
        pushNotificationData.to = token;

        pushNotificationData.data = new PushNotificationData.Data();
        pushNotificationData.data.field = field;



        JsonNode pushNotificationJsonData = Json.toJson(pushNotificationData);

        System.out.println(pushNotificationJsonData.toString());


        request.setHeader("Authorization","key="+APPLICATION_API_KEY);
        CompletionStage<JsonNode> jsonPromise = request.post(pushNotificationJsonData).thenApply(WSResponse::asJson);



    }



}
