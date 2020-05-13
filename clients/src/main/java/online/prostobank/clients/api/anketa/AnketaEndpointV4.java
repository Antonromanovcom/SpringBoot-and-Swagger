package online.prostobank.clients.api.anketa;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.anketa.PromocodeInfoDTO;
import online.prostobank.clients.api.dto.anketa.PromocodeInfoResponseDTO;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.services.anketa.AnketaService;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

import static online.prostobank.clients.api.ApiConstants.*;

/**
 * APIKUB-689
 * <p>
 * Форма регистрации, в которой сначала только поле ввода телефона.
 * При вводе телоефона сразу же создаю заявку
 * Нет смс и выбора города
 */
@Slf4j
@Benchmark
@JsonLogger
@RestController
@RequestMapping(
		value = ANKETA_V4_CONTROLLER,
		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class AnketaEndpointV4 extends MainAnketaEndpoint {
	private static final String[] allowedMethods = new String[]{PROMOCODE, ORGANIZATIONS, BIDS_CREATE};

	public AnketaEndpointV4(AnketaService anketaService) {
		super(anketaService);
	}

	@Override
	protected Source getSource() {
		return Source.API_ANKETA_V4;
	}

	@Override
	protected String[] getAllowedMethods() {
		return allowedMethods;
	}

	@ApiOperation(value = "Сохранение промокода")
	@PostMapping(value = PROMOCODE)
	public ResponseEntity<PromocodeInfoResponseDTO> savePromocode(
			@RequestHeader(X_CSRF_TOKEN) String csrf,
			@NotNull(message = "Параметр не задан") @RequestBody PromocodeInfoDTO dto
	) {
		log.info("{}. Сохранение промокода {} для телефона {}", getSource(), dto.getPromocode(), dto.getPhone());
		return anketaService.getPromocodeInfoResponseDTO(csrf, dto, getAllowedMethods());
	}
}
