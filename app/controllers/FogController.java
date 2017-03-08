package controllers;

import actuator.Actuator;
import actuator.SendDataToWeb;
import actuator.SendDataToWebViaSocket;
import actuator.SendFacebookMessage;
import com.fasterxml.jackson.databind.JsonNode;
//import com.rabbitmq.client.*;
import com.rabbitmq.client.*;
import engine.ExpressionManager;
import engine.SwanException;
import engine.TriStateExpressionListener;
import engine.ValueExpressionListener;
import interdroid.swancore.swansong.*;
import org.json.JSONException;
import org.json.JSONObject;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import sensors.base.GenerateFogSensor;


import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Roshan Bharath Das on 05/03/2017.
 */
public class FogController extends Controller {

    @Inject
    WSClient ws;

    public Result requestToFog() {


        JsonNode json = request().body().asJson();

        //System.out.println(json.toString());

        String senderId = json.findPath("sender").textValue();

        String expression = json.findPath("expression").textValue();

        String command = json.findPath("command").textValue();

        String id= json.findPath("id").textValue();



        //if(swanexpression)then(actuatorexpression)
        //actuatorexpression = url@sensor:valuepath#configuration
        //actuatorexpression for sending push notification to phone

        //actuatorexpression for sending facebook message

        //actuatorexpression for sending to another server in the fog


        controlActuation(expression,id,command,senderId);
        //TO DO: String the actuator part from the expression

        //subExpression[0] = swan expression
        //subExpression[1] = actuator expression


        return ok();

    }



    public void controlActuation(String expression, String id, String command, String senderId){




        String[] subExpression = expression.split("±±±");
        String[] acutatorLocation = subExpression[1].split("@");
        String[] acutatorURL = acutatorLocation[0].split(":");
        String[] actuatorSubExpression = acutatorLocation[1].split("[:#]");



        if(acutatorURL[0].equals("phone")){

        }
        else{

            //SendDataToWeb sendDataToWeb =new SendDataToWeb(acutatorLocation[0],id);
            SendDataToWebViaSocket sendDataToWeb =new SendDataToWebViaSocket(acutatorURL[0],Integer.parseInt(acutatorURL[1]),id);
            controlSWANExpression(command,subExpression[0], id, senderId, sendDataToWeb, actuatorSubExpression);


        }



    }



    public void controlSWANExpression(String command, String expression, String id, String senderid, Actuator actuator,String[] actuatorSensors){


        String responseCommand;

        if (command.contains("register-value")) {

            if (expression != null && id != null) {
                try {
                    ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(expression), new ValueExpressionListener() {
                        @Override
                        public void onNewValues(String id, TimestampedValue[] newValues) {
                            if (newValues != null && newValues.length > 0) {
                                JSONObject jsonObject = new JSONObject();


                                try {
                                    jsonObject.put("id", id);
                                    jsonObject.put("A", "V");
                                    jsonObject.put("data",newValues[0].getValue());
                                    jsonObject.put("time",newValues[0].getTimestamp());
                                    jsonObject.put("sensor",actuatorSensors[0]);
                                    jsonObject.put("valuepath",actuatorSensors[1]);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                actuator.sendResult(jsonObject.toString(),senderid,ws);

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
            }

        } else if (command.contains("register-tristate")) {

            if (expression != null && id != null) {
                try {
                    ExpressionManager.registerTriStateExpression(id, (TriStateExpression) ExpressionFactory.parse(expression), new TriStateExpressionListener() {
                        @Override
                        public void onNewState(String id, long timestamp, TriState newState) {

                            JSONObject jsonObject = new JSONObject();
                            //SendEmail.sendEmail();
                            //sendFacebookMessage.sendResult(senderid, newState, ws);
                            try {
                                jsonObject.put("id", id);
                                jsonObject.put("A", "T");
                                jsonObject.put("data", newState);
                                jsonObject.put("time", timestamp);
                                jsonObject.put("sensor",actuatorSensors[0]);
                                jsonObject.put("valuepath",actuatorSensors[1]);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            actuator.sendResult(jsonObject.toString(), senderid, ws);
                        }
                    });
                } catch (SwanException e) {
                    e.printStackTrace();
                } catch (ExpressionParseException e) {
                    e.printStackTrace();
                }
            } else {
                responseCommand = "Either expression or id is null";
            }

        } else if (command.contains("unregister")) {

            ExpressionManager.unregisterExpression(id);
            responseCommand = "Unregistered";

            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("id",id);
                jsonObject.put("A","U");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            actuator.sendResult(jsonObject.toString(),senderid,ws);

        } else {

            responseCommand = "Unknown command";
        }


    }



    public Result receiveFromFog() {


        JsonNode json = request().body().asJson();

        //System.out.println(json.toString());

        //String senderId = json.findPath("sender").textValue();

        //String expression = json.findPath("expression").textValue();

        //String command = json.findPath("command").textValue();

        //String id= json.findPath("id").textValue();


        System.out.println("Received : "+json.toString());


        return ok();

    }


    public Result testFogSensor() {


        JsonNode json = request().body().asJson();


        String expression = json.findPath("expression").textValue();

        String command = json.findPath("command").textValue();

        String id = json.findPath("id").textValue();


        if (command.contains("register-value")) {

            if (expression != null && id != null) {
                try {
                    ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(expression), new ValueExpressionListener() {
                        @Override
                        public void onNewValues(String id, TimestampedValue[] newValues) {
                            if (newValues != null && newValues.length > 0) {
                                System.out.println("Fog Test Sensor (Value):" + newValues[newValues.length-1].toString());
                            }
                        }
                    });
                } catch (SwanException e) {
                    e.printStackTrace();
                } catch (ExpressionParseException e) {
                    e.printStackTrace();
                }
            }

        } else if (command.contains("register-tristate")) {

            if (expression != null && id != null) {
                try {
                    ExpressionManager.registerTriStateExpression(id, (TriStateExpression) ExpressionFactory.parse(expression), new TriStateExpressionListener() {
                        @Override
                        public void onNewState(String id, long timestamp, TriState newState) {
                            System.out.println("Fog Test Sensor (Tristate):" + newState.toString());
                        }
                    });
                } catch (SwanException e) {
                    e.printStackTrace();
                } catch (ExpressionParseException e) {
                    e.printStackTrace();
                }
            }

        } else if (command.contains("unregister")) {

            ExpressionManager.unregisterExpression(id);
        }


        return ok();
    }


    public Result generateFogSensor() {


        JsonNode json = request().body().asJson();

        try {
            JSONObject jsonObject = new JSONObject(json.toString());
            GenerateFogSensor.generateSensor(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return ok();
    }



    public Result publishExpression() {


        JsonNode json = request().body().asJson();

        String expression = json.findPath("expression").textValue();

        String command = json.findPath("command").textValue();

        String combination = command+"!"+expression;

        String id= json.findPath("id").textValue();


        publish(id,combination);

        return ok();
    }


    public Result subscribeExpression() {


        JsonNode json = request().body().asJson();

        String host = json.findPath("host").textValue();

        int port = json.findPath("port").intValue();

        String id= json.findPath("id").textValue();

        subscribe(id,host,port);

        return ok();
    }



    public void publish(String topicName, String expression) {

        final String EXCHANGE_NAME = "topic_logs";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String routingKey = topicName;
            String message = expression;
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));


            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignore) {
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }





    public void subscribe(String topicName, String hostname, int port) {

        final String EXCHANGE_NAME = "topic_logs";

        ConnectionFactory factory = new ConnectionFactory();
        //TODO: change to hostname
        factory.setHost("localhost");

        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();

            channel.queueBind(queueName, EXCHANGE_NAME, topicName);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");


            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String combinedExpression = new String(body, "UTF-8");

                    System.out.println(" Received subscribed data");

                    String[] subExpression = combinedExpression.split("!");

                    controlActuation(subExpression[1],topicName,subExpression[1],"test");



                }
            };

            channel.basicConsume(queueName, true, consumer);



        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }


}
