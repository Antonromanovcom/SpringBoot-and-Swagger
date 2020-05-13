package online.prostobank.clients.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.dto.AttachmentDTO;
import online.prostobank.clients.api.dto.ClientDocumentsDTO;
import online.prostobank.clients.domain.repository.attachment.AttachmentMigrationRepository;
import online.prostobank.clients.utils.aspects.Benchmark;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.io.ByteArrayInputStream;
import java.util.List;

import static online.prostobank.clients.api.ApiConstants.ATTACHMENT_CONTROLLER;
import static online.prostobank.clients.security.UserRolesConstants.*;

@Benchmark
@Controller
@RequiredArgsConstructor
@RequestMapping(value = ATTACHMENT_CONTROLLER, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Secured({ROLE_POS_ADMIN,
		ROLE_POS_FRONT,
		ROLE_POS_ADMIN_HOME,
		ROLE_POS_FRONT_HOME,
		ROLE_POS_ADMIN_PARTNER,
		ROLE_POS_FRONT_PARTNER,
		ROLE_POS_OUTER_API_ADMIN,
		ROLE_POS_OUTER_API_MANAGER,
		ROLE_POS_CONSULTANT
})
public class AttachmentDownloadController {
	private static final int DEFAULT_OFFSET = 0;
	private static final int DEFAULT_LIMIT = 10;
	private static final int MAX_LIMIT = 100;

	private final AttachmentMigrationRepository migrationRepository;

	/**
	 * Списки идентификаторов вложений в привязке к идентификатору клиента (с пагинацией)
	 * @param offset
	 * @param limit
	 * @return
	 */
	@GetMapping
	@ResponseBody
	public ResponseEntity<Documents> bookApplication(
			@Min(value = 0L, message = "Значение должно быть неотрицательным числом") @RequestParam(value = "offset", required = false) Long offset,
			@Min(value = 0L, message = "Значение должно быть неотрицательным числом") @RequestParam(value = "limit", required = false) Long limit
	) {

		offset = offset == null ? DEFAULT_OFFSET : offset;
		limit = limit == null ? DEFAULT_LIMIT : Math.min(MAX_LIMIT, limit);

		Page page = new Page(offset, limit, migrationRepository.getClientTotalCount());
		Documents documents = new Documents(migrationRepository.getClientsDocuments(offset, limit), page);

		return new ResponseEntity<>(documents, HttpStatus.OK);
	}

	/**
	 * Метаданные указанного вложения
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/meta/{id}")
	@ResponseBody
	public ResponseEntity<AttachmentDTO> getAttachmentMeta(@PathVariable(value = "id", required = false) Long id) {
		if (id == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			AttachmentDTO dto = migrationRepository.getAttachmentMeta(id);
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Двоичные данные (если есть) указанного вложения
	 * @param id
	 * @param response
	 * @return
	 */
	@GetMapping(value = "/binary/{id}")
	public ResponseEntity<InputStreamResource> getBinaryFromAttachment(@PathVariable(value = "id", required = false) Long id,
																	   HttpServletResponse response) {
		if (id == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			byte[] content = migrationRepository.getAttachmentContent(id);
			if (content != null) {
				InputStreamResource isr = new InputStreamResource(new ByteArrayInputStream(content));
				return new ResponseEntity<>(isr, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		} catch (Exception ex) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Пометить вложение, как мигрировавшее
	 * @param id
	 * @return
	 */
	@PostMapping(value = "/meta/{id}")
	@ResponseBody
	public ResponseEntity<?> setAttachmentAsMigrated(@PathVariable(value = "id", required = false) Long id) {
		if (id == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		migrationRepository.setAttachmentAsMigrated(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/statistics")
	@ResponseBody
	public ResponseEntity<Statistics> getStatistics() {
		return new ResponseEntity<>(new Statistics(
				migrationRepository.getClientTotalCount(),
				migrationRepository.getAttachTotalCount(),
				migrationRepository.getNotMigratedCount()),
				HttpStatus.OK);
	}

	@AllArgsConstructor
	@Getter
	private class Page {
		@JsonProperty(value = "offset")
		private Long offset;
		@JsonProperty(value = "limit")
		private Long limit;
		@JsonProperty(value = "count")
		private Long count;
	}

	@AllArgsConstructor
	@Getter
	private class Documents {
		private List<ClientDocumentsDTO> documents;
		private Page page;
	}

	@AllArgsConstructor
	@Getter
	private class Statistics {
		@JsonProperty(value = "total_client_count")
		private Long totalClientCount;
		@JsonProperty(value = "total_attach_count")
		private Long totalAttachCount;
		@JsonProperty(value = "not_migrated_attach_count")
		private Long notProcessedCount;
	}
}
