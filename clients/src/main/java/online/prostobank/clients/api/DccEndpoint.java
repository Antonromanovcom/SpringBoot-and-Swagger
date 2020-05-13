package online.prostobank.clients.api;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import online.prostobank.clients.api.dto.DccDTO;
import online.prostobank.clients.api.dto.DccResponseDTO;
import online.prostobank.clients.services.dcc.DccService;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import static online.prostobank.clients.api.ApiConstants.DCC_CONTROLLER;

@Benchmark
@JsonLogger
@RequiredArgsConstructor
@RestController
@RequestMapping(DCC_CONTROLLER)
@Transactional
public class DccEndpoint {
	private final DccService dccService;

	/**
	 * Создание заявки через domestic call center (звонилка)
	 *
	 * @param dto - json с полями. см. DccDTO класс
	 * @return dccResponseDTO - json с результатом действия
	 */
	@ApiOperation(value = "Создание заявке по команде из системы dcc (domestic call center) - звонилка")
	@RequestMapping(value = "create",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE
	)
	// todo нет такой роли в ролевой модели
	@PreAuthorize("hasAnyAuthority('ROLE_outer.api')")
	public ResponseEntity<DccResponseDTO> endpoint(
			@NotNull(message = "Параметр не задан") @RequestBody DccDTO dto
	) {
		return dccService.createApplication(dto);
	}
}
