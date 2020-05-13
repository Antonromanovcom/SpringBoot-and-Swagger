package online.prostobank.clients.domain.events;

import lombok.Getter;
import online.prostobank.clients.services.dbo.model.DboRequestCreateUserDto;
import org.springframework.context.ApplicationEvent;


/**
 * Отправка информации о регистрации клиента в keycloak
 */
@Getter
public class ClientRegisteredInKeycloakFofDbo extends ApplicationEvent {
	private String tempPassword;
	private String login;
	private String phone;

	public ClientRegisteredInKeycloakFofDbo(DboRequestCreateUserDto dboDto, String tempPassword, String login, String phone) {
		super(dboDto);
		this.tempPassword = tempPassword;
		this.login = login;
		this.phone = phone;
	}
}
