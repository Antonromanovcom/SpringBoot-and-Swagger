package online.prostobank.clients.services.dbo.service;

import online.prostobank.clients.domain.events.ClientNeedRegisterInKeycloakFofDbo;

public interface ClientRegisterService {
	/**
	 * После успеха резервирования счета нужно зарегистрировать учетную запись клиента в Keycloak для взамодействия с ДБО
	 *
	 * @param event event с информацией
	 * @return статус регистрации в Keycloak
	 */
	int clientRegisterInKeycloak(ClientNeedRegisterInKeycloakFofDbo event);

	/**
	 * После перехода клиента в статус Счет открыт нужно удалить старую роль и назначить новую
	 *
	 * @param applicationId - id завки
	 */
	void clientChangeRoleInKeycloak(Long applicationId);

	/**
	 * Направить на регистрацию в кейклок клиентов в статусе счет зарезервирован
	 */
	void findClientAndRegisterInKeycloak();
}
