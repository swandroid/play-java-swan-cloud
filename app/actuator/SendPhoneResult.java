package actuator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.OptimizationController;
import credentials.Firebase;
import models.PushNotificationData;
import org.json.JSONException;
import org.json.JSONObject;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static credentials.Firebase.APPLICATION_API_KEY;
import static credentials.Firebase.FIREBASE_URL;


/**
 * Created by Roshan Bharath Das on 29/06/16.
 */
public class SendPhoneResult implements Actuator{

    public void sendResult(String field, String token, WSClient ws) {
        WSRequest request = ws.url(FIREBASE_URL);
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
        request.setHeader("Authorization", "key=" + APPLICATION_API_KEY);
        CompletionStage<JsonNode> jsonPromise = request.post(pushNotificationJsonData).thenApply(WSResponse::asJson);
    }
    
    public void sendUpdateToPhone(String id,String result) {
        JSONObject data =createPhoneMessage(OptimizationController.optimizationControllerData.activeUsersData.get(id).getToken(),result);
        System.out.println("User token "+OptimizationController.optimizationControllerData.activeUsersData.get(id).getToken());
        System.out.println("Navigation data to send "+data.toString());
        sendResult(data.toString(), OptimizationController.optimizationControllerData.activeUsersData.get(id).getToken(), OptimizationController.optimizationControllerData.activeUsersWs.get(id), Firebase.LEGACY_API_KEY);
    }
    
    public static JSONObject createPhoneMessage(String token, String path){
        JSONObject message = new JSONObject();
        try {
            message.put("to", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        JSONObject data = new JSONObject();
        try {
            data.put("body", path);
            message.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }
    
    public void sendResult(String data, String token, WSClient ws, String API_KEY) {
        WSRequest request = ws.url(FIREBASE_URL);
        PushNotificationData pushNotificationData = new PushNotificationData();
        pushNotificationData.to = token;
        pushNotificationData.data = new PushNotificationData.Data();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(data.toString());
            pushNotificationData.data.field = actualObj;
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        JsonNode pushNotificationJsonData = Json.toJson(pushNotificationData);
        System.out.println("pushNotificationJsonData  "+pushNotificationJsonData.toString());
        request.setHeader("Authorization", "key=" + API_KEY);
        request.setHeader("Content-Type","application/json");
        request.post(pushNotificationJsonData);
    }
    
    

}
