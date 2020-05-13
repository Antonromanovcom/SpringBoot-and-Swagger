package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Сообщение о создании сущности заявки
 */
public class AccountApplicationEntityCreatedEvent extends AccountApplicationEvent {

    public AccountApplicationEntityCreatedEvent(AccountApplication app) {
        super(app);
    }
}
