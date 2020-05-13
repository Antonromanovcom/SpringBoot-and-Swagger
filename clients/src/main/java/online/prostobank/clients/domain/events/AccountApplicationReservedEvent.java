package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Заявка была успешно зарезервирована
 */
@Getter
public class AccountApplicationReservedEvent extends ApplicationEvent {
    private  final Long clientId;
    private  final String phone;

    public AccountApplicationReservedEvent(Long clientId, String phone) {
        super(clientId);
        this.clientId = clientId;
        this.phone = phone;
    }
}
