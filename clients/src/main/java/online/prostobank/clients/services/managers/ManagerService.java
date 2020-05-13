package online.prostobank.clients.services.managers;

import online.prostobank.clients.api.dto.client.ClientGridRequest;
import online.prostobank.clients.api.dto.managers.*;

import java.util.Optional;
import java.util.UUID;

public interface ManagerService {
	/**
	 * Проверяет и сохраняет тех сотрудников, который есть в кейлоке, но еще нет в базе
	 * (используется при инициализации общего списка сотрудников)
	 */
	void saveNotExists();

	/**
	 * Список менеджеров
	 *
	 * @param dto - набор фильтров
	 * @return - список менеджеров
	 */
	Optional<ManagerGridResponse> getAll(ManagerGridRequest dto);

	/**
	 * Найти менеджера по uuid
	 *
	 * @param uuid - uuid менеджера
	 * @return dto менеджера
	 */
	Optional<ManagerDTO> findById(UUID uuid);

	/**
	 * Обновляет информацию менеджера
	 *
	 * @param dto - новая информация
	 * @return dto менеджера
	 */
	Optional<ManagerDTO> saveEdit(ManagerDTO dto);

	/**
	 * Переназначить на пользователя
	 *
	 * @param dto
	 * @return
	 */
	Optional<?> assignTo(ManagerAssignDTO dto);

	/**
	 * Список клиентов с фильтрацией
	 *
	 * @param dto
	 * @return
	 */
	Optional<ClientForManagerGridResponse> getAllClients(ClientGridRequest dto);
}
