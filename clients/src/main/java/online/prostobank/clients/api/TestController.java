package online.prostobank.clients.api;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static online.prostobank.clients.api.ApiConstants.ACCEPTED;
import static online.prostobank.clients.api.ApiConstants.NOT_FOUND;
import static online.prostobank.clients.security.UserRolesConstants.ROLE_POS_ADMIN;
import static online.prostobank.clients.security.UserRolesConstants.ROLE_POS_ADMIN_HOME;

@JsonLogger
@Slf4j
@RestController
@RequestMapping(value = "test")
public class TestController {
	@ApiOperation(value = "Тестовый метод pos-admin и pos-admin-home")
	@GetMapping(value = "get_admin_home")
	@Secured({ROLE_POS_ADMIN_HOME})
	public ResponseEntity<ResponseDTO> getAdminHome(@NonNull KeycloakAuthenticationToken token,
													@RequestParam Long testId) {
		log.info("\ntoken: {},\n testId: {}", token, testId);
		return new ResponseEntity<>(
				Optional.of("")
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Тестовый метод pos-admin")
	@GetMapping(value = "get_admin")
	@Secured({ROLE_POS_ADMIN})
	public ResponseEntity<ResponseDTO> getAdmin(@NonNull KeycloakAuthenticationToken token,
												@RequestParam Long testId) {
		log.info("\ntoken: {},\n testId: {}", token, testId);
		return new ResponseEntity<>(
				Optional.of("")
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}
}
