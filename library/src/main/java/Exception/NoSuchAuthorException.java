package Exception;

public class NoSuchAuthorException extends RuntimeException {
    private String message;

    public NoSuchAuthorException() {
        super();
    }

    public NoSuchAuthorException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
