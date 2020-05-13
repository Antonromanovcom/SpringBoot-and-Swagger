package online.prostobank.clients.services.attacment;

import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.attachment.DocumentClass;
import online.prostobank.clients.domain.attachment.DocumentDTO;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.services.StorageException;
import org.springframework.http.HttpEntity;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Сервис для работы с вложениями. Именно этот интерфейс обращен в сторону приложения. За ним скрываются имплементации
 * для работы с внешним хранилищем, или с БД, или то и другое одновременно.
 */
public interface AttachmentService {
	/**
	 * Сохранение вложения (документа)
	 * @param clientId -  идентификатор карточки клиента
	 * @param attachmentName
	 * @param when
	 * @param content
	 * @param mimeType
	 * @param functionalType
	 * @return
	 * @throws StorageException
	 */
	Attachment createAttachment(@Nullable Long clientId, @Nullable String attachmentName, @Nullable Instant when, @Nullable byte[] content,
								@Nullable String mimeType, @Nullable AttachmentFunctionalType functionalType, DocumentClass documentClass)  throws StorageException;


	/**
	 * Сохранение вложения (документа)
	 * @param clientId -  идентификатор карточки клиента
	 * @param attachmentName
	 * @param when
	 * @param content
	 * @param mimeType
	 * @return
	 * @throws StorageException
	 */
	Attachment createAttachment(@Nullable Long clientId, @Nullable String attachmentName, @Nullable Instant when, @Nullable byte[] content,
								@Nullable String mimeType, DocumentClass documentClass)
			throws StorageException;

	/**
	 * Получение бинарных данных вложения
	 * @param attachment
	 * @return
	 */
	byte[] getBinaryContent(Attachment attachment);

	/**
	 * Получение перечня пользовательских документов
	 * @param clientId
	 * @return
	 */
	Set<Attachment> getUserAttachments(Long clientId) throws StorageException;

	/**
	 * Получение перечня банковских документов
	 * @param clientId
	 * @return
	 */
	Set<Attachment> getBankAttachments(Long clientId) throws StorageException;

	/**
	 * Удаление документа
	 * @param attachment
	 */
	void deleteAttachment(Attachment attachment) throws StorageException;

	/**
	 * Получение числа документов в указанном классе (пользовательские/банковские)
	 * @param clientId
	 * @param documentClass
	 * @return
	 */
	int getAttachmentCount(Long clientId, DocumentClass documentClass) throws StorageException;

	/**
	 * Изменение имени документа
	 * @param attachment
	 * @param newName
	 * @throws StorageException
	 */
	Optional<DocumentDTO> editAttachmentName(Attachment attachment, String newName) throws StorageException;

	/**
	 * Изменить флаг проверки качества документа
	 * @param attachment
	 * @param isQuality
	 * @return
	 * @throws StorageException
	 */
	Optional<DocumentDTO> editAttachmentQuality(Attachment attachment, boolean isQuality) throws StorageException;

	/**
	 * Изменить флаг заверения документа
	 * @param attachment
	 * @param isVerify
	 * @return
	 * @throws StorageException
	 */
	Optional<DocumentDTO> editAttachmentVerification(Attachment attachment, boolean isVerify) throws StorageException;

	/**
	 * Поиск вложения по целочисленному идентификатору
	 * @param clientId
	 * @param attachmentId
	 * @return
	 */
	Optional<Attachment> findById(Long clientId, Long attachmentId);

	/**
	 * Получить url для формирования запроса к хранилищу на получение zip-архива документов
	 * @return
	 */
	String getZipUserAttachmentUrl();

	/**
	 * Получить объект http-запроса для получения из хранилища zip-архива документов с указаннымы путями (значение в мапе)
	 * и присвоить им в архиве указанные имена (ключи в мапе).
	 * @return
	 */
	HttpEntity getHttpEntityForZipRequest(Map<String, String> attachmentsMap);
}
