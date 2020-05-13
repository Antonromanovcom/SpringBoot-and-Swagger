package online.prostobank.clients.api.pos;

import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.CheckType;
import online.prostobank.clients.api.dto.client.ClientDeclineDTO;
import online.prostobank.clients.api.dto.client.HistoryItemDTO;
import online.prostobank.clients.services.client.ClientService;
import online.prostobank.clients.services.validation.InboundDtoValidator;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static online.prostobank.clients.api.ApiConstants.*;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getRoles;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Benchmark
@JsonLogger
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = CLIENT_POS_CONTROLLER, produces = APPLICATION_JSON_UTF8_VALUE)
public class ClientPosController {
	private final ClientService clientService;
	private final InboundDtoValidator validator;

	@ApiOperation(value = "Получение карточки клиента по id")
	@GetMapping(value = FIND_BY_ID)
	public ResponseEntity<ResponseDTO> findById(@NonNull KeycloakAuthenticationToken token, @RequestParam("id") long id) {
		return new ResponseEntity<>(
				clientService.findById(id, getRoles(token))
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK
		);
	}

	@ApiOperation(value = "Выполнить проверку по клиенту")
	@PostMapping(value = CHECK_CLIENT)
	public ResponseEntity<ResponseDTO> checkClient(@RequestParam Long id,
												   @RequestParam CheckType check) {
		return new ResponseEntity<>(
				clientService.checkClient(id, check)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Счет успешно открыт")
	@PostMapping(value = "fulfilled")
	public ResponseEntity<ResponseDTO> fulfilled(@RequestParam @NonNull Long clientId) {
		return new ResponseEntity<>(
				clientService.fulfilled(clientId, 0L)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Счет зарезервирован")
	@PostMapping(value = "reserved")
	public ResponseEntity<ResponseDTO> reserved(@RequestParam @NonNull Long clientId,
												@RequestParam @NonNull Long appId,
												@RequestParam @NonNull String accountNumber,
												@RequestParam @NonNull String requestId
	) {
		return new ResponseEntity<>(
				clientService.reserved(clientId, appId, accountNumber, requestId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Проверка 550-П для списка инн")
	@PostMapping(value = "check_inns", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> checkInns(@RequestBody List<String> inns) {
		return new ResponseEntity<>(
				clientService.recheckP550(inns)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Сохранить системное сообщение")
	@PostMapping(value = SAVE_HISTORY_ITEM, consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<ResponseDTO> saveHistoryItem(@NonNull KeycloakAuthenticationToken token,
													   @RequestBody HistoryItemDTO historyItemDTO) {

		Pair<Boolean, List<String>> validation = validator.validate(historyItemDTO);

		if (validation.getFirst()) {
			try {
				clientService.saveHistoryItem(historyItemDTO, token.getName());
				return new ResponseEntity<>(ResponseDTO.goodResponse(ACCEPTED, true), HttpStatus.OK);
			} catch (Exception ex) {
				return new ResponseEntity<>(ResponseDTO.badResponse(ex.getLocalizedMessage()),
						HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(ResponseDTO.badResponse(String.join(",", validation.getSecond())),
					HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Просигнализировать о необходимости добавить документы в карточку клиента")
	@PostMapping(value = CARD + "/{clientId}/" + NEED_DOCS, consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ResponseDTO> docsNeedSignal(@PathVariable Long clientId) {
		return new ResponseEntity<>(
				clientService.docsNeedSignal(clientId)
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_NOT_FOUND)),
				HttpStatus.OK);
	}

	@ApiOperation(value = "Просигнализировать о необходимости деактивировать заявку клиента")
	@PostMapping(value = DECLINE, consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<ResponseDTO> clientDeactivate(@RequestBody ClientDeclineDTO dto) {
		if (dto == null || dto.getClientId() == null) {
			return new ResponseEntity<>(ResponseDTO.badResponse("Не указаны параметры клиента"),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(
				clientService.accountDecline(dto.getClientId(), dto.getDeclineCause())
						.map(result -> ResponseDTO.goodResponse(ACCEPTED, result))
						.orElseGet(() -> ResponseDTO.badResponse(CLIENT_CARD_NOT_FOUND_OR_NOT_MODIFIED)),
				HttpStatus.OK);
	}
}
