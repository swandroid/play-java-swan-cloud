package engine;

/**
 * Created by goose on 06/06/16.
 */
public class SwanException extends Exception {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 330707408532036445L;

    /**
     * Instantiates a new context framework exception.
     *
     * @param string the string
     */
    public SwanException(final String string) {
        super(string);
    }

    /**
     * Instantiates a new context framework exception.
     *
     * @param e the e
     */
    public SwanException(final Exception e) {
        super(e);
    }

}
