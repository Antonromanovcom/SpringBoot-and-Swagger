package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Сообщение, что документы были успешно прикреплены к заявлению через email
 */
public class ApplicationAttachmentSuccessEvent extends AccountApplicationEvent {

    public ApplicationAttachmentSuccessEvent(AccountApplication app) {
        super(app);
    }
}