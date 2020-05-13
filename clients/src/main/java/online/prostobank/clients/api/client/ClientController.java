package online.prostobank.clients.api.client;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.*;
import online.prostobank.clients.services.client.ClientService;
import online.prostobank.clients.services.validation.InboundDtoValidator;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static online.prostobank.clients.api.ApiConstants.*;
import static online.prostobank.clients.api.dto.client.CheckType.ARRESTS;
import static online.prostobank.clients.api.dto.client.CheckType.PASSPORT;
import static online.prostobank.clients.security.UserRolesConstants.ROLE_POS_OUTER_API_ADMIN;
import static online.prostobank.clients.security.UserRolesConstants.ROLE_POS_OUTER_API_MANAGER;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.*;
import static online.prostobank.clients.services.client.ClientAttachmentUtils.getStreamResponse;

@Benchmark
@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = CLIENT_CONTROLLER, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
// APIKUB-1884 убрал, на стейдже не работает, всегда 403
//@Secured({ROLE_POS_ADMIN,
//		ROLE_POS_FRONT,
//		ROLE_POS_ADMIN_HOME,
//		ROLE_POS_FRONT_HOME,
//		ROLE_POS_ADMIN_PARTNER,
//		ROLE_POS_FRONT_PARTNER,
//		ROLE_POS_OUTER_API_ADMIN,
//		ROLE_POS_OUTER_API_MANAGER,
//		ROLE_POS_CONSULTANT
//})
public class ClientController {
	private final ClientService clientService;
	private final InboundDtoValidator validator;

	@ApiOperation(value = "Список клиентов с фильтрацией")
	@PostMapping(value = GET_ALL, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> getAll(@NonNull KeycloakAuthenticationToken token, @RequestBody ClientGridRequest dto) {
		return new ResponseEntity<>(
				clientService.getAll(dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Создать карточку клиента")
	@PostMapping(value = CREATE_CLIENT_CARD, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_CONSULTANT,
//	})
	public ResponseEntity<ResponseDTO> createClientCard(@NonNull KeycloakAuthenticationToken token,
														@RequestBody ClientCardCreateDTO dto) {

		Pair<Boolean, List<String>> validation = validator.validate(dto);

		if (validation.getFirst()) {
			try {
				return new ResponseEntity<>(
						clientService.createClientCard(dto),
						HttpStatus.OK
				);
			} catch (Exception ex) {
				return new ResponseEntity<>(ResponseDTO.badResponse("При создании карточки произошла ошибка. Пожалуйста, попробуйте еще раз."),
						HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Получение карточки клиента по id")
	@GetMapping(value = FIND_BY_ID)
	public ResponseEntity<ResponseDTO> findById(@NonNull KeycloakAuthenticationToken token, @RequestParam("id") long id) {
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<>(
				clientService.findById(id, getRoles(token))
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				headers, HttpStatus.OK
		);
	}

	@ApiOperation(value = "Сохранение информации по клиенту после редактирования")
	@PostMapping(value = SAVE_EDIT_CLIENT_INFO, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> saveEditClientInfo(@NonNull KeycloakAuthenticationToken token, @RequestBody ClientEditDTO dto) {
		try {
			return new ResponseEntity<>(
					clientService.saveEditClientInfo(dto)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
					HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<>(ResponseDTO.badResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Сохранение информации опросника после редактирования")
	@PostMapping(value = SAVE_EDIT_QUESTIONNAIRE, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> saveEditQuestionnaire(@NonNull KeycloakAuthenticationToken token, @RequestBody QuestionnaireDTO dto) {
		return new ResponseEntity<>(
				clientService.saveEditQuestionnaire(dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Выполнить проверку по клиенту")
	@PostMapping(value = CHECK_CLIENT)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> checkClient(@NonNull KeycloakAuthenticationToken token,
												   @RequestParam Long id,
												   @RequestParam CheckType check) {
		if (check == ARRESTS || check == PASSPORT) {
			if (notAllowed(getRoles(token), ROLE_POS_OUTER_API_ADMIN, ROLE_POS_OUTER_API_MANAGER)) {
				return new ResponseEntity<>(ResponseDTO.badResponse("Forbidden"), HttpStatus.FORBIDDEN);
			}
		}
		return new ResponseEntity<>(
				clientService.checkClient(id, check)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Сохранить комментарий")
	@PostMapping(value = SAVE_COMMENT)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> saveComment(@NonNull KeycloakAuthenticationToken token,
												   @RequestParam Long id,
												   @RequestParam String text) {
		return new ResponseEntity<>(
				clientService.saveComment(id, text, token.getName())
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Перевыслать пользователю")
	@PostMapping(value = "resend_to_user")
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> resendToUser(@NonNull KeycloakAuthenticationToken token,
													@RequestParam Long id) {
		return new ResponseEntity<>(
				clientService.resendToUser(id)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Отправка СМС-напоминания")
	@PostMapping(value = SMS_REMINDER)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> smsReminder(@NonNull KeycloakAuthenticationToken token,
												   @RequestParam Long id) {
		return new ResponseEntity<>(
				clientService.smsReminder(id)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Экспорт в PDF")
	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = EXPORT_PDF, produces = {MediaType.APPLICATION_PDF_VALUE})
	public ResponseEntity<InputStreamResource> exportPdf(@NonNull KeycloakAuthenticationToken token,
														 @RequestParam Long id) {
		try {
			return clientService.exportPdf(id)
					.map(bytes -> getStreamResponse(bytes, "client_" + id + ".pdf", false))
					.orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
		} catch (Exception ex) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@ApiOperation(value = "Переназначить на текущего пользователя")
	@PostMapping(value = ASSIGN_TO_ME)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//	})
	public ResponseEntity<ResponseDTO> assignToMe(@NonNull KeycloakAuthenticationToken token,
												  @RequestParam Long id) {
		return new ResponseEntity<>(
				clientService.assignToMe(id, token.getName())
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Переназначить на пользователя")
	@PostMapping(value = ASSIGN_TO)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//	})
	public ResponseEntity<ResponseDTO> assignTo(@NonNull KeycloakAuthenticationToken token,
												@RequestParam Long id,
												@RequestParam String newUser) {
		return new ResponseEntity<>(
				clientService.assignTo(id, newUser)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Начать работу над заявкой")
	@PostMapping(value = START_WORK)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> startWork(@NonNull KeycloakAuthenticationToken token,
												 @RequestParam Long id) {
		return new ResponseEntity<>(
				clientService.startWork(id, token.getName())
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Отправка смс с проверками")
	@PostMapping(value = SMS_CHECK)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> smsCheck(@NonNull KeycloakAuthenticationToken token,
												@RequestParam Long id) {
		return new ResponseEntity<>(
				clientService.smsCheck(id)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Сбросить код подтверждения")
	@PostMapping(value = RESET_SMS_CHECK)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> resetSmsCheck(@NonNull KeycloakAuthenticationToken token,
													 @RequestParam Long id) {
		return new ResponseEntity<>(
				clientService.resetSmsCheck(id)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Смс подтверждена")
	@PostMapping(value = SMS_CONFIRMATION)
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> smsConfirmation(@NonNull KeycloakAuthenticationToken token,
													   @RequestParam Long id,
													   @RequestParam String code) {
		return new ResponseEntity<>(
				clientService.smsConfirmation(id, code)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Редактирование паспорта")
	@PostMapping(value = EDIT_PASSPORT + "/{clientId}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> editPassport(@NonNull KeycloakAuthenticationToken token,
													@PathVariable Long clientId,
													@RequestBody PassportDTO passportDTO) {
		Pair<Boolean, List<String>> validation = validator.validate(passportDTO);

		if (validation.getFirst()) {
			return new ResponseEntity<>(
					clientService.editPassport(clientId, passportDTO)
							.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
							.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Отправка сообщения клиенту от менеджера")
	@PostMapping(value = SEND_MESSAGE_TO_CLIENT, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> sendMessageToClient(@NonNull KeycloakAuthenticationToken token,
														   @RequestBody MessageDTO message) {
		return new ResponseEntity<>(
				clientService.sendMessageToClient(message, UUID.fromString(getKeycloakId(token)), token.getName())
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Сохранение информации о тарифе и времени перезвона")
	@PostMapping(value = CARD + "/{clientId}/" + SAVE_AUX_INFO, consumes = {MediaType.APPLICATION_JSON_VALUE})
//	@Secured({ROLE_POS_ADMIN,
//			ROLE_POS_FRONT,
//			ROLE_POS_ADMIN_HOME,
//			ROLE_POS_FRONT_HOME,
//			ROLE_POS_ADMIN_PARTNER,
//			ROLE_POS_FRONT_PARTNER,
//			ROLE_POS_OUTER_API_ADMIN,
//			ROLE_POS_OUTER_API_MANAGER,
//	})
	public ResponseEntity<ResponseDTO> saveAuxClientInfo(@NonNull KeycloakAuthenticationToken token, @PathVariable Long clientId,
														 @RequestBody ClientAuxInfoDTO dto) {

		return new ResponseEntity<>(
				clientService.setClientAuxInfo(clientId, dto)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}
}
