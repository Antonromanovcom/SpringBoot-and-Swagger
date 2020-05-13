package online.prostobank.clients.api.anketa;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.anketa.AccountApplicationDTO;
import online.prostobank.clients.api.dto.anketa.ApplicationAcceptedDTO;
import online.prostobank.clients.api.dto.anketa.ContactInfoVerifyDTO;
import online.prostobank.clients.api.dto.anketa.OrganizationDto;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.services.anketa.AnketaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static online.prostobank.clients.api.ApiConstants.*;

@Slf4j
public abstract class MainAnketaEndpoint {
	final AnketaService anketaService;

	protected MainAnketaEndpoint(AnketaService anketaService) {
		this.anketaService = anketaService;
	}

	protected abstract Source getSource();

	protected abstract String[] getAllowedMethods();

	@ApiOperation(value = "Проверка e-mail на дубликат")
	@RequestMapping(value = "check_email",
			method = RequestMethod.GET,
			produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<String> checkEmail(
			@RequestParam(name = "email") String email
	) {
		log.info(getSource().name() + ". Проверка на дубликат e-mail {}", email);
		return anketaService.checkEmail(email);
	}

	@ApiOperation(value = "Создаем карточку в статусе 'Новый клиент'")
	@PostMapping(value = BOOK)
	public ResponseEntity<ApplicationAcceptedDTO> bookApplication(
			@NotNull(message = "Параметр не задан") @RequestBody ContactInfoVerifyDTO dto,
			@RequestHeader(value = X_FORWARDED_FOR, required = false) String forwardedForHeader
	) {
		log.info("{}. /book. Получили телефон. Создаем карточку клиента в статусе 'Новый клиент' для {}", getSource(), dto.getPhone());
		return anketaService.commonBookApplication(StringUtils.EMPTY, dto, getSource(), forwardedForHeader, getAllowedMethods());
	}

	@ApiOperation(value = "Информация об организации")
	@GetMapping(value = ORGANIZATIONS)
	public ResponseEntity<OrganizationDto> getOrganizationInfo(
			@RequestHeader(X_CSRF_TOKEN) String csrf,
			@Min(value = 0, message = "Значение должно быть неотрицательным числом") @RequestParam(name = "innOrOgrn") String inn,
			@Min(value = 0, message = "Значение должно быть неотрицательным числом") @RequestParam(name = "phone") String phone
	) {
		log.info("{}. /organizations. Информация об организации {}", getSource(), inn);
		return anketaService.getOrganizationInfo(csrf, inn, phone, getAllowedMethods());
	}

	@ApiOperation(value = "Финальная стадия создания карточки")
	@PostMapping(value = BIDS_CREATE)
	public ResponseEntity<ApplicationAcceptedDTO> createApplicationConfirmed(
			@RequestHeader(X_CSRF_TOKEN) String csrf,
			@NotNull(message = "Параметр не задан") @RequestBody AccountApplicationDTO dto
	) {
		log.info("{}. Финальная стадия создания карточки для ИНН '{}' и номером телефона '{}'", getSource(), dto.getContactInfo().getInnOrOgrn(), dto.getContactInfo().getPhone());
		return anketaService.createApplication(csrf, dto);
	}

	@ApiOperation(value = "Проверка финального создания карточки")
	@PostMapping(value = "final_check")
	public ResponseEntity<ApplicationAcceptedDTO> finalCheck(
			@RequestHeader(X_CSRF_TOKEN) String csrf,
			@NotNull(message = "Параметр не задан") @RequestBody AccountApplicationDTO dto
	) {
		log.info("{}. Проверка финального создания карточки для ИНН '{}' и номером телефона '{}'", getSource(), dto.getContactInfo().getInnOrOgrn(), dto.getContactInfo().getPhone());
		return anketaService.finalCheck(csrf, dto);
	}
}
