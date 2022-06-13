package Exception;

public class SuchBookAlreadyExistsException extends RuntimeException {
    private String message;

    public SuchBookAlreadyExistsException() {
        super();
    }

    public SuchBookAlreadyExistsException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
