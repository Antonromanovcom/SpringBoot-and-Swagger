package online.prostobank.clients.api.client;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.services.client.ClientService;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static online.prostobank.clients.api.ApiConstants.*;

@Benchmark
@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(CLIENT_DBO_CONTROLLER)
public class DboClientController {
	private final ClientService clientService;

	@ApiOperation(value = "Наличие заявок у клиента")
	@GetMapping(value = "exist_applications")
	public ResponseEntity<ResponseDTO> existApplications(@NonNull KeycloakAuthenticationToken token) {
		return new ResponseEntity<>(
				clientService.existApplications(token.getName())
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Получение списка документов")
	@GetMapping(value = "attachment_list")
	public ResponseEntity<ResponseDTO> attachmentList(@NonNull KeycloakAuthenticationToken token) {
		return new ResponseEntity<>(
				clientService.attachmentList(token.getName())
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}
}
