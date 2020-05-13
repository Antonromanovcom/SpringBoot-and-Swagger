package online.prostobank.clients.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


/**
 * Зарегистрировать пользователя (клиента) в киклоаке
 */
@Getter
public class ClientNeedRegisterInKeycloakFofDbo extends ApplicationEvent {
	private final Long appId;
	private final String login;
	private final String phone;
	private final String firstName;
	private final String lastName;
	private final String middleName;
	private final String inn;

	public ClientNeedRegisterInKeycloakFofDbo(Long appId, String login, String phone, String firstName, String lastName, String middleName, String inn) {
		super(appId);
		this.appId = appId;
		this.login = login;
		this.phone = phone;
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleName = middleName;
		this.inn = inn;
	}
}
