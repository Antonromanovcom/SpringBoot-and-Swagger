package online.prostobank.clients.api.client;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.ClientAttachmentHelpDTO;
import online.prostobank.clients.api.dto.client.ClientCardDTO;
import online.prostobank.clients.api.dto.rest.AttachmentDTO;
import online.prostobank.clients.domain.Attachment;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.services.client.ClientAttachmentClass;
import online.prostobank.clients.services.client.ClientAttachmentService;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.apache.commons.io.FilenameUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static java.util.Arrays.asList;
import static online.prostobank.clients.api.ApiConstants.*;
import static online.prostobank.clients.services.client.ClientAttachmentUtils.encode;
import static online.prostobank.clients.services.client.ClientAttachmentUtils.getStreamResponse;
import static online.prostobank.clients.utils.Utils.FILE_EXTENSIONS;

@Benchmark
@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(CLIENT_ATTACHMENT_CONTROLLER)
// APIKUB-1884 убрал, на стейдже не работает, всегда 403
//@Secured({ROLE_POS_ADMIN,
//		ROLE_POS_FRONT,
//		ROLE_POS_ADMIN_HOME,
//		ROLE_POS_FRONT_HOME,
//		ROLE_POS_ADMIN_PARTNER,
//		ROLE_POS_FRONT_PARTNER,
//		ROLE_POS_OUTER_API_ADMIN,
//		ROLE_POS_OUTER_API_MANAGER,
//})
public class ClientAttachmentController {
	private final ClientAttachmentService attachmentService;

	@ApiOperation(value = "Загрузить вложение")
	@PostMapping(value = "upload")
	public ResponseEntity<ResponseDTO> upload(@NonNull KeycloakAuthenticationToken token,
											  @RequestParam Long clientId,
											  @RequestParam ClientAttachmentClass classId,
											  @RequestParam AttachmentFunctionalType typeId,
											  @RequestParam MultipartFile file) throws IOException {
		return !asList(FILE_EXTENSIONS).contains("." + FilenameUtils.getExtension(file.getOriginalFilename()))
				? new ResponseEntity<>(ResponseDTO.badResponse("not correct file"), HttpStatus.BAD_REQUEST)
				: new ResponseEntity<>(
				attachmentService.upload(token.getName(),
						new ClientAttachmentHelpDTO(clientId, classId, typeId, file.getOriginalFilename(), file.getBytes(), file.getContentType()))
						.map(AttachmentDTO::createFrom)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Скачать вложение")
	@GetMapping(value = "download")
	public ResponseEntity<InputStreamResource> getBinaryFromAttachment(@RequestParam Long clientId, @RequestParam String attachmentId) {
		attachmentId = attachmentId.replace("\"", "");
		attachmentId = attachmentId.replace("\'", "");
		Optional<byte[]> content = attachmentService.download(clientId, Long.valueOf(attachmentId));
		Optional<Attachment> attachment = attachmentService.findByClientAndId(clientId, Long.valueOf(attachmentId));
		if (content.isPresent() && attachment.isPresent()) {
			return getStreamResponse(content.get(), encode(attachment.get().getAttachmentName()), true);
		} else {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseDTO> responseDTOHandle(Exception e) {
		log.error(e.getLocalizedMessage(), e);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "Переименовать вложение")
	@PutMapping(value = "rename")
	public ResponseEntity<ResponseDTO> rename(@NonNull KeycloakAuthenticationToken token,
											  @RequestParam Long clientId,
											  @RequestParam Long attachmentId,
											  @RequestParam String name) {
		return new ResponseEntity<>(
				attachmentService.rename(token.getName(), clientId, attachmentId, name)
						.map(AttachmentDTO::createFrom)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Удалить вложение")
	@DeleteMapping(value = "delete")
	public ResponseEntity<ResponseDTO> delete(@NonNull KeycloakAuthenticationToken token,
											  @RequestParam Long clientId,
											  @RequestParam Long attachmentId) {
		return new ResponseEntity<>(
				attachmentService.delete(token.getName(), clientId, attachmentId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Установить качество вложения")
	@PutMapping(value = "set_quality")
	public ResponseEntity<ResponseDTO> setQuality(@NonNull KeycloakAuthenticationToken token,
												  @RequestParam Long clientId,
												  @RequestParam Long attachmentId,
												  @RequestParam boolean quality) {
		return new ResponseEntity<>(
				attachmentService.setQuality(token.getName(), clientId, attachmentId, quality)
						.map(AttachmentDTO::createFrom)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Пометить вложение для заверения")
	@PutMapping(value = "set_verified")
	public ResponseEntity<ResponseDTO> setVerified(@NonNull KeycloakAuthenticationToken token,
												   @RequestParam Long clientId,
												   @RequestParam Long attachmentId,
												   @RequestParam boolean verified) {
		return new ResponseEntity<>(
				attachmentService.setVerified(token.getName(), clientId, attachmentId, verified)
						.map(AttachmentDTO::createFrom)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Распознать вложение")
	@PutMapping(value = "recognize")
	public ResponseEntity<ResponseDTO> recognize(@NonNull KeycloakAuthenticationToken token,
												 @RequestParam Long clientId,
												 @RequestParam Long attachmentId) {
		return new ResponseEntity<>(
				attachmentService.recognize(token.getName(), clientId, attachmentId)
						.map(ClientCardDTO::createFrom)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}
}
