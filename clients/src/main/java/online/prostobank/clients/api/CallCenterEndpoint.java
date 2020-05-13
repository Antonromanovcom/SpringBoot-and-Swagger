package online.prostobank.clients.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.CallCenterAccountApplicationDTO;
import online.prostobank.clients.api.dto.CallCenterApiResponseDto;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.enums.ClientType;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.repository.CityRepository;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.utils.TaxNumberUtils;
import online.prostobank.clients.utils.aspects.Benchmark;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static online.prostobank.clients.api.ApiConstants.CALL_CENTER_CONTROLLER;
import static online.prostobank.clients.domain.enums.Source.API_OUTER_CALL_CENTER;

/**
 * Точка для обращения внешнего колл центра по апи
 */
@Slf4j
@Benchmark
@JsonLogger
@RequiredArgsConstructor
@RestController
@RequestMapping(
		value = CALL_CENTER_CONTROLLER,
		consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
		produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Transactional
public class CallCenterEndpoint {
	private final CityRepository cityRepository;
	private final AccountApplicationRepository accountApplicationRepository;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;

	/**
	 * Создание заявки внешним коллцентром
	 */
	@PostMapping(value = "/create")
	// todo нет такой роли в ролевой модели
	@PreAuthorize("hasAnyAuthority('outer.api')")
	@ResponseBody
	public ResponseEntity<CallCenterApiResponseDto> createApplication(
			@NotNull(message = "Параметр не задан") @RequestBody CallCenterAccountApplicationDTO dto
	) {

		log.info("Начинается создание заявки внешним колл-центром");

		//todo добавить логирование токена хотя бы того, кто выполняет запрос

		String inn = "";
		String ogrn = "";
		CallCenterApiResponseDto responseDto = new CallCenterApiResponseDto();

		// валидация ИНН или ОГРН. обязательное поле
		if (TaxNumberUtils.isInnValid(StringUtils.trim(dto.taxNumber))) {
			inn = StringUtils.trim(dto.taxNumber);
		} else if (TaxNumberUtils.isOgrnValid(StringUtils.trim(dto.taxNumber))) {
			ogrn = StringUtils.trim(dto.taxNumber);
		} else {
			// невалидные данные
			responseDto.errorMessage = "Не верный ИНН/ОГРН. Обязательное поле";
			log.warn(responseDto.errorMessage);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}
		log.info("Успешное валидирование ИНН/ОГРН " + dto.taxNumber);

		if (accountApplicationRepository.countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCaseAndActiveTrue(dto.taxNumber) > 0) {
			responseDto.errorMessage = "Пользователь с ИНН/ОГРН " + dto.taxNumber + " уже зарегистрирован";
			log.warn(responseDto.errorMessage);
			return ResponseEntity.ok(responseDto);
		}

		if (StringUtils.isBlank(StringUtils.trim(dto.phone))) {
			// пустой телефон. обязательное поле
			responseDto.errorMessage = "Не указан номер телефона. Обязательное поле";
			log.warn(responseDto.errorMessage);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		if (ClientValue.normalizePhone(dto.phone).length() != 10) {
			responseDto.errorMessage = "Не корректный номер телефона. Обязательное поле";
			log.warn(responseDto.errorMessage);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}
		log.info("Успешное валидирование номера телефона " + dto.phone);

		String city = dto.city;
		city = StringUtils.capitalize(city.toLowerCase());
		Optional<City> byName = cityRepository.findByNameIgnoreCase(StringUtils.trim(city));
		if (!byName.isPresent()) {
			// неизвестный город
			responseDto.errorMessage = "Не работаем в этом городе. Обязательное поле";
			log.warn(responseDto.errorMessage);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}
		log.info("Успешное валидирование города " + city);

		if (dto.legalType == null) {
			responseDto.errorMessage = "Не указан признак ИП/ООО. Обязательное поле";
			log.warn(responseDto.errorMessage);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		} else if (dto.legalType != ClientType.LLC
				&& dto.legalType != ClientType.SP) {
			responseDto.errorMessage = "Указан неизвестный признак ИП/ООО. Обязательное поле";
			log.warn(responseDto.errorMessage + dto.legalType);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		// проверка длин
		if (checkStringFieldLength(dto.companyName, responseDto, "\"companyName\"", 255))
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);

		if (checkStringFieldLength(dto.fio, responseDto, "\"fio\"", 255)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		if (checkStringFieldLength(dto.email, responseDto, "\"email\"", 255)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		if (checkStringFieldLength(dto.phone, responseDto, "\"phone\"", 255)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		if (checkStringFieldLength(dto.address, responseDto, "\"address\"", 255)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		if (checkStringFieldLength(dto.comment, responseDto, "\"comment\"", 2000)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		ClientValue clientValue;
		if (dto.legalType == ClientType.LLC) { // ООО
			clientValue = new ClientValue(StringUtils.trim(dto.companyName), StringUtils.trim(dto.email), StringUtils.trim(dto.phone), inn, ogrn, StringUtils.trim(dto.fio));
		} else { // ИП
			clientValue = new ClientValue(StringUtils.trim(dto.fio), StringUtils.trim(dto.email), StringUtils.trim(dto.phone), inn, ogrn, StringUtils.trim(dto.fio));
		}

		AccountApplication accountApplication = new AccountApplication(
				byName.get(),
				clientValue,
				API_OUTER_CALL_CENTER
		);
		accountApplication = repositoryWrapper.saveAccountApplication(accountApplication).getSecond();
		accountApplication.getClient().setAddress(StringUtils.trim(dto.address));
		if (!StringUtils.isEmpty(dto.comment)) {
			accountApplication.setComment(dto.comment);
		}

		accountApplication = repositoryWrapper.saveAccountApplication(accountApplication).getSecond();
		responseDto.successMessage = "Успешное создание заявки номер " + accountApplication.getId();
		log.info("{}. ИНН: '{}', ФИО: '{}', Телефон: '{}', Адрес: '{}', e-mail: '{}', город: '{}', комментарий: '{}', тип организации: '{}'",
				responseDto.successMessage, dto.taxNumber, dto.fio, dto.phone, dto.address, dto.email, city, dto.comment,
				dto.legalType);
		return ResponseEntity.ok(responseDto);
	}

	private boolean checkStringFieldLength(String fieldToCheck, CallCenterApiResponseDto responseDto, String fieldName, int maxLength) {
		if (StringUtils.isNotBlank(fieldToCheck) && fieldToCheck.length() > maxLength) {
			responseDto.errorMessage = "Длинна поля " + fieldName + " превышает допустимое";
			log.warn(responseDto.errorMessage);
			return true;
		}
		log.info("Успешное валидирование поля " + fieldName + " " + fieldToCheck);
		return false;
	}
}

