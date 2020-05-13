package online.prostobank.clients.services.dcc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.prostobank.clients.api.dto.DccDTO;
import online.prostobank.clients.api.dto.DccResponseDTO;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.repository.CityRepository;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.utils.TaxNumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static online.prostobank.clients.domain.enums.Source.API_DCC;
import static online.prostobank.clients.utils.Utils.UNKNOWN_CITY;

@RequiredArgsConstructor
@Slf4j
@Service
public class DccServiceImpl implements DccService {
	private final AccountApplicationRepository accountApplicationRepository;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;
	private final CityRepository cityRepository;

	@Override
	public ResponseEntity<DccResponseDTO> createApplication(DccDTO dto) {
		String inn = "";
		String ogrn = "";
		val phone = StringUtils.trim(dto.getPhone());
		val operator = StringUtils.trim(dto.getOperator());

		val fio = StringUtils.trim(dto.getFio());
		val address = StringUtils.trim(dto.getAddress());
		val email = StringUtils.trim(dto.getEmail());
		val comment = StringUtils.trim(dto.getComment());

		val taxNumber = StringUtils.trim(dto.getTaxNumber());

		// валидация ИНН или ОГРН. обязательное поле
		if (TaxNumberUtils.isInnValid(taxNumber)) {
			inn = taxNumber;
		} else if (TaxNumberUtils.isOgrnValid(taxNumber)) {
			ogrn = taxNumber;
		} else {
			// невалидные данные
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DccResponseDTO.WRONG_INN);
		}

		if (StringUtils.isBlank(operator)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DccResponseDTO.NO_OPERATOR_PROVIDED);
		}

		// проверяем существует ли заявка
		if (accountApplicationRepository.countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCaseAndActiveTrue(taxNumber) > 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DccResponseDTO.ALREADY_EXIST);
		}

		if (StringUtils.isBlank(phone)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DccResponseDTO.NO_PHONE_PROVIDED);
		}

		Optional<City> byName = cityRepository.findByNameIgnoreCase(UNKNOWN_CITY);

		if (!byName.isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DccResponseDTO.WRONG_CITY);
		}

		ClientValue clientValue = new ClientValue(fio, email, phone, inn, ogrn, fio);

		AccountApplication accountApplication = new AccountApplication(
				byName.get(),
				clientValue,
				API_DCC
		);
		accountApplication.setComment(comment);
		accountApplication.setCreator(operator);
		accountApplication.setComment(comment);
		accountApplication.getClient().setAddress(address);

		accountApplication = repositoryWrapper.saveAccountApplication(accountApplication).getSecond();

		log.info("заявка " + accountApplication.getId() + " успешно заведена");

		return ResponseEntity.ok(DccResponseDTO.OK);
	}
}
