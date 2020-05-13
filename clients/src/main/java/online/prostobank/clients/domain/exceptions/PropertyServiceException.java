package online.prostobank.clients.domain.exceptions;

import javax.annotation.Nonnull;

public class PropertyServiceException extends Exception {
    public PropertyServiceException(@Nonnull String errorMessage) {
        super(errorMessage);
    }
}

