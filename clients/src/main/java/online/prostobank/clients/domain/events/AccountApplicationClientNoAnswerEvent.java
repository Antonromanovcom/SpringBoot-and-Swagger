package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;


/**
 * Недозвон
 */
public class AccountApplicationClientNoAnswerEvent extends AccountApplicationEvent {

    public AccountApplicationClientNoAnswerEvent(AccountApplication app) {
        super(app);
    }
}