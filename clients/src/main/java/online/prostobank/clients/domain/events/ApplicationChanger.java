package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationChanger extends ApplicationEvent {
	private Long clientId;

	public ApplicationChanger(Long clientId) {
		super(clientId);
		this.clientId = clientId;
	}
}
