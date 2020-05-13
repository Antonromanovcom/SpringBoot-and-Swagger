package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;
import org.springframework.context.ApplicationEvent;

public abstract class AccountApplicationEvent extends ApplicationEvent {

    public AccountApplicationEvent(AccountApplication app) {
        super(app);
    }

    public AccountApplication getAccountApplication() {
        return (AccountApplication) source;
    }
}
