package thesis.testing.utils;

/**
 * Defines exception that occurs, when there is something wrong with a data point
 */
public class OldbException extends Exception {

    public OldbException() {
        super();
    }

    public OldbException(String errorMessage) {
        super(errorMessage);
    }

    public OldbException(Throwable err) {
        super(err);
    }

    public OldbException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

}
