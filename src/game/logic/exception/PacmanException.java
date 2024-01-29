package game.logic.exception;

public class PacmanException extends Exception {
    // TODO: provide information for alert
    // TODO: make class abstract
    // TODO: provide information what to after exception


    public PacmanException() {
    }

    public PacmanException(String message) {
        super(message);
    }

    public PacmanException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacmanException(Throwable cause) {
        super(cause);
    }

    public PacmanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
