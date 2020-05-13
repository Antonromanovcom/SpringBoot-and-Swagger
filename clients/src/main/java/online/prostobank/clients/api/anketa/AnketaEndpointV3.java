package online.prostobank.clients.api.anketa;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.services.anketa.AnketaService;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static online.prostobank.clients.api.ApiConstants.*;

/**
 * APIKUB-689
 * <p>
 * Форма регистрации, в которой сначала только поле ввода телефона.
 * При вводе телоефона сразу же создаю заявку
 * Нет смс
 */
@Slf4j
@Benchmark
@JsonLogger
@RestController
@RequestMapping(
		value = ANKETA_V3_CONTROLLER,
		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class AnketaEndpointV3 extends MainAnketaEndpoint {
	private static final String[] allowedMethods = new String[]{CITIES, ORGANIZATIONS, BIDS_CREATE};

	public AnketaEndpointV3(AnketaService anketaService) {
		super(anketaService);
	}

	@Override
	protected Source getSource() {
		return Source.API_ANKETA_V3;
	}

	@Override
	protected String[] getAllowedMethods() {
		return allowedMethods;
	}

	@ApiOperation(value = "Список городов для анкеты")
	@GetMapping(value = CITIES)
	public ResponseEntity<List<CityDTO>> getCitiesDictionary() {
		log.info("{}. /dictionaries/cities. Получение списка городов.", getSource());
		return anketaService.getCitiesDictionary(allowedMethods);
	}
}
