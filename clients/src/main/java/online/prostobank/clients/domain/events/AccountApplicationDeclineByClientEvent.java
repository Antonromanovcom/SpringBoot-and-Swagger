package online.prostobank.clients.domain.events;

import lombok.Getter;
import online.prostobank.clients.domain.AccountApplication;

/**
 * Отказ клиента
 */
@Getter
public class AccountApplicationDeclineByClientEvent {

    private String oldState;
    private AccountApplication app;

    public AccountApplicationDeclineByClientEvent(AccountApplication app, String oldState) {
        this.app = app;
        this.oldState = oldState;
    }
}
