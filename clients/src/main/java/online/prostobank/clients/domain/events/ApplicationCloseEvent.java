package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

public class ApplicationCloseEvent extends AccountApplicationEvent{

    public ApplicationCloseEvent(AccountApplication app) {
        super(app);
    }
}
