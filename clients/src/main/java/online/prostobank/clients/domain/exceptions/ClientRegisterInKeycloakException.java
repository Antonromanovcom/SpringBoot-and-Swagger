package online.prostobank.clients.domain.exceptions;

public class ClientRegisterInKeycloakException extends RuntimeException {
    private final int registerStatus;

    public ClientRegisterInKeycloakException(int registerStatus) {
        super();
        this.registerStatus = registerStatus;
    }

    public int getRegisterStatus() {
        return registerStatus;
    }
}
