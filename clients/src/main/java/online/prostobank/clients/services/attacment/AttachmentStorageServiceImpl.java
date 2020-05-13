package online.prostobank.clients.services.attacment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.attachment.*;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.security.UserService;
import online.prostobank.clients.services.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getTokenString;

/**
 * Сервис доступа к модулю хранения документов (внешнему хранилищу).
 */
@Slf4j
@Service
public class AttachmentStorageServiceImpl implements AttachmentStorageService {

    private final RestTemplate restTemplate;
    private final String storageUrl;
    private final String hostName;
    private final UserService userService;

    private static final String DOCUMENT_SCHEMA = "%s/document/%s"; //используется при удалении
    private static final String DOCUMENT_EDIT_SCHEMA = "%s/document"; //используется при редактировании метаданных
    private static final String FILE_SCHEMA = "%s/file/%s"; //используется при создании файла и загрузки бинарника в хранилище
    private static final String BINARY_SCHEMA = "%s/binary/%s"; //используется при получении бинарника из хранилища
    private static final String ZIP_SCHEMA = "%s/zip"; //используется при получении архива из перечня бинарников из хранилища
    private static final String POST_PARAMETER_BINARY = "file";
    private static final String POST_PARAMETER_FILENAME = "filename";
    private static final String POST_PARAMETER_DESCRIPTION = "description";
    private static final String ALL_DOCUMENT_FOLDER_SCHEMA = "%s/user/%s/folder"; //используется при получении всей папки с документами
    private static final String USER_DOCUMENT_IN_CLASS_SCHEMA = "%s/user/%s/class/%s/document"; //используется при создании документа


    @Autowired
    public AttachmentStorageServiceImpl(@Value("${storage.url}") String storageUrl, @Value("${storage.host:}") String hostName, UserService userService) {
        this.restTemplate = new RestTemplate();
        this.storageUrl = storageUrl;
        this.hostName = hostName;
        this.userService = userService;
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    /**
     * Сохранение вложения (документа) в content-storage
     *
     * @param clientId       -  идентификатор карточки клиента
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
        if (clientId == null || attachmentName == null || content == null || mimeType == null || functionalType == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Создание документа для clientId = {}; name = {}; documentClass = {}; size = {}", clientId, attachmentName, documentClass.getName(), content.length);
        return createOrReplaceAttachment(clientId, attachmentName, when, content, mimeType, functionalType, jwtToken(), documentClass);
    }

    /**
     * Сохранение вложения (документа) в content-storage
     *
     * @param clientId       -  идентификатор карточки клиента
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
        if (clientId == null || attachmentName == null || content == null || mimeType == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Создание документа для clientId = {}; name = {}; documentClass = {}; size = {}", clientId, attachmentName, documentClass.getName(), content.length);
        return createOrReplaceAttachment(clientId, attachmentName, when, content, mimeType, AttachmentFunctionalType.UNKNOWN, jwtToken(), documentClass);
    }

    /**
     * Получение перечня пользовательских документов из стораджа
     *
     * @param clientId
     * @return
     */
    @Override
//    @Cacheable("userAttachments") //временно отключен кэш
    public Set<Attachment> getUserAttachments(Long clientId) throws StorageException {
        if (clientId == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Запрос пользовательских документов для clientId = {}", clientId);
        return getAttachmentsByClass(clientId, DocumentClass.USER);
    }

    /**
     * Получение перечня банковских документов из стораджа
     *
     * @param clientId
     * @return
     */
    @Override
//    @Cacheable("bankAttachments") //временно отключен кэш
    public Set<Attachment> getBankAttachments(Long clientId) throws StorageException {
        if (clientId == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Запрос банковских документов для clientId = {}", clientId);
        return getAttachmentsByClass(clientId, DocumentClass.BANK);
    }

    /**
     * Удаление (на самом деле создание версии со специальной маркировкой) документа
     *
     * @param attachment
     */
    @Override
    public void deleteAttachment(Attachment attachment) throws StorageException {
        if (attachment == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Удаление вложения attachmentId = {}", attachment.getId());
        HttpHeaders headers = getHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = String.format(DOCUMENT_SCHEMA, storageUrl, attachment.getDocumentId());
        log.info("Удаление вложения url = {}", url);
        try {
            clearCache();
            HttpEntity<VersionDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE, entity, VersionDTO.class);
            clearCache();
        } catch (HttpClientErrorException ex) {
            log.error("Ошибка при попытке удаления документа id = {}", attachment.getId());
            throw new StorageException("Ошибка при попытке удаления документа", ex);
        }
    }

    @Override
    public int getAttachmentCount(Long clientId, DocumentClass documentClass) throws StorageException {
        if (clientId == null || documentClass == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        if (documentClass == DocumentClass.USER) {
            log.info("Запрос числа пользовательских документов clientId = {}; documentClass = {}", clientId, documentClass.getName());
            return getUserAttachments(clientId).size();
        }
        if (documentClass == DocumentClass.BANK) {
            log.info("Запрос числа банковских документов clientId = {}; documentClass = {}", clientId, documentClass.getName());
            return getBankAttachments(clientId).size();
        }
        log.info("Запрос числа документов без указания класса");
        return 0;
    }

    /**
     * Изменение названия документа
     *
     * @param attachment
     * @param newName
     * @throws StorageException
     */
    @Override
    public Optional<DocumentDTO> editAttachmentName(Attachment attachment, String newName) throws StorageException {
        if (attachment == null || newName == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Изменение имени вложения attachmentId = {}; newName = {}", attachment.getId(), newName);
        DocumentDTO documentDTO = new DocumentDTO(new UniqueId(attachment.getDocumentId()));
        documentDTO.setTitle(newName);
        documentDTO.setDocumentType(attachment.getFunctionalType().getFrontendKey());

        DocumentAux documentAux = new DocumentAux();
        documentAux.setQuality(attachment.getQuality());
        documentAux.setVerified(attachment.getVerified());
        ObjectMapper mapper = new ObjectMapper();
        try {
            documentDTO.setAuxParameters(mapper.convertValue(documentAux, JsonNode.class));
        } catch (IllegalArgumentException ex) {
            log.error("Не удалось сконвертировать дополнительные сведения о документе в json", ex);
        }

        return updateAndReturnDocument(documentDTO);
    }

    /**
     * Изменить флаг проверки качества документа
     *
     * @param attachment
     * @param isQuality
     * @return
     * @throws StorageException
     */
    @Override
    public Optional<DocumentDTO> editAttachmentQuality(Attachment attachment, boolean isQuality) throws StorageException {
        if (attachment == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Изменение флага качества вложения attachmentId = {}; isQuality = {}", attachment.getId(), isQuality);
        DocumentDTO documentDTO = new DocumentDTO(new UniqueId(attachment.getDocumentId()));
        documentDTO.setTitle(attachment.getAttachmentName());
        documentDTO.setDocumentType(attachment.getFunctionalType().getFrontendKey());

        DocumentAux documentAux = new DocumentAux();
        documentAux.setQuality(isQuality);
        documentAux.setVerified(attachment.getVerified());
        ObjectMapper mapper = new ObjectMapper();
        try {
            documentDTO.setAuxParameters(mapper.convertValue(documentAux, JsonNode.class));
        } catch (IllegalArgumentException ex) {
            log.error("Не удалось сконвертировать дополнительные сведения о документе в json", ex);
        }

        return updateAndReturnDocument(documentDTO);
    }

    /**
     * Изменить флаг заверения документа
     *
     * @param attachment
     * @param isVerify
     * @return
     * @throws StorageException
     */
    @Override
    public Optional<DocumentDTO> editAttachmentVerification(Attachment attachment, boolean isVerify) throws StorageException {
        if (attachment == null) {
            log.error("Не указаны требуемые параметры");
            throw new StorageException("Не указаны требуемые параметры");
        }
        log.info("Изменение флага заверения вложения attachmentId = {}; isVerify = {}", attachment.getId(), isVerify);
        DocumentDTO documentDTO = new DocumentDTO(new UniqueId(attachment.getDocumentId()));
        documentDTO.setTitle(attachment.getAttachmentName());
        documentDTO.setDocumentType(attachment.getFunctionalType().getFrontendKey());

        DocumentAux documentAux = new DocumentAux();
        documentAux.setQuality(attachment.getQuality());
        documentAux.setVerified(isVerify);
        ObjectMapper mapper = new ObjectMapper();
        try {
            documentDTO.setAuxParameters(mapper.convertValue(documentAux, JsonNode.class));
        } catch (IllegalArgumentException ex) {
            log.error("Не удалось сконвертировать дополнительные сведения о документе в json", ex);
        }

        return updateAndReturnDocument(documentDTO);
    }

    private Set<Attachment> getAttachmentsByClass(Long clientId, DocumentClass documentClass) throws StorageException {
        Optional<FolderDTO> userFolder = getUserDocumentFolder(clientId);
        log.info("Получена папка документов пользователя clientId = {}; not_empty = {}", clientId, userFolder.isPresent());
        ObjectMapper mapper = new ObjectMapper();
        if (userFolder.isPresent()) {
            try {
                log.info("Содержимое папки {}", mapper.writeValueAsString(userFolder.get()));
            } catch (JsonProcessingException ex) {
                log.error("Не удалось расшифровать содержимое папки документов", ex);
            }
        }
        return userFolder.map(folderDTO -> folderDTO.getFolders().stream()
                .filter(folder -> documentClass.getName().equals(folder.getTitle()))
                .flatMap(folder -> folder.getDocuments().stream())
                .filter(document -> !isDeletedDocument(document.getVersions()))
                .flatMap(document -> mapDocumentToAttachments(document).stream())
                .peek(it -> it.setId(getIdForPath(it.getPath())))
                .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

    }

    private Set<Attachment> mapDocumentToAttachments(DocumentDTO sourceDocument) {
        log.info("Маппинг документа documentId = {}", sourceDocument.getId());
        Set<Attachment> result = new HashSet<>(createAttachmentFromDocument(sourceDocument, "", sourceDocument.getCreatedBy(), true));
        if (sourceDocument.getVersions() != null) {
            sourceDocument.getVersions().stream()
                    .filter(it -> !it.getName().contains("rootVersion")) //начальная версия
                    .forEach(it -> {
                        DocumentDTO mappedDocument = it.getVersionedDocument();
                        mappedDocument.setCreatedAt(it.getCreatedAt());
                        result.addAll(createAttachmentFromDocument(it.getVersionedDocument(), it.getName(), it.getVersionedDocument().getVersionCreator(), false));
                    });
        }
        return result;
    }

    private Set<Attachment> createAttachmentFromDocument(DocumentDTO versionedDocument, String versionName, String versionCreator, boolean isCurrentVersion) {
        Set<Attachment> result = new HashSet<>();
        String title = versionedDocument.getTitle();
        String type = versionedDocument.getDocumentType();
        Instant createdAt = versionedDocument.getCreatedAt();
        for (FileDTO file : versionedDocument.getFiles()) {
            String path = file.getPath().value();
            Attachment attachment = createInputAttachment(title, createdAt, "", AttachmentFunctionalType.getByStringKey(type));
            ObjectMapper mapper = new ObjectMapper();
            DocumentAux documentAux = null;
            try {
                documentAux = mapper.treeToValue(versionedDocument.getAuxParameters(), DocumentAux.class);
            } catch (JsonProcessingException ex) {
                log.error("У документа {} отсутствует блок дополнительных данных", versionedDocument.getId().toString());
            }

            if (attachment != null) {
                if (documentAux != null) {
                    attachment.setQuality(documentAux.getQuality());
                    attachment.setVerified(documentAux.isVerified());
                }
                attachment.setPath(path);
                attachment.setDocumentId(versionedDocument.getId().toString());
                attachment.setVersionName(versionName);
                attachment.setVersionCreator(versionCreator);
                attachment.setCurrentVersion(isCurrentVersion);
                result.add(attachment);
            }
        }
        return result;
    }

    private Optional<DocumentDTO> updateAndReturnDocument(DocumentDTO documentDTO) throws StorageException {
        HttpHeaders headers = getHeaders();
        HttpEntity http = new HttpEntity<>(documentDTO, headers);
        String url = String.format(DOCUMENT_EDIT_SCHEMA, storageUrl);
        log.info("updateAndReturnDocument documentId = {}; url = {}", documentDTO.getId(), url);
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("Содержимое документа {}", mapper.writeValueAsString(documentDTO));
        } catch (JsonProcessingException ex) {
            log.error("Не удалось расшифровать содержимое документа", ex);
        }
        ResponseEntity<DocumentDTO> documentResponse;
        try {
            documentResponse = restTemplate.exchange(url, HttpMethod.PUT, http, DocumentDTO.class);
        } catch (HttpClientErrorException ex) {
            log.error("Не удалось обновить метаданные документа", ex);
            throw new StorageException("Не удалось обновить метаданные документа", ex);
        }
        if (documentResponse != null && documentResponse.getBody() != null) {
            clearCache();
            return Optional.of(documentResponse.getBody());
        }
        return Optional.empty();
    }

    private Optional<FolderDTO> getUserDocumentFolder(Long clientId) throws StorageException {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = String.format(ALL_DOCUMENT_FOLDER_SCHEMA, storageUrl, String.valueOf(clientId));
        HttpEntity<FolderDTO> response;
        log.info("Загрузка папки пользователя clientId = {}; url = {}", clientId, url);
        log.info("Заголовки = {}", headers.toString());
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, FolderDTO.class);
        } catch (HttpClientErrorException ex) {
            log.info("Не удалось выполнить загрузку папки пользователя (возможно папка еще не существует т.к. документы не загружались)");
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new StorageException("Не удалось прочитать данные о документах");
        }
        return Optional.ofNullable(response.getBody());
    }

    private Attachment createInputAttachment(String attachmentName, Instant when, String mimeType,
                                             AttachmentFunctionalType functionalType) {

        when = when == null ? Instant.now() : when;
        mimeType = mimeType == null ? "" : mimeType;
        functionalType = functionalType == null ? AttachmentFunctionalType.UNKNOWN : functionalType;

        try {
            return new Attachment(attachmentName, when, mimeType, functionalType);
        } catch (StorageException ex) {
            log.error("Не удалось сконструировать документ", ex);
            return null;
        }
    }

    private Attachment createOrReplaceAttachment(Long userId, String attachmentName, Instant when, byte[] content, String mimeType,
                                                 AttachmentFunctionalType functionalType, String token, DocumentClass documentClass) throws StorageException {
        try {
            Set<Attachment> existsAttachment = getAttachmentsByClass(userId, documentClass);
            if (functionalType.isUnique()) {
                Optional<Attachment> uniqueAttachment = existsAttachment.stream()
                        .filter(it -> it.getFunctionalType().equals(functionalType) && it.isCurrentVersion())
                        .findAny();
                if (uniqueAttachment.isPresent()) {
                    return replaceAttachment(uniqueAttachment.get(), attachmentName, content, token);
                }
            } else {
                Optional<Attachment> sameNameAndTypeAttachment = existsAttachment.stream()
                        .filter(it -> it.getAttachmentName().equals(attachmentName) && it.getFunctionalType() == functionalType && it.isCurrentVersion())
                        .findAny();
                if (sameNameAndTypeAttachment.isPresent()) {
                    return replaceAttachment(sameNameAndTypeAttachment.get(), attachmentName, content, token);
                }
            }
            return createAttachment(userId, attachmentName, when, content, mimeType, functionalType, token, documentClass);
        } catch (Exception ex) {
            log.error("Не удалось создать или обновить документ", ex);
            throw new StorageException(ex.getMessage());
        }
    }

    private Attachment createAttachment(Long userId, String attachmentName, Instant when, byte[] content, String mimeType,
                                        AttachmentFunctionalType functionalType, String token, DocumentClass documentClass) throws StorageException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setBearerAuth(token);
        setHostHeader(headers);

        String frontendKey = functionalType == null ? "" : functionalType.getFrontendKey();
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setTitle(attachmentName);
        documentDTO.setDocumentType(frontendKey);
        HttpEntity http = new HttpEntity<>(documentDTO, headers);
        HttpEntity httpCreate = new HttpEntity<>(headers);

        String url = String.format(USER_DOCUMENT_IN_CLASS_SCHEMA, storageUrl, String.valueOf(userId), documentClass.getName());
        log.info("Создание документа в хранилище documentId = {}; url = {}", documentDTO.getId(), url);
        try {
            ResponseEntity<DocumentDTO> responseEntity = restTemplate.exchange(url, HttpMethod.POST, http, DocumentDTO.class);
            DocumentDTO response = responseEntity.getBody();
            if (response != null) {
                Attachment newAttachment = new Attachment(response.getTitle(), response.getCreatedAt(), "", AttachmentFunctionalType.getByStringKey(response.getDocumentType()));

                ResponseEntity<FileDTO> fileResponse = createOrUpdateFile(response, content, HttpMethod.POST, token);

                if (fileResponse != null && fileResponse.getBody() != null) {
                    newAttachment.setMigrated(true);
                    newAttachment.setId(getIdForPath(fileResponse.getBody().getPath().toString()));
                    newAttachment.setDocumentId(response.getId().toString());
                    return newAttachment;
                }
            }
        } catch (Exception ex) {
            log.error("Не удалось создать документ", ex);
            throw new StorageException(ex.getMessage());
        }
        throw new StorageException("ответ на создание документа не удалось разобрать");
    }

    private Attachment replaceAttachment(Attachment attachment, String newAttachmentName, byte[] content, String token) throws IOException, StorageException {
        DocumentDTO documentDTO = new DocumentDTO(new UniqueId(attachment.getDocumentId()));
        documentDTO.setTitle(newAttachmentName);
        documentDTO.setDocumentType(attachment.getFunctionalType().getFrontendKey());
        ResponseEntity<FileDTO> fileResponse = createOrUpdateFile(documentDTO, content, HttpMethod.PUT, token);
        if (fileResponse != null && fileResponse.getBody() != null) {
            attachment.setId(getIdForPath(fileResponse.getBody().getPath().toString()));
            if (!newAttachmentName.equals(attachment.getAttachmentName())) {
                editAttachmentName(attachment, newAttachmentName);
                attachment.setAttachmentName(newAttachmentName);
            }
        }
        return attachment;
    }

    //POST создает файл в документе, PUT заменяет файл в документе (предыдущий документ уходит в версию)
    private ResponseEntity<FileDTO> createOrUpdateFile(DocumentDTO documentDTO, byte[] content, HttpMethod method, String token) throws IOException {
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add(POST_PARAMETER_FILENAME, documentDTO.getTitle());
        bodyMap.add(POST_PARAMETER_DESCRIPTION, documentDTO.getDescription());
        bodyMap.add(POST_PARAMETER_BINARY, getUserFileResource(content, documentDTO.getTitle()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);
        setHostHeader(headers);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

        String url = String.format(FILE_SCHEMA, storageUrl, documentDTO.getId());

        return restTemplate.exchange(url, method, requestEntity, FileDTO.class);
    }

    @Override
    public byte[] getBinaryContent(Attachment attachment) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(
                new ByteArrayHttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken());
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        setHostHeader(headers);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = String.format(BINARY_SCHEMA, storageUrl, attachment.getPath());
        log.info("Получение бинарных данных attachmentId = {}; url = {}", attachment.getId(), url);
        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET, entity, byte[].class);
        } catch (HttpClientErrorException ex) {
            log.error("Не получить бинарные данные", ex);
            return new byte[0];
        }
        return response.getBody();
    }

    @Override
    public String getZipUserAttachmentUrl() {
        return String.format(ZIP_SCHEMA, storageUrl);
    }

    @Override
    public HttpEntity getHttpEntityForZipRequest(Map<String, String> attachmentsMap) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken());
        setHostHeader(headers);
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(attachmentsMap, headers);
    }

    public static class MultipartFileResource extends ByteArrayResource {

        private String filename;

        public MultipartFileResource(byte[] content, String name) throws IOException {
            super(content);
            this.filename = name;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }

    private static Resource getUserFileResource(byte[] content, String name) throws IOException {
        return new MultipartFileResource(content, name);
    }

    private String jwtToken() {
        return getTokenString()
                .orElseGet(userService::getAccessToken);
    }

    private boolean isDeletedDocument(List<VersionDTO> versions) {
        if (versions == null || versions.isEmpty()) {
            return false;
        }
        return versions.stream().anyMatch(VersionDTO::getIsDeleted);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setBearerAuth(jwtToken());
        setHostHeader(headers);
        return headers;
    }

    private long getIdForPath(String path) {
        return Math.abs(UUID.nameUUIDFromBytes(path.getBytes()).getMostSignificantBits());
    }

    private void clearCache() {
        clearBankAttachmentCache();
        clearUserAttachmentCache();
    }

    private void setHostHeader(HttpHeaders headers) {
        if (!StringUtils.isEmpty(hostName)) {
            headers.set(HttpHeaders.HOST, hostName);
        }
    }

    //    @CacheEvict(value = "userAttachments", allEntries = true) //временно отключен кэш
    public void clearUserAttachmentCache() {}

    //    @CacheEvict(value = "bankAttachments", allEntries = true) //временно отключен кэш
    public void clearBankAttachmentCache() {}
}
