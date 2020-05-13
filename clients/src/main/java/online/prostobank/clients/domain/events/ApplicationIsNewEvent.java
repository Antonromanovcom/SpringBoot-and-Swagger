package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Сообщение, что заявка была успешно переведена в состояние new
 */
public class ApplicationIsNewEvent extends AccountApplicationEvent {

    public ApplicationIsNewEvent(AccountApplication app) {
        super(app);
    }
}

