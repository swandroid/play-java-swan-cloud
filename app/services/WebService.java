package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.PushNotificationData;
import models.SwanSongExpression;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static credentials.Firebase.APPLICATION_API_KEY;

/**
 * Created by goose on 01/06/16.
 */
public class WebService {

    @Inject WSClient ws;

    public void sendResult(String url, String expressionId, Object data){

        WSRequest request = ws.url(url);
        PushNotificationData pushNotificationData = new PushNotificationData();

        SwanSongExpression swansong = SwanSongExpression.find.byId(expressionId);



        pushNotificationData.to = swansong.tokenId;



        pushNotificationData.data = new PushNotificationData.Data();
        pushNotificationData.data.field = data;



        JsonNode pushNotificationJsonData = Json.toJson(pushNotificationData);

        System.out.println(pushNotificationJsonData.toString());


        request.setHeader("Authorization","key="+APPLICATION_API_KEY);
        CompletionStage<JsonNode> jsonPromise = request.post(pushNotificationJsonData).thenApply(WSResponse::asJson);


    }




}
