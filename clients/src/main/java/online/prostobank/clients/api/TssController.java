package online.prostobank.clients.api;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.domain.tss.TssDTO;
import online.prostobank.clients.services.tss.TssService;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

import static online.prostobank.clients.api.ApiConstants.ACCEPTED;
import static online.prostobank.clients.api.ApiConstants.NOT_FOUND;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getTokenString;

@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = TssController.API_TSS, produces = MediaType.APPLICATION_JSON_VALUE)
public class TssController {
	public static final String API_TSS = "api/tss/";
	public static final String CALLBACK = "callback";

	private final TssService tssService;

	@ApiOperation(value = "Подписать сообщение")
	@GetMapping(value = "sign")
	public ResponseEntity<ResponseDTO> sign(KeycloakAuthenticationToken token,
											@RequestParam Long clientId,
											@RequestParam Long attachmentId) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(
				tssService.sign(getTokenString(token), clientId, attachmentId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Доставка подписи сервису потребителю")
	@PostMapping(value = CALLBACK, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseDTO> callback(@RequestBody TssDTO dto) {
		return new ResponseEntity<>(
				tssService.callback(dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Получить результат подписания")
	@GetMapping(value = "sign_result")
	public ResponseEntity<ResponseDTO> signResult(KeycloakAuthenticationToken token,
												  @RequestParam UUID requestId) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(
				tssService.signResult(getTokenString(token), requestId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}


	@ApiOperation(value = "Повторная отправка СМС для подтверждения подписания сообщения")
	@GetMapping(value = "resend_sms")
	public ResponseEntity<ResponseDTO> resendSms(KeycloakAuthenticationToken token,
												 @RequestParam UUID requestId) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(
				tssService.resendSms(getTokenString(token), requestId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Проверить подпись сообщения")
	@GetMapping(value = "sign_check")
	public ResponseEntity<ResponseDTO> signCheck(KeycloakAuthenticationToken token,
												 @RequestParam UUID requestId) {
		if (token == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(
				tssService.signCheck(getTokenString(token), requestId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ExceptionHandler(HttpClientErrorException.class)
	public ResponseEntity<ResponseDTO> getResponseDTOResponseEntity(HttpClientErrorException e) {
		log.error("statusCode: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
		return new ResponseEntity<>(ResponseDTO.badResponse(e.getResponseBodyAsString()), HttpStatus.OK);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseDTO> getResponseDTOResponseEntity(Exception e) {
		String localizedMessage = e.getLocalizedMessage();
		log.error(localizedMessage, e);
		return new ResponseEntity<>(ResponseDTO.badResponse(localizedMessage), HttpStatus.OK);
	}
}
