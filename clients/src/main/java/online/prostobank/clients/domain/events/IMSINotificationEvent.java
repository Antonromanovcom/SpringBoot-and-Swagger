package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.ImsiNotification;
import org.springframework.context.ApplicationEvent;


/**
 * Обнаружена смена сим-карты пользователем
 */
public class IMSINotificationEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param notification the object on which the event initially occurred (never {@code null})
     */
    public IMSINotificationEvent(ImsiNotification notification) {
        super(notification);
    }

    public ImsiNotification getImsiNotification() {
        return (ImsiNotification) source;
    }
}
