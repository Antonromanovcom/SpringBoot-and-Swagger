package online.prostobank.clients.services.client;

import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.*;
import online.prostobank.clients.api.dto.rest.AttachmentDTO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientService {
	/**
	 * Сохранение информации по клиенту после редактирования
	 *
	 * @param dto   - набор изменений
	 * @return - объект с изменениями
	 */
	Optional<ClientEditDTO> saveEditClientInfo(ClientEditDTO dto) throws Exception;

	/**
	 * Сохранение информации опросника после редактирования
	 *
	 * @param dto   - набор изменений
	 * @return - объект с изменениями
	 */
	Optional<QuestionnaireDTO> saveEditQuestionnaire(QuestionnaireDTO dto);

	/**
	 * Список клиентов с фильтрацией
	 *
	 * @param dto   - набор фильтров
	 * @return Список клиентов с фильтрацией
	 */
	Optional<ClientGridResponse> getAll(ClientGridRequest dto);

	/**
	 * Получение информации клиента по его id
	 *
	 * @param clientId - id клиента
	 * @param roles    - роли юзера
	 * @return - инфо по клиенту
	 */
	Optional<ClientCardDTO> findById(long clientId, Collection<String> roles);

	/**
	 * Выполнить проверку по клиенту
	 *
	 * @param id    - id клиента
	 * @param check - тип проверки
	 * @return - результат
	 */
	Optional<ClientCardDTO> checkClient(Long id, CheckType check);

	/**
	 * Сохранить комментарий
	 *
	 * @param id       - id клиента
	 * @param text     - текст комментария
	 * @param username - логин пользователя
	 * @return - результат
	 */
	Optional<ClientCardDTO> saveComment(Long id, String text, String username);

	/**
	 * Перевыслать пользователю
	 *
	 * @param id - id клиента
	 * @return - результат
	 */
	Optional<Boolean> resendToUser(Long id);

	/**
	 * Отправка СМС-напоминания
	 *
	 * @param id - id клиента
	 * @return - результат
	 */
	Optional<String> smsReminder(Long id);

	/**
	 * Экспорт в PDF
	 *
	 * @param id - id клиента
	 * @return - результат
	 */
	Optional<byte[]> exportPdf(Long id);

	/**
	 * @param id   - id клиента
	 * @param user - имя юзера
	 * @return - результат
	 */
	Optional<Boolean> assignToMe(Long id, String user);

	/**
	 * @param id   - id клиента
	 * @param name - имя юзера
	 * @return - результат
	 */
	Optional<Boolean> startWork(Long id, String name);

	/**
	 * @param id      - id клиента
	 * @param newUser - логин пользователя
	 * @return - результат
	 */
	Optional<Boolean> assignTo(Long id, String newUser);

	/**
	 * @param id - id клиента
	 * @return - результат
	 */
	Optional<String> smsCheck(Long id);

	/**
	 * @param id   - id клиента
	 * @param code - код из смс
	 * @return - результат
	 */
	Optional<Boolean> smsConfirmation(Long id, String code);

	/**
	 * @param id - id клиента
	 * @return - результат
	 */
	Optional<Boolean> resetSmsCheck(Long id);

	/**
	 * Наличие заявок у клиента
	 *
	 * @param name - логин клиента
	 * @return - наличие заявок
	 */
	Optional<Boolean> existApplications(String name);

	/**
	 * Получение списка документов клиента
	 *
	 * @param name - логин клиента
	 * @return - результат
	 */
	Optional<List<AttachmentDTO>> attachmentList(String name);

	/**
	 * Создать карточку клиента
	 *
	 * @param dto - dto с информацией
	 * @return - результат
	 */
	ResponseDTO createClientCard(ClientCardCreateDTO dto);

	/**
	 * ПОС оповещает, что счет успешно открыт
	 *
	 * @param clientId - id клиента
	 * @param appId    - id заявки
	 * @return - результат
	 */
	Optional<?> fulfilled(Long clientId, Long appId);

	/**
	 * ПОС оповещает, что счет успешно зарезервирован
	 *
	 * @param clientId      - id клиента
	 * @param appId         - id заявки
	 * @param accountNumber - номер счета
	 * @param requestId     - идентификатор запроса на создание заявки на открытие счета
	 * @return - результат
	 */
	Optional<?> reserved(Long clientId, Long appId, String accountNumber, String requestId);

	/**
	 * Список клиентов по признаку назначения
	 *
	 * @param assignedTo - имя пользователя, для которого ищутся назначенные ему карточки
	 * @return - результат
	 */
	Optional<ClientGridResponse> getByAssignedTo(String assignedTo);

	/**
	 * Изменение данных паспорта
	 *
	 * @param clientId    - id клиента
	 * @param passportDTO - данные паспорта
	 * @return - результат
	 */
	Optional<PassportDTO> editPassport(Long clientId, PassportDTO passportDTO);

	/**
	 * Отправка сообщения клиенту от менеджера
	 *
	 * @param message - сообщение
	 * @param manager - менеджер
	 * @param name    - логин менеджера
	 * @return - результат
	 */
	Optional<Boolean> sendMessageToClient(MessageDTO message, UUID manager, String name);

	/**
	 * Сохранение записи системной истории
	 *
	 * @param historyItemDTO - DTO
	 */
	void saveHistoryItem(HistoryItemDTO historyItemDTO, String initiator) throws Exception;

	/**
	 * Проверка 550-П для списка инн, асинхронно
	 *
	 * @param inns - список инн
	 * @return строку started
	 */
	Optional<?> recheckP550(List<String> inns);

	/**
	 * Прием сигнала от ПОС о необходимости добавить документы
	 * @param clientId
	 * @return
	 */
	Optional<Boolean> docsNeedSignal(Long clientId);

	/**
	 * Прием сигнала от ПОС о деактивации заявки
	 * @param clientId
	 * @return
	 */
	Optional<Boolean> accountDecline(Long clientId, String causeMessage);

	/**
	 * Изменение времени дозвона и наименовния тарифа в карточке клиента
	 * @param dto
	 * @return
	 */
	Optional<ClientAuxInfoDTO> setClientAuxInfo(Long clientId, ClientAuxInfoDTO dto);
}
