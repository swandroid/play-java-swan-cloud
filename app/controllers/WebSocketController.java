package controllers;

//import play.libs.streams.ActorFlow;
import akka.io.Inet;
import engine.ExpressionManager;
import engine.SwanException;
import engine.ValueExpressionListener;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.ValueExpression;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;
import play.api.libs.streams.ActorFlow;
import play.mvc.*;
import akka.actor.*;
import akka.stream.*;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import scala.concurrent.Future;
import sensors.base.SensorFactory;
import sensors.base.SensorInterface;
import sensors.impl.WebsocketSensor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by Roshan Bharath Das on 26/05/2017.
 */


public class WebSocketController { //extends Controller {


    public WebSocket socket() {
        return WebSocket.Text.accept(request -> {
            // Log events to the console

           // Sink<String, ?> in = Sink.foreach(System.out::println);
            Sink<String, ?> in = Sink.foreach(a -> pushDataForProcessing(a));
            // Send a single 'Hello!' message and then leave the socket open
            Source<String, ?> out = Source.single("Hello").concat(Source.maybe());
            return Flow.fromSinkAndSource(in, out);
        });
    }


    public void sendDataToPhoneAfterProcessing(Source<String, ?> out){

        String id = "websocket1-2345";
        String myExpression = "self@websocket:value?delay='1000'{ANY,1000}";

        try {
            ExpressionManager.registerValueExpression(id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                        @Override
                        public void onNewValues(String id, TimestampedValue[] newValues) {
                            if (newValues != null && newValues.length > 0) {

                               /*
                                JSONObject jsonObject = new JSONObject();


                                try {
                                    jsonObject.put("id", id);
                                    jsonObject.put("A", "V");
                                    jsonObject.put("data",newValues[0].getValue());
                                    jsonObject.put("time",newValues[0].getTimestamp());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }*/

                                //actuator.sendResult(jsonObject.toString(),senderid,ws);

                            }
                        }
                    });
        } catch (SwanException e1) {
            e1.printStackTrace();
        } catch (ExpressionParseException e1) {
            e1.printStackTrace();
        }

    }



    public void pushDataForProcessing(String a){

        String entity="websocket";

        WebsocketSensor sensor = (WebsocketSensor) SensorFactory.getSensor(entity);
        sensor.updateResult(a);

        //System.out.println("blablah "+a);

    }

    
  /*public WebSocket socket() {
      return WebSocket.Text.accept(request -> {

          // log the message to stdout and send response back to client
          return Flow.<String>create().map(msg -> {
              System.out.println(msg);
              return "I received your message: " + msg;
          });
      });
  }*/


}
