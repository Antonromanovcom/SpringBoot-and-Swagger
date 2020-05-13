package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


/**
 * Попытка повторно завести заявку на ИНН, на который уже заведена заявка
 */
@Getter
public class AccountApplicationDuplicateAttemptEvent extends ApplicationEvent {
	private String phone;
	private String inn;

	public AccountApplicationDuplicateAttemptEvent(Long appId, String phone, String inn) {
		super(appId);
		this.phone = phone;
		this.inn = inn;
	}
}
