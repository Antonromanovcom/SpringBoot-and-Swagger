package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;


/**
 * Отправка СМС-напоминания
 */
public class SmsReminderEvent extends AccountApplicationEvent {
    private String href;

    public SmsReminderEvent(AccountApplication app, String href) {
        super(app);
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}
