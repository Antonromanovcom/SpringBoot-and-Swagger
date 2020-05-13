package online.prostobank.clients.api.client;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.services.client.ClientAttachmentClass;
import online.prostobank.clients.services.client.ClientAttachmentService;
import online.prostobank.clients.utils.aspects.Benchmark;
import org.apache.commons.io.IOUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static online.prostobank.clients.api.ApiConstants.CLIENT_ATTACHMENT_CONTROLLER;

@Benchmark
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
public class ClientZipController {
	private final ClientAttachmentService attachmentService;

	@ApiOperation(value = "Скачать все вложения класса клиента")
	@GetMapping(value = "download_all")
	public void processRequest(KeycloakAuthenticationToken token,
							   @RequestParam Long clientId,
							   @RequestParam ClientAttachmentClass classId, HttpServletResponse response) {

		processZipRequest(clientId, classId, response, false);
	}

	@ApiOperation(value = "Скачать все вложения клиента для заверения")
	@GetMapping(value = "download_to_verify")
	public void downloadToVerify(@RequestParam Long clientId, HttpServletResponse response) {
		processZipRequest(clientId, null, response, true);
	}

	private void processZipRequest(Long clientId, ClientAttachmentClass classId, HttpServletResponse response,
								   boolean isVerified) {

		RestTemplate restTemplate = new RestTemplate();
		String url = attachmentService.getZipUserAttachmentUrl();
		Optional<HttpEntity> http;
		if (isVerified) {
			http = attachmentService.getHttpEntityForZipVerifiedAttachmentsRequest(clientId);
		} else {
			http = attachmentService.getHttpEntityForZipAttachmentsRequest(clientId, classId);
		}
		if (http.isPresent()) {
			response.setStatus(HttpServletResponse.SC_OK);
			String filename = isVerified ? "archiveToVerify.zip" : "archive.zip";
			response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));
			restTemplate.execute(
					url,
					HttpMethod.POST,
					restTemplate.httpEntityCallback(http.get()),
					responseExtractor -> {
						IOUtils.copy(responseExtractor.getBody(), response.getOutputStream());
						return null;
					});
		} else {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
		}
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseDTO> responseDTOHandle(Exception e) {
		log.error(e.getLocalizedMessage(), e);
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
