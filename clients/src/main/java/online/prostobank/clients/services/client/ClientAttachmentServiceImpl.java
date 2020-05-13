package online.prostobank.clients.services.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.client.ClientAttachmentHelpDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.attachment.DocumentClass;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.recognition.interfaces.IRecognizedDocument;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.repository.HistoryRepository;
import online.prostobank.clients.services.StorageException;
import online.prostobank.clients.services.attacment.AttachmentService;
import online.prostobank.clients.services.interfaces.IRecognitionService;
import online.prostobank.clients.utils.AccountApplicationHelper;
import online.prostobank.clients.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static online.prostobank.clients.services.client.ClientAttachmentUtils.toZip;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientAttachmentServiceImpl implements ClientAttachmentService {
	private final AttachmentService attachmentService;
	private final HistoryRepository historyRepository;
	private final IRecognitionService recognitionService;
	private final AccountApplicationRepository accountApplicationRepository;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;

	@Override
	public Optional<Attachment> upload(String initiator, ClientAttachmentHelpDTO dto) {
		if (initiator == null || dto == null) {
			log.error("Отсутствуют требуемые параметры");
			return Optional.empty();
		}
		Long clientId = dto.getClientId();
		ClientAttachmentClass classId = dto.getClassId();
		AttachmentFunctionalType typeId = dto.getTypeId();
		String name = dto.getName();
		byte[] content = dto.getContent();
		String contentType = dto.getContentType();

		try {
			if (StringUtils.isBlank(contentType)) {
				contentType = Utils.getMimeType(content);
				if (StringUtils.isBlank(contentType)) {
					contentType = Utils.getMimeTypeByFileName(name);
				}
			}
			Attachment prepare = attachmentService.createAttachment(clientId, name, Instant.now(), content, contentType, typeId, DocumentClass.from(classId));
			historyRepository.insertChangeHistory(clientId, initiator, "Вложение создано " + prepare.getAttachmentName());
			return Optional.of(prepare);
		} catch (StorageException e) {
			log.error(e.getLocalizedMessage(), e);
			return Optional.empty();
		}
	}

	@Override
	public Optional<byte[]> download(Long clientId, Long attachmentId) {
		if (clientId == null || attachmentId == null) {
			log.error("Не указаны требуемые параметры clientId = {}; attachId = {}", clientId, attachmentId);
			return Optional.empty();
		}
		return attachmentService.findById(clientId, attachmentId)
				.map(attachmentService::getBinaryContent);
	}

	@Override
	public Optional<byte[]> downloadAll(Long clientId, ClientAttachmentClass classId) {
		if (clientId == null || classId == null) {
			log.error("Отсутствуют требуемые параметры");
			return Optional.empty();
		}
		try {
			Set<Attachment> attachments;
			if (classId == ClientAttachmentClass.BANK) {
				attachments = attachmentService.getBankAttachments(clientId);
			} else if (classId == ClientAttachmentClass.USER) {
				attachments = attachmentService.getUserAttachments(clientId);
			} else {
				return Optional.empty();
			}
			return Optional.of(
					toZip(attachments
							.stream()
							.collect(Collectors.toMap(
									Utils::addFourRandomCharToAttachName,
									attachmentService::getBinaryContent))));

		} catch (StorageException ex) {
			return Optional.empty();
		}
	}

	@Override
	public String getZipUserAttachmentUrl() {
		return attachmentService.getZipUserAttachmentUrl();
	}

	@Override
	public Optional<HttpEntity> getHttpEntityForZipAttachmentsRequest(Long clientId, ClientAttachmentClass classId) {
		if (clientId == null || classId == null) {
			log.error("Отсутствуют требуемые параметры");
			return Optional.empty();
		}
		try {
			Set<Attachment> attachments;
			if (classId == ClientAttachmentClass.BANK) {
				attachments = attachmentService.getBankAttachments(clientId);
			} else if (classId == ClientAttachmentClass.USER) {
				attachments = attachmentService.getUserAttachments(clientId);
			} else {
				return Optional.empty();
			}
			return Optional.ofNullable(
					attachmentService.getHttpEntityForZipRequest(attachments
							.stream()
							.collect(Collectors.toMap(
									Utils::addFourRandomCharToAttachName,
									Attachment::getPath)))
			);
		} catch (StorageException ex) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<HttpEntity> getHttpEntityForZipVerifiedAttachmentsRequest(Long clientId) {
		if (clientId == null) {
			log.error("Отсутствуют требуемые параметры");
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(
					attachmentService.getHttpEntityForZipRequest(
							attachmentService.getUserAttachments(clientId)
									.stream()
									.filter(Attachment::getVerified)
									.collect(Collectors.toMap(
											Utils::addFourRandomCharToAttachName,
											Attachment::getPath)))
			);
		} catch (StorageException ex) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<byte[]> downloadToVerify(Long clientId) {
		try {
			return Optional.of(
					toZip(attachmentService.getUserAttachments(clientId)
							.stream()
							.filter(Attachment::getVerified)
							.collect(Collectors.toMap(
									Utils::addFourRandomCharToAttachName,
									attachmentService::getBinaryContent))));

		} catch (StorageException ex) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Attachment> rename(String initiator, Long clientId, Long attachmentId, String name) {
		if (clientId == null || attachmentId == null || name == null) {
			log.error("Не указаны требуемые параметры clientId = {}; attachId = {}; name = {}", clientId, attachmentId, name);
			return Optional.empty();
		}

		Optional<Attachment> attachmentOptional = attachmentService.findById(clientId, attachmentId);
		if (attachmentOptional.isPresent() && ClientAttachmentUtils.checkFileName(name)) {
			Attachment attachment = attachmentOptional.get();
			try {
				String oldName = attachment.getAttachmentName();
				name += Utils.getFileNameExtension(oldName);
				attachmentService.editAttachmentName(attachment, name);
				attachment.setAttachmentName(name);
				historyRepository.insertChangeHistory(clientId, initiator, "Документ переименован с " + oldName + " на " + name);
				return Optional.of(attachment);
			} catch (StorageException ex) {
				log.error("Не удалось переименовать документ -- ошибка хранилища", ex);
				return Optional.empty();
			}
		}
		log.error("Не удалось переименовать документ -- не найден документ или неверное имя clientId = {}; attachId = {}; name = {}",
				clientId, attachmentId, name);
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> delete(String initiator, Long clientId, Long attachmentId) {
		if (clientId == null || attachmentId == null) {
			log.error("Не указаны требуемые параметры clientId = {}; attachId = {}", clientId, attachmentId);
			return Optional.empty();
		}
		Optional<Attachment> attachmentOptional = attachmentService.findById(clientId, attachmentId);
		if (attachmentOptional.isPresent()) {
			Attachment attachment = attachmentOptional.get();
			String oldName = attachment.getAttachmentName();
			try {
				attachmentService.deleteAttachment(attachment);
				historyRepository.insertChangeHistory(clientId, initiator, "Документ удален " + oldName);
				return Optional.of(true);
			} catch (StorageException ex) {
				log.error("Не удалось переименовать документ -- ошибка хранилища", ex);
				return Optional.empty();
			}
		}
		log.error("Не удалось удалить документ -- не найден документ clientId = {}; attachId = {};", clientId, attachmentId);
		return Optional.empty();
	}

	@Override
	public Optional<Attachment> setQuality(String initiator, Long clientId, Long attachmentId, boolean quality) {
		if (clientId == null || attachmentId == null) {
			log.error("Не указаны требуемые параметры clientId = {}; attachId = {}", clientId, attachmentId);
			return Optional.empty();
		}
		Optional<Attachment> attachmentOptional = attachmentService.findById(clientId, attachmentId);
		if (attachmentOptional.isPresent()) {
			Attachment attachment = attachmentOptional.get();
			try {
				attachmentService.editAttachmentQuality(attachment, quality);
				historyRepository.insertChangeHistory(clientId, initiator, "Качество документа " + attachment.getAttachmentName() + " установлено: " + quality);
				attachment.setQuality(quality);
				return Optional.of(attachment);
			} catch (StorageException ex) {
				log.error("Не удалось установить флаг качества -- ошибка хранилища", ex);
				return Optional.empty();
			}
		}
		log.error("Не удалось установить флаг качества -- не найден документ clientId = {}; attachId = {};", clientId, attachmentId);
		return Optional.empty();
	}

	@Override
	public Optional<Attachment> setVerified(String initiator, Long clientId, Long attachmentId, boolean verified) {
		if (clientId == null || attachmentId == null) {
			log.error("Не указаны требуемые параметры clientId = {}; attachId = {}", clientId, attachmentId);
			return Optional.empty();
		}
		Optional<Attachment> attachmentOptional = attachmentService.findById(clientId, attachmentId);
		if (attachmentOptional.isPresent()) {
			Attachment attachment = attachmentOptional.get();
			try {
				attachmentService.editAttachmentVerification(attachment, verified);
				historyRepository.insertChangeHistory(clientId, initiator, "Документ для заверения " + attachment.getAttachmentName() + " помечен: " + verified);
				attachment.setVerified(verified);
				return Optional.of(attachment);
			} catch (StorageException ex) {
				log.error("Не удалось установить флаг заверения -- ошибка хранилища", ex);
				return Optional.empty();
			}
		}
		log.error("Не удалось установить флаг заверения -- не найден документ clientId = {}; attachId = {};", clientId, attachmentId);
		return Optional.empty();
	}

	@Override
	public Optional<AccountApplication> recognize(String initiator, Long clientId, Long attachmentId) {
		if (clientId == null || attachmentId == null) {
			log.error("Не указаны требуемые параметры clientId = {}; attachId = {}", clientId, attachmentId);
			return Optional.empty();
		}
		Optional<Attachment> attachmentOptional = attachmentService.findById(clientId, attachmentId);
		if (attachmentOptional.isPresent()) {
			Attachment attachment = attachmentOptional.get();
			return  Optional
					.of(recognitionService.getDocument(attachmentService.getBinaryContent(attachment), attachment.getFunctionalType()))
					.filter(IRecognizedDocument::isRecognized)
					.flatMap(document -> accountApplicationRepository.findById(clientId)
							.map(application -> {
								AccountApplicationHelper.Result result = AccountApplicationHelper.fillFromDocument(application, document);
								historyRepository.insertChangeHistory(clientId, initiator,
										"Документ распознан " + attachment.getAttachmentName() + " \n" + result.getHistoryMessage());
								return repositoryWrapper.saveAccountApplication(application, true).getSecond();
							}));
		}
		return Optional.empty();
	}

	@Override
	public Optional<Attachment> findByClientAndId(Long clientId, Long attachmentId) {
		return attachmentService.findById(clientId, attachmentId);
	}
}
