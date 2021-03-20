package interdroid.swancore.swansong;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;

public class ExpressionFactory {

	public static Expression parse(String parseString)
			throws ExpressionParseException {
		return SwanExpressionParser.parseExpression(parseString);
	}
	
	public static HashMap<String,String> parseOptimizationJsonExpression(JsonNode json) throws ExpressionParseException {
		HashMap<String,String> userData = new HashMap<>();
		if(json.get("expression").asText().equals("cloud@optimization")){
			userData.put("originLatitude",json.get("originLatitude").asText());
			userData.put("originLongitude",json.get("originLongitude").asText());
			userData.put("destinationLatitude",json.get("destinationLatitude").asText());
			userData.put("destinationLongitude",json.get("destinationLongitude").asText());
			userData.put("sensors",json.get("sensors").asText());
			userData.put("id",json.get("id").asText());
			userData.put("token",json.get("token").asText().replace("SEMICOLON",":"));
			userData.put("deadline",json.get("deadline").asText());
			return userData;
		}else throw new ExpressionParseException(new Exception("The expression does not have the right format."));
	}
	
	public static HashMap<String,String> parseOptimizationJsonUpdateExpression(JsonNode json){
		HashMap<String,String> userData = new HashMap<>();
		userData.put("id",json.get("id").asText());
		userData.put("originLatitude",json.get("originLatitude").asText());
		userData.put("originLongitude",json.get("originLongitude").asText());
		userData.put("deadline", String.valueOf(json.get("deadline")));
		return userData;
	}

}
