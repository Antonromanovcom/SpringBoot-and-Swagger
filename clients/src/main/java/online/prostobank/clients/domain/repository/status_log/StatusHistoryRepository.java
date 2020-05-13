package online.prostobank.clients.domain.repository.status_log;

import online.prostobank.clients.domain.StatusHistoryItem;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;

import java.util.List;

public interface StatusHistoryRepository {
	/**
	 * Сохранение журнальной записи о смене состояния карточки клиента.
	 * @param clientId
	 * @param previousStatus
	 * @param event
	 * @param createdBy
	 * @param causeMessage
	 */
	void insertStatusHistory(Long clientId, ClientStates previousStatus, ClientEvents event, ClientStates newStatus, String createdBy, String causeMessage);

	/**
	 * Получение журнальных записей о смене состояния карточки клиента
	 * @param clientId
	 * @return
	 */
	List<StatusHistoryItem> selectAllByClientIdOrdered(Long clientId);
}
