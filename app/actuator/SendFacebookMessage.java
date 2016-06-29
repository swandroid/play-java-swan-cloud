package actuator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import credentials.Facebook;
import models.PushNotificationData;
import models.SwanSongExpression;
import org.json.JSONException;
import org.json.JSONObject;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;

import javax.inject.Inject;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static credentials.Firebase.APPLICATION_API_KEY;

/**
 * Created by goose on 22/06/16.
 */
public class SendFacebookMessage {


    public void sendResult(String senderId, Object data, WSClient ws){

        WSRequest request = ws.url(Facebook.FACEBOOK_URL);

        try {
            JSONObject idJsonObject = new JSONObject();
            idJsonObject.put("id",senderId);

            JSONObject textJsonObject = new JSONObject();
            textJsonObject.put("text",data);

            JSONObject postJsonObject = new JSONObject();
            postJsonObject.put("recipient",idJsonObject);
            postJsonObject.put("message",textJsonObject);

            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode actualObj = mapper.readTree(postJsonObject.toString());

                System.out.println(actualObj);

                request.setQueryString("access_token="+Facebook.FACEBOOK_TOKEN);
                //request.setHeader("access_token",Facebook.FACEBOOK_TOKEN);
                CompletionStage<JsonNode> jsonPromise = request.post(actualObj).thenApply(WSResponse::asJson);


            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        //pushNotificationData.to = swansong.tokenId;



        //pushNotificationData.data = new PushNotificationData.Data();
        //pushNotificationData.data.field = data;



    }



}
