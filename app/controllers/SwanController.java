package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Controller;
import play.mvc.Result;
import swansong.Expression;
import swansong.ExpressionFactory;

/**
 * Created by Roshan Bharath Das on 24/05/16.
 */
public class SwanController extends Controller{

    public Result swanSongService(){

        JsonNode json = request().body().asJson();

        if(json == null) {

            return badRequest("Expecting Json data");

        } else {

            String swanstring = json.findPath("song").textValue();

            try {

                Expression expression = ExpressionFactory.parse(swanstring);

                return ok("Expression: "+ expression.toParseString());

            } catch (Throwable t) {
                return ok("Bad expression:" + t);
            }


        }



    }


}
