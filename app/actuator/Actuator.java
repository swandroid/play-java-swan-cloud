package actuator;

import play.libs.ws.WSClient;

/**
 * Created by Roshan Bharath Das on 06/03/2017.
 */
public interface Actuator {

    public void sendResult(String field, String token, WSClient ws);


}
