package controllers;

import actuator.SendEmail;
import actuator.SendFacebookMessage;
import actuator.SendPhoneResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import engine.ExpressionManager;
import engine.SwanException;
import engine.TriStateExpressionListener;
import engine.ValueExpressionListener;
import models.PushNotificationData;
import models.SwanSongExpression;
import org.json.JSONException;
import org.json.JSONObject;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import swansong.*;
import views.html.index;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

import static credentials.Firebase.APPLICATION_API_KEY;
import static credentials.Firebase.FIREBASE_URL;
//import static credentials.Firebase.PHONE_TOKEN;


/**
 * Created by Roshan Bharath Das on 24/05/16.
 */
public class SwanController extends Controller{

    @Inject WSClient ws;

    ArrayList<SensorValueExpression> sensorValueExpressionList;


    public Result index() {
        return ok(index.render("Your new application is ready."));
    }



    public Result setupWebHook(){


        String[] queryStringToken = request().queryString().get("hub.verify_token");

        String[] queryStringResponse = request().queryString().get("hub.challenge");

        for(String string : queryStringResponse){

            System.out.println(string);

        }


        if(queryStringToken[0].equals("my_voice_is_my_password_verify_me")){

                return ok(request().queryString().get("hub.challenge")[0]);

        }



        return ok("Error, wrong token");

    }



    public  Result requestWebHookSwanBot() {


        JsonNode json = request().body().asJson();

        //System.out.println(json.toString());

        String receivedText = json.findPath("text").textValue();

        JsonNode sender = json.findPath("sender");


        String senderid = sender.findPath("id").textValue();
        String value = json.findPath("text").textValue();

        String command = "unknown",id=null,expression=null;
        String responseCommand;

        SendFacebookMessage sendFacebookMessage = new SendFacebookMessage();

        if(value!=null) {

            System.out.println(value);
            String[] parts = value.split(";");

            for (String part : parts) {
                System.out.println(part);
            }

            if (parts.length == 3) {
                command = parts[0];
                id = parts[1];
                expression = parts[2];
            } else if (parts.length == 2) {
                command = parts[0];
                id = parts[1];
            } else {
                responseCommand = "Minimum number of data should be two";
                sendFacebookMessage.sendResult(senderid, responseCommand, ws);
                return ok();
            }

            if (command.contains("register-value")) {

                if (expression != null && id != null) {
                    try {
                        ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(expression), new ValueExpressionListener() {
                            @Override
                            public void onNewValues(String id, TimestampedValue[] newValues) {
                                if (newValues != null && newValues.length > 0) {
                                    //System.out.println("Rain Sensor (Value):" + newValues[newValues.length - 1].toString());
                                    sendFacebookMessage.sendResult(senderid, newValues[0].toString(), ws);
                                }
                            }
                        });
                    } catch (SwanException e) {
                        e.printStackTrace();
                    } catch (ExpressionParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    responseCommand = "Either expression or id is null";
                    sendFacebookMessage.sendResult(senderid, responseCommand, ws);
                }


            } else if (command.contains("register-tristate")) {

                if (expression != null && id != null) {
                    try {
                        ExpressionManager.registerTriStateExpression(id, (TriStateExpression) ExpressionFactory.parse(expression), new TriStateExpressionListener() {
                            @Override
                            public void onNewState(String id, long timestamp, TriState newState) {

                                //SendEmail.sendEmail();
                                sendFacebookMessage.sendResult(senderid, newState, ws);
                                System.out.println("Currency Sensor (TriState):" + newState);
                            }
                        });
                    } catch (SwanException e) {
                        e.printStackTrace();
                    } catch (ExpressionParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    responseCommand = "Either expression or id is null";
                    sendFacebookMessage.sendResult(senderid, responseCommand, ws);
                }

            } else if (command.contains("unregister")) {

                ExpressionManager.unregisterExpression(id);
                responseCommand = "Unregistered";
                sendFacebookMessage.sendResult(senderid, responseCommand, ws);

            } else {

                responseCommand = "Unknown command";
                sendFacebookMessage.sendResult(senderid, responseCommand, ws);

            }

            //System.out.println(sender+value);

        }

        return ok();

    }



    public Result swanPhoneRegister(){

        JsonNode json = request().body().asJson();

        String id = json.findPath("id").textValue();

        String token = json.findPath("token").textValue();
        String expression = json.findPath("expression").textValue();


        String convertedExpression = convertExpression(expression);

        System.out.println("id:"+id+"\ntoken:"+token+"\nexpression:"+convertedExpression);

        SendPhoneResult sendPhoneResult = new SendPhoneResult();

        if (convertedExpression != null && id != null) {
            try {
                ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(convertedExpression), new ValueExpressionListener() {
                    @Override
                    public void onNewValues(String id, TimestampedValue[] newValues) {
                        if (newValues != null && newValues.length > 0) {


                            JSONObject jsonObject = new JSONObject();


                            try {
                                jsonObject.put("id",id);
                                jsonObject.put("action","register-value");
                                jsonObject.put("data",newValues[0].getValue());
                                jsonObject.put("timestamp",newValues[0].getTimestamp());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            //System.out.println("Rain Sensor (Value):" + newValues[newValues.length - 1].toString());
                            sendPhoneResult.sendResult(jsonObject.toString(), token, ws);


                        }
                    }
                });
            } catch (SwanException e) {
                e.printStackTrace();
            } catch (ExpressionParseException e) {
                e.printStackTrace();
            }
        }


        return ok();
    }

    public String convertExpression(String expression) {
        String convertedExpression = null;
        if (expression.contains("cloud")) {
            convertedExpression = expression.replace("cloud", "self");
        }
        return convertedExpression;
    }

    public Result swanPhoneUnregister(){

        JsonNode json = request().body().asJson();

        String id = json.findPath("id").textValue();

        String token = json.findPath("token").textValue();


        SendPhoneResult sendPhoneResult = new SendPhoneResult();
        ExpressionManager.unregisterExpression(id);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id",id);
            jsonObject.put("command","unregister");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        sendPhoneResult.sendResult(jsonObject.toString(), token, ws);

        return ok();
    }



    public Result swanSongJSONService(){

        JsonNode json = request().body().asJson();


        if(json == null) {

            return badRequest("Expecting Json data");

        } else {


            String tokenId = json.findPath("tokenId").textValue();
            String expressionId = json.findPath("expressionId").textValue();
            String expressionString = json.findPath("expression").textValue();

            System.out.println("tokenId:"+tokenId+" expressionId:"+expressionId+" expressionString:"+expressionString);
            saveSwanSong(tokenId,expressionId,expressionString);





            try {

                Expression expression = ExpressionFactory.parse(expressionString);


                callWebService(FIREBASE_URL , expressionId, expression);

                return ok("Expression: "+ expression.toParseString());

            } catch (Throwable t) {
                return ok("Bad expression:" + t);
            }

        }

    }

    public void saveSwanSong(String tokenId, String expressionId, String expression){

        SwanSongExpression swanSong = new SwanSongExpression();
        swanSong.tokenId = tokenId;
        swanSong.expressionId = expressionId;
        swanSong.expression = expression;

        swanSong.save();


    }


    public Result callWebService(String url, String expressionId, Object data){


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



        return ok(jsonPromise.toString());
    }





    public Result testWebService(){


        WSRequest request = ws.url(FIREBASE_URL);
        PushNotificationData pushNotificationData = new PushNotificationData();
        //pushNotificationData.to = PHONE_TOKEN;

        pushNotificationData.data = new PushNotificationData.Data();
        pushNotificationData.data.field = 10;



        JsonNode pushNotificationJsonData = Json.toJson(pushNotificationData);

        System.out.println(pushNotificationJsonData.toString());


        request.setHeader("Authorization","key="+APPLICATION_API_KEY);
        CompletionStage<JsonNode> jsonPromise = request.post(pushNotificationJsonData).thenApply(WSResponse::asJson);



        return ok(jsonPromise.toString());
    }








    public void initialize (String id, Expression expression){
            //System.out.println("initialize-start");
            //System.out.println(expression.getLocation());
            resolveLocation(expression);
            String location = expression.getLocation();
            if (!location.equals(Expression.LOCATION_SELF)
                    && !location.equals(Expression.LOCATION_INDEPENDENT)) {
               System.out.println("Wrong input");

            } else if (expression instanceof LogicExpression) {

            //    System.out.println("LogicExpression  "+ id + Expression.LEFT_SUFFIX +"="+((LogicExpression) expression).getLeft()+"   "+ id + Expression.RIGHT_SUFFIX+"="+"="+((LogicExpression) expression).getRight());
                initialize(id + Expression.LEFT_SUFFIX,
                        ((LogicExpression) expression).getLeft());
                initialize(id + Expression.RIGHT_SUFFIX,
                        ((LogicExpression) expression).getRight());
            } else if (expression instanceof ComparisonExpression) {
            //    System.out.println("ComparisonExpression "+ id + Expression.LEFT_SUFFIX +"="+((ComparisonExpression) expression).getLeft()+"   "+ id + Expression.RIGHT_SUFFIX+"="+"="+((ComparisonExpression) expression).getRight());
                initialize(id + Expression.LEFT_SUFFIX,
                        ((ComparisonExpression) expression).getLeft());
                initialize(id + Expression.RIGHT_SUFFIX,
                        ((ComparisonExpression) expression).getRight());
            } else if (expression instanceof MathValueExpression) {
            //    System.out.println("MathValueExpression "+ id + Expression.LEFT_SUFFIX +"="+((MathValueExpression) expression).getLeft()+"   "+ id + Expression.RIGHT_SUFFIX+"="+"="+((MathValueExpression) expression).getRight());
                initialize(id + Expression.LEFT_SUFFIX,
                        ((MathValueExpression) expression).getLeft());
                initialize(id + Expression.RIGHT_SUFFIX,
                        ((MathValueExpression) expression).getRight());
            } else if (expression instanceof SensorValueExpression) {
            //    System.out.println("SensorValueExpression ");
                if (((SensorValueExpression) expression).getEntity().equals("time")) {
                    return;
                }
                // do the real work here, bind to the sensor.
                sensorValueExpressionList.add((SensorValueExpression) expression);

            }
        System.out.println("initialize-end");

    }


    public void resolveLocation(Expression expression) {
      //  System.out.println("resolveLocation-start");
        if (!Expression.LOCATION_INFER.equals(expression.getLocation())) {
            return;
        }
        String left = null;
        String right = null;
        if (expression instanceof LogicExpression) {
        //    System.out.println("resolveLocation-LogicExpression");
            resolveLocation(((LogicExpression) expression).getLeft());
            left = ((LogicExpression) expression).getLeft().getLocation();
            resolveLocation(((LogicExpression) expression).getRight());
            right = ((LogicExpression) expression).getRight().getLocation();
        } else if (expression instanceof ComparisonExpression) {
         //   System.out.println("resolveLocation-ComparisonExpression");
            resolveLocation(((ComparisonExpression) expression).getLeft());
            left = ((ComparisonExpression) expression).getLeft().getLocation();
            resolveLocation(((ComparisonExpression) expression).getRight());
            right = ((ComparisonExpression) expression).getRight()
                    .getLocation();
        } else if (expression instanceof MathValueExpression) {
         //   System.out.println("resolveLocation-MathValueExpression");
            resolveLocation(((MathValueExpression) expression).getLeft());
            left = ((MathValueExpression) expression).getLeft().getLocation();
            resolveLocation(((MathValueExpression) expression).getRight());
            right = ((MathValueExpression) expression).getRight().getLocation();
        }
        if (left.equals(right)) {
            expression.setInferredLocation(left);
        } else if (left.equals(Expression.LOCATION_INDEPENDENT)) {
            expression.setInferredLocation(right);
        } else if (right.equals(Expression.LOCATION_INDEPENDENT)) {
            expression.setInferredLocation(left);
        } else if (left.equals(Expression.LOCATION_SELF)
                || right.equals(Expression.LOCATION_SELF)) {
            expression.setInferredLocation(Expression.LOCATION_SELF);
        } else {
            expression.setInferredLocation(left);
        }
     //   System.out.println("resolveLocation-end");
    }



    public Result swanSongFormService(){



        sensorValueExpressionList = new ArrayList<SensorValueExpression>();

        String swansong = request().body().asFormUrlEncoded().get("name")[0];

            try {

                Expression expression = ExpressionFactory.parse(swansong);


                initialize("12",expression);

                ArrayNode resultlist = Json.newArray();

                for(SensorValueExpression sensorValueExpression:sensorValueExpressionList){
                    ObjectNode result = Json.newObject();
                    result.put("location",sensorValueExpression.getLocation());
                    result.put("entityid",sensorValueExpression.getEntity());
                    result.put("valuepath",sensorValueExpression.getValuePath());
                    result.put("historyreductionmode",sensorValueExpression.getHistoryReductionMode().toParseString());
                    result.put("historylength",sensorValueExpression.getHistoryLength());

                    resultlist.add(result);

                }

                ObjectNode output = Json.newObject();
                output.set("Expression", resultlist);
                return ok(output);
                //return ok("Good Expression: "+ expression.toParseString());

            } catch (Throwable t) {
                return ok("Bad expression:" + t);
            }


    }



    public Result testRegisterRainValueSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "1234";
        String myExpression = "self@rain:expected_mm{ANY,0}";
        try {
            ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                @Override
                public void onNewValues(String id, TimestampedValue[] newValues) {
                    if(newValues!=null && newValues.length>0) {
                        System.out.println("Rain Sensor (Value):" + newValues[newValues.length-1].toString());
                    }
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }


        return ok("Registered");

    }



    public Result testRegisterTestValueSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "2345";
        String myExpression = "self@test:value{ANY,0}";
        try {
            ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                @Override
                public void onNewValues(String id, TimestampedValue[] newValues) {
                    if(newValues!=null && newValues.length>0) {
                        System.out.println("Test Sensor (Value):" + newValues[newValues.length-1].toString());
                    }
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }


        return ok("Registered");

    }

    public Result testRegisterRainTriStateSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "1235";
        String myExpression = "self@rain:expected_mm{ANY,1000} > 0.0";
        try {
            ExpressionManager.registerTriStateExpression(id, (TriStateExpression) ExpressionFactory.parse(myExpression), new TriStateExpressionListener() {
                @Override
                public void onNewState(String id, long timestamp, TriState newState) {

                    System.out.println("Rain Sensor (TriState):"+newState);
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }


        return ok("Registered");

    }

    public Result testRegisterTestTriStateSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "2346";
        String myExpression = "self@test:value{ANY,1000} > 0";
        try {
            ExpressionManager.registerTriStateExpression(id, (TriStateExpression) ExpressionFactory.parse(myExpression), new TriStateExpressionListener() {
                @Override
                public void onNewState(String id, long timestamp, TriState newState) {

                    System.out.println("Test Sensor (TriState):"+newState);
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }


        return ok("Registered");

    }


    public Result testRegisterCurrencyValueSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "3333";
        String myExpression = "self@currency:exchange?from='EUR'#to='USD'$server_storage=FALSE{ANY,1000}";
        try {
            ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                @Override
                public void onNewValues(String id, TimestampedValue[] newValues) {
                    if(newValues!=null && newValues.length>0) {
                        System.out.println("Currency Sensor (Value):" + newValues[newValues.length-1].toString());
                    }
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }


        return ok("Registered");

    }

    public Result testRegisterCurrencyTriStateSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "3334";
        String myExpression = "self@currency:exchange{ANY,1000} > 75.0 || self@test:value{ANY,1000} > 0";
        try {
            ExpressionManager.registerTriStateExpression(id, (TriStateExpression) ExpressionFactory.parse(myExpression), new TriStateExpressionListener() {
                @Override
                public void onNewState(String id, long timestamp, TriState newState) {

                    //SendEmail.sendEmail();

                    System.out.println("Currency Sensor (TriState):"+newState);
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }


        return ok("Expression Registered");

    }


    public Result registerExpressionForEmailNotification(){


        JsonNode json = request().body().asJson();


        if(json == null) {

            return badRequest("Expecting Json data");

        } else {


            String expressionId = json.findPath("id").textValue();
            String expressionString = json.findPath("expression").textValue();

            String notificationEmail = json.findPath("email").textValue();

            System.out.println("notificationEmail:" + notificationEmail + " expressionId:" + expressionId + " expressionString:" + expressionString);


            //String id = "3334";
            //String myExpression = "self@currency:exchange{ANY,1000} > 75.0 || self@test:value{ANY,1000} > 0";
            try {
                ExpressionManager.registerTriStateExpression(expressionId, (TriStateExpression) ExpressionFactory.parse(expressionString), new TriStateExpressionListener() {
                    @Override
                    public void onNewState(String id, long timestamp, TriState newState) {


                        SendEmail.sendEmail(notificationEmail, expressionId, expressionString, newState);

                        System.out.println("Currency Sensor (TriState):" + newState);
                    }
                });
            } catch (SwanException e) {
                e.printStackTrace();
            } catch (ExpressionParseException e) {
                e.printStackTrace();
            }


        }

        return ok("Expression Registered");

    }


    public Result unRegisterExpressionForEmailNotification() {


        JsonNode json = request().body().asJson();


        if(json == null) {

            return badRequest("Expecting Json data");

        } else {

            String expressionId = json.findPath("id").textValue();
            ExpressionManager.unregisterExpression(expressionId);

        }

        return ok("Expression Unregistered");

    }

    public Result testUnregisterRainValueSwan(){


        String id = "1234";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }


    public Result testUnregisterRainTriStateSwan(){


        String id = "1235";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }


    public Result testUnregisterTestValueSwan(){


        String id = "2345";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }


    public Result testUnregisterTestTriStateSwan(){


        String id = "2346";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }



    public Result testUnregisterCurrencyValueSwan(){


        String id = "3333";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }


    public Result testUnregisterCurrencyTriStateSwan(){


        String id = "3334";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }




    public Result testRegisterAll(){

        testRegisterRainValueSwan();

        testRegisterTestValueSwan();

        testRegisterCurrencyValueSwan();

        testRegisterRainTriStateSwan();

        testRegisterTestTriStateSwan();

        testRegisterCurrencyTriStateSwan();


        return ok("Registered");
    }



    public Result testUnregisterAll(){

        testUnregisterRainValueSwan();

        testUnregisterTestValueSwan();

        testUnregisterCurrencyValueSwan();

        testUnregisterRainTriStateSwan();

        testUnregisterTestTriStateSwan();

        testUnregisterCurrencyTriStateSwan();

        return ok("Unregistered");
    }



}
