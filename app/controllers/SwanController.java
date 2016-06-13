package controllers;

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
import models.SwanSong;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import swansong.*;
import views.html.index;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

import static credentials.Firebase.APPLICATION_API_KEY;
import static credentials.Firebase.FIREBASE_URL;
import static credentials.Firebase.PHONE_TOKEN;


/**
 * Created by Roshan Bharath Das on 24/05/16.
 */
public class SwanController extends Controller{

    @Inject WSClient ws;

    ArrayList<SensorValueExpression> sensorValueExpressionList;


    public Result index() {
        return ok(index.render("Your new application is ready."));
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

        SwanSong swanSong = new SwanSong();
        swanSong.tokenId = tokenId;
        swanSong.expressionId = expressionId;
        swanSong.expression = expression;

        swanSong.save();


    }


    public Result callWebService(String url, String expressionId, Object data){


        WSRequest request = ws.url(url);
        PushNotificationData pushNotificationData = new PushNotificationData();

        SwanSong swansong = SwanSong.find.byId(expressionId);



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
        pushNotificationData.to = PHONE_TOKEN;

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



    public Result testRegisterValueSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "1234";
        String myExpression = "self@rain:expected_mm{ANY,0}";
        try {
            ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                @Override
                public void onNewValues(String id, TimestampedValue[] newValues) {
                    if(newValues!=null && newValues.length>0) {
                        System.out.println("New value received:" + newValues[newValues.length-1].toString());
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

    public Result testRegisterTriStateSwan(){


        ExpressionManager expressionManager = new ExpressionManager();

        String id = "1235";
        String myExpression = "self@rain:expected_mm{ANY,0} > 0.0";
        try {
            ExpressionManager.registerTriStateExpression(id, (TriStateExpression) ExpressionFactory.parse(myExpression), new TriStateExpressionListener() {
                @Override
                public void onNewState(String id, long timestamp, TriState newState) {

                    System.out.println("New state detected:"+newState);
                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }


        return ok("Registered");

    }

    public Result testUnregisterValueSwan(){


        String id = "1234";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }


    public Result testUnregisterTriStateSwan(){


        String id = "1235";

        ExpressionManager.unregisterExpression(id);

        return ok("Unregistered");

    }



}
