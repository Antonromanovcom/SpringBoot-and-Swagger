package online.prostobank.clients.api.dictionary;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.AttachmentTypeHierarchyDTO;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.api.dto.dictionary.StatusDTO;
import online.prostobank.clients.security.UserRoles;
import online.prostobank.clients.services.dictionary.DictionaryService;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static online.prostobank.clients.api.ApiConstants.*;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getAllowedRolesForAdmins;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getRoles;

@JsonLogger
@RequiredArgsConstructor
@RestController
@RequestMapping(
		value = DICTIONARY_CONTROLLER,
		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
// APIKUB-1884 убрал, на стейдже не работает, всегда 403
//@Secured({ROLE_POS_ADMIN,
//		ROLE_POS_FRONT,
//		ROLE_POS_ADMIN_HOME,
//		ROLE_POS_FRONT_HOME,
//		ROLE_POS_ADMIN_PARTNER,
//		ROLE_POS_FRONT_PARTNER,
//		ROLE_POS_OUTER_API_ADMIN,
//		ROLE_POS_OUTER_API_MANAGER,
//		ROLE_POS_CONSULTANT,
//})
public class DictionaryController {
	private final DictionaryService dictionaryService;

	@ApiOperation(value = "Получение списка доступных статусов")
	@GetMapping(value = GET_STATUSES)
	public ResponseEntity<ResponseDTO> getStatuses(@NonNull KeycloakAuthenticationToken token) {
		return new ResponseEntity<>(
				dictionaryService.getStatuses(getRoles(token))
						.map(statuses -> statuses.stream().map(StatusDTO::createFrom).collect(toList()))
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(DICTIONARY_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Получение списка городов")
	@GetMapping(value = GET_CITIES)
	public ResponseEntity<ResponseDTO> getCities() {
		return new ResponseEntity<>(
				dictionaryService.getCities()
						.map(cities -> cities.stream().map(CityDTO::createFrom).collect(toList()))
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(DICTIONARY_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Получение списка типов документов с группировкой")
	@GetMapping(value = GET_DOC_TYPES)
	public ResponseEntity<ResponseDTO> getAttachmentTypes() {
		return new ResponseEntity<>(ResponseDTO.goodResponse(ACCEPTED, AttachmentTypeHierarchyDTO.instance()),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Получение словаря ролей")
	@GetMapping(value = GET_USER_ROLES)
	public ResponseEntity<ResponseDTO> getUsersRoles(@NonNull KeycloakAuthenticationToken token) {
		Set<String> allowedRolesForAdmins = getAllowedRolesForAdmins();
		Map<String, String> result = Arrays.stream(UserRoles.values())
				.filter(userRoles -> allowedRolesForAdmins.contains(userRoles.getRoleName()))
				.collect(toMap(UserRoles::getRoleName, UserRoles::getHumanReadable));
		return new ResponseEntity<>(ResponseDTO.goodResponse(ACCEPTED, result),
				HttpStatus.OK);
	}
}
