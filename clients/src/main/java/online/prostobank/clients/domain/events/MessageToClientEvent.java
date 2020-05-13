package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Отправка сообщения клиенту от менеджера
 */
@Getter
public class MessageToClientEvent extends ApplicationEvent {
	private final Long clientId;
	private final String message;
	private final String email;
	private final String phone;

	public MessageToClientEvent(Long clientId, String message, String email, String phone) {
		super(clientId);
		this.clientId = clientId;
		this.message = message;
		this.email = email;
		this.phone = phone;
	}
}
