package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Редактирование заявки
 */
public class EmarsysSaveEditEvent extends AccountApplicationEvent {

    public EmarsysSaveEditEvent(AccountApplication app) {
        super(app);
    }
}
