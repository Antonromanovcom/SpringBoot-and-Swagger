package online.prostobank.clients.services.forui;

import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.ClientCardCreateDTO;
import online.prostobank.clients.domain.City;

public interface ColdApplicationService {
	/**
	 * Создание холодной заявки
	 *
	 * @param dto  - dto с информацией
	 * @param city - город
	 * @return - результат
	 */
	ResponseDTO createColdApplication(ClientCardCreateDTO dto, City city);
}
