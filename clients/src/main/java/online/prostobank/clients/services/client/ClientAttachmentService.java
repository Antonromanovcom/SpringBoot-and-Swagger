package online.prostobank.clients.services.client;

import online.prostobank.clients.api.dto.client.ClientAttachmentHelpDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.Attachment;
import org.springframework.http.HttpEntity;

import java.util.Optional;

public interface ClientAttachmentService {
	/**
	 * Загрузить вложение
	 *
	 * @param initiator - имя юзера
	 * @param dto       - инфо по вложению
	 * @return - инфо по загруженному вложению
	 */
	Optional<Attachment> upload(String initiator, ClientAttachmentHelpDTO dto);

	/**
	 * Скачать вложение
	 *
	 * @param attachmentId - id вложения
	 * @return - вложение
	 */
	Optional<byte[]> download(Long clientId, Long attachmentId);

	/**
	 * Скачать все вложения класса клиента
	 *
	 * @param clientId - id клиента
	 * @param classId  - id класса
	 * @return - архив с вложениями
	 */
	Optional<byte[]> downloadAll(Long clientId, ClientAttachmentClass classId);

	/**
	 * Получить url для формирования запроса к хранилищу на получение zip-архива документов
	 * @return
	 */
	String getZipUserAttachmentUrl();

	/**
	 * Получить объект http-запроса для получения из хранилища zip-архива документов указанного клиента и указанного класса
	 * @param clientId
	 * @param classId
	 * @return
	 */
	Optional<HttpEntity> getHttpEntityForZipAttachmentsRequest(Long clientId, ClientAttachmentClass classId);

	/**
	 * Получить объект http-запроса для получения из хранилища zip-архива документов пользователя с флагом "заверено"
	 * @param clientId
	 * @return
	 */
	Optional<HttpEntity> getHttpEntityForZipVerifiedAttachmentsRequest(Long clientId);

	/**
	 * Скачать все вложения клиента для заверения
	 *
	 * @param clientId - id клиента
	 * @return - архив с вложениями
	 */
	Optional<byte[]> downloadToVerify(Long clientId);

	/**
	 * Переименовать вложение
	 *
	 * @param initiator    - имя юзера
	 * @param clientId     - id клиента
	 * @param attachmentId - id вложения
	 * @param name         - новое имя
	 * @return - инфо по вложению
	 */
	Optional<Attachment> rename(String initiator, Long clientId, Long attachmentId, String name);

	/**
	 * Удалить вложение
	 *
	 * @param initiator    - имя юзера
	 * @param clientId     - id клиента
	 * @param attachmentId - id вложения
	 * @return - инфо по вложению
	 */
	Optional<Boolean> delete(String initiator, Long clientId, Long attachmentId);

	/**
	 * Установить качество вложения
	 *
	 * @param initiator    - имя юзера
	 * @param clientId     - id клиента
	 * @param attachmentId - id вложения
	 * @param quality      - флаг
	 * @return - инфо по вложению
	 */
	Optional<Attachment> setQuality(String initiator, Long clientId, Long attachmentId, boolean quality);

	/**
	 * Пометить вложение для заверения
	 *
	 * @param initiator    - имя юзера
	 * @param clientId     - id клиента
	 * @param attachmentId - id вложения
	 * @param verified     - флаг
	 * @return - инфо по вложению
	 */
	Optional<Attachment> setVerified(String initiator, Long clientId, Long attachmentId, boolean verified);

	/**
	 * Распознать вложение
	 *
	 * @param initiator    - имя юзера
	 * @param clientId     - id клиента
	 * @param attachmentId - id вложения
	 * @return - инфо по карточке клиента
	 */
	Optional<AccountApplication> recognize(String initiator, Long clientId, Long attachmentId);

	Optional<Attachment> findByClientAndId(Long clientId, Long attachmentId);
}
