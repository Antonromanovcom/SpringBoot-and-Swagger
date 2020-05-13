package online.prostobank.clients.domain.events;

import lombok.Getter;
import online.prostobank.clients.api.dto.anketa.UtmDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Отправка ютм в гугл по пути создания заявок
 */
@Getter
public class SendUtmEvent extends ApplicationEvent {
	private String clientState;
    private Long appId;

	public SendUtmEvent(UtmDTO dto, String clientState, Long appId) {
		super(dto);
		this.clientState = clientState;
		this.appId = appId;
	}

	public UtmDTO getUtm() {
		return (UtmDTO) source;
	}
}
