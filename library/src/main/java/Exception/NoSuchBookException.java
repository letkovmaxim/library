package Exception;

public class NoSuchBookException extends RuntimeException {
    private String message;

    public NoSuchBookException() {
        super();
    }

    public NoSuchBookException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
