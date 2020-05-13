package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationCreator extends ApplicationEvent {
	private Long clientId;

	public ApplicationCreator(Long clientId) {
		super(clientId);
		this.clientId = clientId;
	}
}
