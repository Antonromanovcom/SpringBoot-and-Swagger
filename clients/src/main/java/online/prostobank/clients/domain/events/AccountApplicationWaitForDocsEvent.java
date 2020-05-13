package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Ожидание документов
 */
public class AccountApplicationWaitForDocsEvent extends AccountApplicationEvent {

    public AccountApplicationWaitForDocsEvent(AccountApplication app) {
        super(app);
    }
}