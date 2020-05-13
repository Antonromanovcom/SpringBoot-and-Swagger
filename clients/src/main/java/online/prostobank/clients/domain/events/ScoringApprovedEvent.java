package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Успешная проверка скоринга
 */
public class ScoringApprovedEvent extends AccountApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param notification the object on which the event initially occurred (never {@code null})
     */
    public ScoringApprovedEvent(AccountApplication notification) {
        super(notification);
    }

}

