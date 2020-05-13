package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationDocumentNotifier extends ApplicationEvent {
	private final Long clientId;

	public ApplicationDocumentNotifier(Long clientId) {
		super(clientId);
		this.clientId = clientId;
	}
}
