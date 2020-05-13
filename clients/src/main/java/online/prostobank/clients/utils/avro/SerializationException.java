package online.prostobank.clients.utils.avro;

public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException() {
        super();
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
