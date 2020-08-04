package thesis.testing.utils;

/**
 * Defines exception that occurs, when there is something wrong with a data packet
 */
public class ArchivingException extends Exception {

    public ArchivingException() {
        super();
    }

    public ArchivingException(String errorMessage) {
        super(errorMessage);
    }

    public ArchivingException(Throwable err) {
        super(err);
    }

    public ArchivingException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

}
