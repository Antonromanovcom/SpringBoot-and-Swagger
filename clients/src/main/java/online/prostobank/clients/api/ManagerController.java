package online.prostobank.clients.api;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.ClientGridRequest;
import online.prostobank.clients.api.dto.managers.ManagerAssignDTO;
import online.prostobank.clients.api.dto.managers.ManagerDTO;
import online.prostobank.clients.api.dto.managers.ManagerGridRequest;
import online.prostobank.clients.services.client.ClientService;
import online.prostobank.clients.services.managers.ManagerService;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static online.prostobank.clients.api.ApiConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "api/manager/", consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
// APIKUB-1884 убрал, на стейдже не работает, всегда 403
//@Secured({ROLE_POS_ADMIN,
//		ROLE_POS_ADMIN_HOME,
//		ROLE_POS_ADMIN_PARTNER,
//		ROLE_POS_OUTER_API_ADMIN,
//})
public class ManagerController {
	private final ManagerService managerService;
	private final ClientService clientService;

	@ApiOperation(value = "Список менеджеров с фильтрацией")
	@PostMapping(value = GET_ALL)
	public ResponseEntity<ResponseDTO> getAll(@NonNull KeycloakAuthenticationToken token,
											  @RequestBody ManagerGridRequest dto) {
		return new ResponseEntity<>(
				managerService.getAll(dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Получение карточки менеджера по uuid")
	@GetMapping(value = FIND_BY_ID)
	public ResponseEntity<ResponseDTO> findById(@NonNull KeycloakAuthenticationToken token,
												@RequestParam("uuid") UUID uuid) {
		return new ResponseEntity<>(
				managerService.findById(uuid)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Сохранение информации по менеджеру после редактирования")
	@PostMapping(value = "save_edit")
	public ResponseEntity<ResponseDTO> saveEdit(@NonNull KeycloakAuthenticationToken token,
												@RequestBody ManagerDTO dto) {
		return new ResponseEntity<>(
				managerService.saveEdit(dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Переназначить на пользователя")
	@PostMapping(value = ASSIGN_TO)
	public ResponseEntity<ResponseDTO> assignTo(@NonNull KeycloakAuthenticationToken token,
												@RequestBody ManagerAssignDTO dto) {
		return new ResponseEntity<>(
				managerService.assignTo(dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Список клиентов с фильтрацией")
	@PostMapping(value = GET_ALL_CLIENTS, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> getAllClients(@NonNull KeycloakAuthenticationToken token,
													 @RequestBody ClientGridRequest dto) {
		return new ResponseEntity<>(
				managerService.getAllClients(dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Список клиентов с фильтрацией по имени пользователя, которому назначены карточки")
	@GetMapping(value = GET_ASSIGNED_TO)
	public ResponseEntity<ResponseDTO> getByAssignedTo(@NonNull KeycloakAuthenticationToken token,
													   @RequestParam String assignedTo) {
		return new ResponseEntity<>(
				clientService.getByAssignedTo(assignedTo)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}
}
