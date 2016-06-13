package engine;

/**
 * Created by goose on 06/06/16.
 */
public class SensorSetupFailedException extends SwanException {


    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 4870747041723819862L;

    /**
     * Constructs this exception.
     *
     * @param message the message for the exception
     */
    public SensorSetupFailedException(final String message) {
        super(message);
    }

}
