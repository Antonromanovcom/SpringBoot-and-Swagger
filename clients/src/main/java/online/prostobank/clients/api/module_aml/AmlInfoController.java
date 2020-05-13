package online.prostobank.clients.api.module_aml;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.services.client.ClientAmlModuleService;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static online.prostobank.clients.api.ApiConstants.*;

/**
 * API для модуля №9, предоставляющим антиотмывочный функционал (AML)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(value="Module #9 API controller", description="Получение сведений о клиенте для Модуля №9")
@RequestMapping(value = AML_CONTROLLER, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//@Secured({ROLE_FINMON_ADMIN})
public class AmlInfoController {

	private final ClientAmlModuleService clientAmlModuleService;

	@ApiOperation(value = "Получение карточки клиента по номеру счёта")
	@GetMapping(value = FIND_BY_ACCOUNT_NUMBER)
	public ResponseEntity<ResponseDTO> findByAccountNumber(@NonNull KeycloakAuthenticationToken token,
	                                                       @RequestParam("number") String accountNumber) {
		accountNumber = StringUtils.trimToEmpty(accountNumber);
		if (!StringUtils.isNumeric(accountNumber)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<>(
				clientAmlModuleService.findByAccountNumber(accountNumber)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				headers, HttpStatus.OK
		);
	}

	@ApiOperation(value = "Получение карточки клиента по идентификатору keycloak")
	@GetMapping(value = FIND_BY_KEYCLOAK_ID)
	public ResponseEntity<ResponseDTO> findByKeycloakId(@RequestParam("id") UUID keycloakId) {
		if (keycloakId == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<>(
				clientAmlModuleService.findByKeycloakId(keycloakId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				headers, HttpStatus.OK
		);
	}
}
