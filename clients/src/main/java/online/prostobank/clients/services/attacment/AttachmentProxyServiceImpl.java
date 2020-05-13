package online.prostobank.clients.services.attacment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.state.StateSetterDTO;
import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.attachment.DocumentClass;
import online.prostobank.clients.domain.attachment.DocumentDTO;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.services.StorageException;
import online.prostobank.clients.services.state.StateMachineServiceI;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Сервис для работы с вложениями. В зависимости от значения toggleConfig использует либо сервис работы с БД, либо
 * сервис работы с внешним хранилищем. При работе с внешним хранилищем файлы дублируются в БД (чтобы не нарушать работу
 * с документами со стороны ПОС).
 *
 * В рамках APIKUB-2219 поддержка работы с БД удалена.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentProxyServiceImpl implements AttachmentService {
    private final AttachmentStorageService storageService;
    private final StateMachineServiceI stateMachineService;

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
    @Override
    public Attachment createAttachment(@Nullable Long clientId, @Nullable String attachmentName, @Nullable Instant when,
                                       @Nullable byte[] content, @Nullable String mimeType, @Nullable AttachmentFunctionalType functionalType,
                                       DocumentClass documentClass) throws StorageException {
        if (clientId == null || attachmentName == null || content == null || mimeType == null || functionalType == null || documentClass == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Создание документа для clientId = {}; name = {}; documentClass = {}; size = {}", clientId, attachmentName, documentClass.getName(), content.length);
        if (documentClass.equals(DocumentClass.USER)) {
            stateMachineService.setState(new StateSetterDTO(clientId, ClientEvents.AT_LEAST_ONE_DOCUMENTS_LOADED));
        }
        return storageService.createAttachment(clientId, attachmentName, when, content, mimeType, functionalType, documentClass);
    }

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
    @Override
    public Attachment createAttachment(@Nullable Long clientId, @Nullable String attachmentName, @Nullable Instant when,
                                       @Nullable byte[] content, @Nullable String mimeType, DocumentClass documentClass) throws StorageException {
        if (clientId == null || attachmentName == null || content == null || mimeType == null || documentClass == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Создание документа для clientId = {}; name = {}; documentClass = {}; size = {}", clientId, attachmentName, documentClass.getName(), content.length);
        if (documentClass.equals(DocumentClass.USER)) {
            stateMachineService.setState(new StateSetterDTO(clientId, ClientEvents.AT_LEAST_ONE_DOCUMENTS_LOADED));
        }
        return storageService.createAttachment(clientId, attachmentName, when, content, mimeType, documentClass);
    }

    /**
     * Получение бинарных данных вложения
     * @param attachment
     * @return
     */
    @Override
    public byte[] getBinaryContent(Attachment attachment) {
        if (attachment == null) {
            return new byte[0];
        }
        log.info("Запрос бинарного контента для attachmentId = {}", attachment.getId());
        return storageService.getBinaryContent(attachment);
    }

    /**
     * Получение перечня пользовательских документов
     * @param clientId
     * @return
     */
    @Override
    public Set<Attachment> getUserAttachments(Long clientId) throws StorageException {
        if (clientId == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Запрос пользовательских документов для clientId = {}", clientId);
        return storageService.getUserAttachments(clientId);
    }

    /**
     * Получение перечня банковских документов
     * @param clientId
     * @return
     */
    @Override
    public Set<Attachment> getBankAttachments(Long clientId) throws StorageException {
        if (clientId == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Запрос банковских документов для clientId = {}", clientId);
        return storageService.getBankAttachments(clientId);
    }

    /**
     * Удаление документа
     * @param attachment
     */
    @Override
    public void deleteAttachment(Attachment attachment) throws StorageException {
        if (attachment == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Удаление вложения attachmentId = {}", attachment.getId());
        storageService.deleteAttachment(attachment);
    }

    /**
     * Получение числа документов в указанном классе (пользовательские/банковские)
     * @param clientId
     * @param documentClass
     * @return
     */
    @Override
    public int getAttachmentCount(Long clientId, DocumentClass documentClass) throws StorageException {
        if (clientId == null || documentClass == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Запрос числа документов clientId = {}; documentClass = {}", clientId, documentClass.getName());
        return storageService.getAttachmentCount(clientId, documentClass);
    }

    /**
     * Изменение имени документа
     * @param attachment
     * @param newName
     * @throws StorageException
     */
    @Override
    public Optional<DocumentDTO> editAttachmentName(Attachment attachment, String newName) throws StorageException {
        if (attachment == null || newName == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Изменение имени вложения attachmentId = {}; newName = {}", attachment.getId(), newName);
        return storageService.editAttachmentName(attachment, newName);
    }

    /**
     * Изменить флаг проверки качества документа
     * @param attachment
     * @param isQuality
     * @return
     * @throws StorageException
     */
    @Override
    public Optional<DocumentDTO> editAttachmentQuality(Attachment attachment, boolean isQuality) throws StorageException {
        if (attachment == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Изменение флага качества вложения attachmentId = {}; isQuality = {}", attachment.getId(), isQuality);
        return storageService.editAttachmentQuality(attachment, isQuality);
    }

    /**
     * Изменить флаг заверения документа
     * @param attachment
     * @param isVerify
     * @return
     * @throws StorageException
     */
    @Override
    public Optional<DocumentDTO> editAttachmentVerification(Attachment attachment, boolean isVerify) throws StorageException {
        if (attachment == null) {
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Изменение флага заверения вложения attachmentId = {}; isVerify = {}", attachment.getId(), isVerify);
        return storageService.editAttachmentVerification(attachment, isVerify);
    }

    /**
     * Поиск вложения по целочисленному идентификатору
     *
     * @param attachId
     * @return
     */
    @Override
    public Optional<Attachment> findById(Long clientId, Long attachId) {
        if (clientId == null || attachId == null) {
            log.error("Не указаны требуемые параметры clientId = {}; attachId = {}", clientId, attachId);
            return Optional.empty();
        }
        log.info("Поиск вложения clientId = {}; attachId = {}", clientId, attachId);
        try {
            return Stream.concat(getUserAttachments(clientId).stream(), getBankAttachments(clientId)
                    .stream())
                    .filter(it -> attachId.equals(it.getId()))
                    .findFirst();
        } catch (StorageException ex) {
            log.info("Поиск вложения не привел к успеху clientId = {}; attachId = {}", clientId, attachId);
        }
        return Optional.empty();
    }

    @Override
    public String getZipUserAttachmentUrl() {
        return storageService.getZipUserAttachmentUrl();
    }

    @Override
    public HttpEntity getHttpEntityForZipRequest(Map<String, String> attachmentsMap) {
        return storageService.getHttpEntityForZipRequest(attachmentsMap);
    }

    @PostConstruct
    public void init() {
        log.info("Прокси-сервис работы с документами инициализирован");
    }
}
