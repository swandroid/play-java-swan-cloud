package engine;

/**
 * Created by goose on 06/06/16.
 */
public class SensorConfigurationException extends SwanException {

    /**
     * The serial version id.
     */
    private static final long serialVersionUID = -252249549122493864L;

    /**
     * Construct this type of exception.
     *
     * @param message the message for the exception
     */
    public SensorConfigurationException(final String message) {
        super(message);
    }

}