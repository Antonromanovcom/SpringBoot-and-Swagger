package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Событие о том, что переданы контактные данные (телефон, город) для подтверждения
 */
public class ConfirmationCodeGeneratedEvent extends AccountApplicationEvent {

    public ConfirmationCodeGeneratedEvent(AccountApplication app) {
        super(app);
    }
}