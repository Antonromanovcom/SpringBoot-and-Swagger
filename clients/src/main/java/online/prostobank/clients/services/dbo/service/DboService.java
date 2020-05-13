package online.prostobank.clients.services.dbo.service;

import online.prostobank.clients.services.dbo.model.DboRequestCreateUserDto;

public interface DboService {
	/**
	 * После регистрации клиента в keycloak отправляем информацию в ДБО
	 *
	 * @param dto - отправляемая информация
	 */
	void sentNewUser(DboRequestCreateUserDto dto);
}
