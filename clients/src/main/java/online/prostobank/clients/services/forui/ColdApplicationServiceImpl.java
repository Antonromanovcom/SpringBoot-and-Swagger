package online.prostobank.clients.services.forui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.Result;
import online.prostobank.clients.api.dto.client.ClientCardCreateDTO;
import online.prostobank.clients.connectors.api.KonturService;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.enums.HistoryItemType;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.domain.statuses.StatusValue;
import online.prostobank.clients.services.KycService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Optional;

import static online.prostobank.clients.api.ApiConstants.ACCEPTED;
import static online.prostobank.clients.api.ApiConstants.EXCEPTION_MESSAGE;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getSource;
import static online.prostobank.clients.utils.Utils.wrapProperty;

@Slf4j
@RequiredArgsConstructor
@Service
public class ColdApplicationServiceImpl implements ColdApplicationService {
	private static final String DUPLICATE_ENTRY = "Duplicate entry";

	private final AccountApplicationRepository accountApplicationRepository;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;
	private final KycService kycService;

	@Nonnull
	public ResponseDTO createColdApplication(ClientCardCreateDTO dto, City city) {
		@Nonnull String phone = dto.getPhone();
		String inn = wrapProperty(dto.getInn());
		String email = wrapProperty(dto.getEmail());
		String comment = wrapProperty(dto.getComment());
		try {
			if (!StringUtils.isEmpty(inn)) {
				val applicationList = accountApplicationRepository
						.findAllByClientTaxNumberSameAsOgrnAndActiveIsTrue(inn);
				if (!applicationList.isEmpty()) {
					log.error("Duplicate entry, unable to submit cold application");
					return new ResponseDTO(DUPLICATE_ENTRY, Result.ERROR, "");
				}
			}

			val clientValue = buildClientValue(email, phone, inn, kycService.getInfoResult(inn));
			val currentColdApplication = buildAccountApplication(city, comment, clientValue);

			AccountApplication clientCard = repositoryWrapper.saveAccountApplication(currentColdApplication).getSecond();

			Long clientId = clientCard.getId();
			String clientPhone = clientCard.getClient().getPhone();
			String clientInn = clientCard.getClient().getInn();
			log.info("Создана карточка клиента: {}, номер телефона: {}, ИНН: {}", clientId, clientPhone, clientInn);

			return ResponseDTO.goodResponse(ACCEPTED, clientCard);
		} catch (Exception e) {
			log.error("Failed to submit cold application:: {}", ExceptionUtils.getRootCauseMessage(e));
			return ResponseDTO.badResponse(EXCEPTION_MESSAGE);
		}
	}

	private AccountApplication buildAccountApplication(@Nonnull City city,
													   @Nonnull String comment,
													   @Nonnull ClientValue clientValue) {
		val currentColdApplication = new AccountApplication(
				city,
				clientValue,
				Source.API_TM
		);
		if (StringUtils.isNotBlank(comment)) {
			currentColdApplication.addHistoryRecord(comment, HistoryItemType.COMMENT);
		}
		getSource().ifPresent(currentColdApplication::setSource);

		return currentColdApplication;
	}

	@Nonnull
	private ClientValue buildClientValue(
			@Nonnull String email,
			@Nonnull String phone,
			@Nonnull String inn,
			KonturService.InfoResult infoResult
	) {
		String name = Optional.ofNullable(infoResult)
				.map(result -> result.name)
				.orElse("");
		String head = Optional.ofNullable(infoResult)
				.map(result -> result.headName)
				.orElse("");

		val client = new ClientValue(name, email, phone, inn, "", head);

		Optional.ofNullable(infoResult)
				.map(result -> result.kpp)
				.ifPresent(client::setKpp);
		Optional.ofNullable(infoResult)
				.map(result -> result.shortName)
				.ifPresent(client::setShortName);
		return client;
	}
}
