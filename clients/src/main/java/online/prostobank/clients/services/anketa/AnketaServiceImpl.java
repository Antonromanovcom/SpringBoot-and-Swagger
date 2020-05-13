package online.prostobank.clients.services.anketa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.anketa.*;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.api.dto.state.StateSetterDTO;
import online.prostobank.clients.config.properties.MainAnketaEndpointProperties;
import online.prostobank.clients.connectors.api.KonturService;
import online.prostobank.clients.connectors.exceptions.AnticorruptionException;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.Utm;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.domain.events.AccountApplicationDuplicateAttemptEvent;
import online.prostobank.clients.domain.events.AccountApplicationEntityCreatedEvent;
import online.prostobank.clients.domain.events.DeactivateClientEvent;
import online.prostobank.clients.domain.events.SendUtmEvent;
import online.prostobank.clients.domain.exceptions.EmailDuplicateException;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.repository.CityRepository;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.services.state.StateMachineServiceI;
import online.prostobank.clients.services.state.StatusInfoDTO;
import online.prostobank.clients.utils.TaxNumberUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static online.prostobank.clients.api.ApiConstants.*;
import static online.prostobank.clients.api.dto.anketa.UtmDTO.createFrom;
import static online.prostobank.clients.connectors.KonturServiceImpl.EMPTY_RESULT_BY_TAX_NUMBER;
import static online.prostobank.clients.domain.state.state.ClientStates.*;
import static online.prostobank.clients.utils.Utils.getAccountLink;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AnketaServiceImpl implements AnketaService {
	private static final String DEFAULT_RESPONSE = "Спасибо, Ваша заявка на резервирование счета принята в работу. В ближайшее время Вам позвонит наш менеджер.";
	private static final String ERROR_RESPONSE_CALL = "Спасибо, что оставили заявку на резервирование счета. Ожидайте звонка, с Вами свяжется наш менеджер";
	private static final String ERROR_RESPONSE = "Спасибо, что оставили заявку на резервирование счета. C Вами свяжется наш менеджер";
	private static final String DECLINE_RESPONSE = "К сожалению, мы не можем открыть вам счет без визита в банк. Мы отправили на вашу почту подробности.";
	private static final String NOT_FOUND_IN_REGISTRY = "Не найден в реестре";

	private final MainAnketaEndpointProperties properties;
	private final AccountApplicationRepository accountApplicationRepository;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;
	private final CityRepository cityRepository;
	private final StandardPBEStringEncryptor encryptors;
	private final ApplicationEventPublisher bus;
	private final IAnticorruption anticorruption;
	private final StateMachineServiceI stateMachineService;

	// список актуальных токенов
	private volatile static Set<String> csrfSet = new HashSet<>();
	// подсчёт кол-ва попыток использования токена
	private volatile static Map<String, Integer> errorCounterForToken = new HashMap<>();

	@Override
	public ResponseEntity<String> checkEmail(String email) {
		return accountApplicationRepository.countAllByClientEmailAndActiveIsTrue(email) == 0
				? new ResponseEntity<>("ok", HttpStatus.OK)
				: new ResponseEntity<>("fail", HttpStatus.OK);
	}

	/**
	 * Получение списка городов. Генерится первый токен. Старт цепочки токенов.
	 * Нельзя выполнить запрос из середины, не выполнив предыдущие
	 *
	 * @return список городов для фронта
	 */
	@Override
	public ResponseEntity<List<CityDTO>> getCitiesDictionary(String... nextMethodName) {
		String partner = properties.getPartner();
		List<CityDTO> cities = cityRepository.findInSpecialOrderToList().stream()
				.map(city -> CityDTO.createFrom(city, partner))
				.collect(toList());

		String newToken = createNewToken(StringUtils.EMPTY, nextMethodName);
		log.info("New token in new chain {}", newToken);
		return ResponseEntity
				.ok()
				.header(X_CSRF_TOKEN, newToken)
				.body(cities);
	}

	/**
	 * Оставляем заявку в статусе "Создана"
	 */
	@Override
	public ResponseEntity<ApplicationAcceptedDTO> commonBookApplication(String csrf,
																		ContactInfoVerifyDTO dto,
																		Source source,
																		String forwardedForHeader,
																		String... nextMethod) {
		AccountApplication application = getLastOrCreateNewAccountApplication(dto, source, forwardedForHeader);
		if (!application.getCity().isServiced()) {
			// APIKUB-1608
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		ApplicationAcceptedDTO acceptedDTO = new ApplicationAcceptedDTO();

		if (application.getClientState() == NEW_CLIENT) {
			application = repositoryWrapper.saveAccountApplication(application).getSecond();
			log.info("Успешно оставили заявку {} в статусе \"Создана\" для {}", application.getId(), dto.getPhone());
			return ResponseEntity.ok().header(X_CSRF_TOKEN, createNewToken(csrf, nextMethod)).body(acceptedDTO);
		} else {
			acceptedDTO.setMessage(DEFAULT_RESPONSE);
			acceptedDTO.setDeveloperPayload("Проблема при создании заявки");
			log.warn(acceptedDTO.getDeveloperPayload() + " для телефона {}", dto.getPhone());

			return ApplicationAcceptedDTO.error(acceptedDTO).response(HttpStatus.BAD_REQUEST);
		}
	}


	/**
	 * Получение информации об организации по ИНН
	 */
	@Override
	public ResponseEntity<OrganizationDto> getOrganizationInfo(String csrf,
															   String inn,
															   String phone,
															   String... nextMethodName) {
		log.info("Получение информации об организации по ИНН '{}'", inn);
		if (isNotValidCsrf(csrf, ORGANIZATIONS)) {
			// не прошёл проверку csrf
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		OrganizationDto orgDto = new OrganizationDto();

		if (!TaxNumberUtils.isInnValid(inn)) {
			orgDto.setError("Введён невалидный ИНН/ОГРН");
			log.warn("Попытка получения информации по невалидному ИНН/ОГРН '{}'", inn);
			// новый токен не нужен. начнаем считать кол-во попыток по этому токену
			return new ResponseEntity<>(orgDto, HttpStatus.OK);
		}

		List<AccountApplication> unconfirmed = accountApplicationRepository.findUnconfirmedByClientPhoneAndActiveIsTrueOrderByIdDesc(phone);

		if (CollectionUtils.isEmpty(unconfirmed)) {
			log.warn("Trying to get information for application with phone {} and tax number {} that doesn't exist or not in a proper status", phone, inn);
			return new ResponseEntity<>(orgDto, HttpStatus.BAD_REQUEST);
		}
		AccountApplication accountApplication = unconfirmed.get(0);
		accountApplication.getClient().setInnOgrn(inn);
		accountApplication = repositoryWrapper.saveAccountApplication(accountApplication).getSecond();

		KonturService.InfoResult ir = accountApplication.clientInfoAndUpdate();

		if (StringUtils.isBlank(ir.errorText)) {
			orgDto.setInn(ir.inn);
			orgDto.setOgrn(ir.ogrn);
			orgDto.setOrgName(ir.name);
			orgDto.setRegDate(ir.regDate);
			orgDto.setRegPlace(ir.regPlace);
			orgDto.setClientName(ir.headName);
			orgDto.setKpp(ir.kpp);
			orgDto.setType(ir.type);

		} else {
			orgDto.setError(ir.errorText);
		}

		ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();

		if (StringUtils.isBlank(ir.errorText) || ir.errorText.equals(EMPTY_RESULT_BY_TAX_NUMBER)) {
			// корректный результат
			bodyBuilder = bodyBuilder.header(X_CSRF_TOKEN, createNewToken(csrf, nextMethodName));
		}

		// если ошибка (errorText не пустой), то новый токен не нужен. начинаем считать кол-во попыток по этому токену
		return bodyBuilder.body(orgDto);
	}

	/**
	 * Финальная стадия создания заявки.
	 * Заявка в статусе CONTACT_INFO_CONFIRMED (номер телефона клиента подверждён) продвигается до статуса Новая (New)
	 * Для этого дозаполняется e-mail, проводятся проверки, подтягивается информация о компании (названиефио ген. дира и чредителей), деактивируются дубли.
	 *
	 * @param csrf токен
	 * @param dto  информация о клиенте (почта, телефон..), паспорт, смс код подтверждения телефона
	 * @return Возвращает 400, если нет заявки по переданному номеру телефона
	 * Возвращает 400, если смс токен не соответствует высланному
	 * Возвращает 200, но ок = false и message = DEFAULT_RESPONSE, developerPayload = "ИНН/ОГРН _номер_инн_ невалиден"
	 * Возвращает 200, но ок = false и message = DEFAULT_RESPONSE, developerPayload с сообщением Exception, если произошла ошибка
	 * Возвращает 200 ок = true и message = DEFAULT_RESPONSE, developerPayload = "Нужно связаться с клиентом, данная компания не найдена в реестре"; если инн корректен, но компания пока не зарегистрирована.
	 * Возвращает 200 ок = true и message = "Вам зарезервирован расчетный счет № _номерсчёта_. В ближайшее время Вам позвонит наш менеджер. Подгрузите документы в личном кабинете."
	 */
	@Override
	public ResponseEntity<ApplicationAcceptedDTO> createApplication(String csrf,
																	AccountApplicationDTO dto) {
		if (isNotValidCsrf(csrf, BIDS_CREATE)) {
			// не прошёл проверку csrf
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		ApplicationAcceptedDTO acceptedDTO = new ApplicationAcceptedDTO();

		try {
			return createApplicationUnconfirmed(csrf, dto, acceptedDTO);
		} catch (AnticorruptionException ex) {
			return fillUnexpectedReply(csrf, acceptedDTO, ex);
		} catch (EmailDuplicateException e) {
			// не должно вызываться. Потому что покрывается методом /check_email при заполнении анкеты
			log.warn(e.getLocalizedMessage());
			return err(ERROR_RESPONSE_CALL, e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<ApplicationAcceptedDTO> finalCheck(String csrf, AccountApplicationDTO dto) {
		try {
			// todo
			return err();
		} catch (Exception e) {
			return err(ERROR_RESPONSE, e.getLocalizedMessage(), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<PromocodeInfoResponseDTO> getPromocodeInfoResponseDTO(
			String csrf,
			PromocodeInfoDTO dto,
			String... allowedMethods) {

		if (isNotValidCsrf(csrf, PROMOCODE)) {
			// не прошёл проверку csrf
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		List<AccountApplication> foundApps = accountApplicationRepository
				.findUnconfirmedByClientPhoneAndActiveIsTrueOrderByIdDesc(ClientValue.normalizePhone(dto.getPhone()));
		PromocodeInfoResponseDTO response = new PromocodeInfoResponseDTO();

		if (foundApps.isEmpty()) {
			String errorMessage = String.format("Заявка по номеру телефона %s не найдена", dto.getPhone());
			log.warn(errorMessage);
			response.setOk(false);
			response.setErrorMessage(errorMessage);
			response.setDeveloperPayload(DeveloperPayloadDTO.PHONE);
			return ResponseEntity.status(BAD_REQUEST).body(response);
		}

		final AccountApplication accountApplication = foundApps.get(0);

		if (!TaxNumberUtils.isInnValid(dto.getPromocode()) && !TaxNumberUtils.isOgrnValid(dto.getPromocode())) {
			accountApplication.addHistoryRecord(String.format("Попытка использовать невалидный промокод %s", dto.getPromocode()));
			response.setOk(false);
			response.setErrorMessage("Введён невалидный промокод");
			response.setDeveloperPayload(DeveloperPayloadDTO.PROMO);
			return ResponseEntity.status(BAD_REQUEST).body(response);
		}

		Long foundRegistered = accountApplicationRepository
				.countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCaseAndActiveTrue(dto.getPromocode());
		ClientValue client = accountApplication.getClient();

		response.setOk(!dto.getPromocode().equals(client.getInn()) &&
				foundRegistered > 0 &&
				client.setPromoInn(dto.getPromocode()));
		response.setErrorMessage(response.isOk() ? StringUtils.EMPTY : "Введён невалидный промокод");
		response.setDeveloperPayload(response.isOk() ? DeveloperPayloadDTO.EMPTY : DeveloperPayloadDTO.PROMO);

		accountApplication.addHistoryRecord(
				response.isOk()
						? String.format("Введён промокод %s", dto.getPromocode())
						: String.format("Попытка использовать невалидный промокод %s", dto.getPromocode())
		);

		if (response.isOk()) {
			return ResponseEntity.ok().header(X_CSRF_TOKEN, createNewToken(csrf, allowedMethods)).body(response);
		} else {
			return ResponseEntity.status(BAD_REQUEST).body(response);
		}
	}


	/**
	 * Проверка токена
	 *
	 * @param token      csrf токен
	 * @param methodName название метода
	 * @return прошёл/не прошёл проверку токен
	 */
	private boolean isNotValidCsrf(String token, String methodName) {
		log.info("Проверка токена '{}' для метода '{}'", token, methodName);
		// неизвестный токен
		if (csrfSet.stream().noneMatch(ct -> ct.equals(token))) {
			log.warn("Неизвестный токен '{}' для метода '{}'", token, methodName);
			return true;
		}

		String allowedMethodsJoined = decryptToken(token)[1];
		String[] allowedMethods = allowedMethodsJoined.split(",");

		boolean isAllowedMethodInToken = Arrays.asList(allowedMethods).contains(methodName);

		if (!isAllowedMethodInToken) {
			// если новый токен используют не для того метода
			log.warn("Попытка использовать токен '{}' для методов '{}' в методе '{}'",
					token,
					allowedMethodsJoined,
					methodName);

			return true;
		}

		Integer attempts = errorCounterForToken.get(token);
		if (attempts == null) {
			errorCounterForToken.put(token, 1);
			return false;
		}

		if (attempts > MAX_AVAILABLE_ATTEMPTS) {
			log.warn("Превышен максимально допустиый предел количества попыток");
			return true;
		}

		attempts += 1;
		errorCounterForToken.put(token, attempts);
		return false;
	}

	private void removeOldToken(String oldToken) {
		if (StringUtils.isNotBlank(oldToken)) {
			csrfSet.remove(oldToken);
			log.info("Old token {} has been removed", oldToken);
		}
	}

	private String createNewToken(String oldToken, String... nextMethodName) {
		if (nextMethodName == null || nextMethodName.length == 0) {
			return oldToken;
		}

		removeOldToken(oldToken);
		// создаю новый токен
		String token = UUID.randomUUID().toString();
		// сохраняю созданный токен и название метода зашифрованно

		String allowedMethods = String.join(",", nextMethodName);
		String encryptedToken = encryptors.encrypt(token + " " + allowedMethods);
		csrfSet.add(encryptedToken);
		return encryptedToken;
	}

	private String[] decryptToken(String token) {
		return encryptors.decrypt(token).split(" ");
	}


	private ResponseEntity<ApplicationAcceptedDTO> createApplicationUnconfirmed(String csrf,
																				AccountApplicationDTO dto,
																				ApplicationAcceptedDTO acceptedDTO)
			throws EmailDuplicateException {

		String phone = ClientValue.normalizePhone(dto.getContactInfo().getPhone());
		List<AccountApplication> unconfirmedApplications = accountApplicationRepository
				.findUnconfirmedByClientPhoneAndActiveIsTrueOrderByIdDesc(phone);

		if (unconfirmedApplications.isEmpty()) {
			log.warn("No AccountApplication for mobile phone number {} in state 'CONTACT_INFO_UNCONFIRMED'", phone);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		log.warn("Found {} AccountApplication for mobile phone number {} in state 'CONTACT_INFO_UNCONFIRMED'", unconfirmedApplications.size(), phone);

		AccountApplication accountApplication = unconfirmedApplications.get(0);
		accountApplication.setClientState(CONTACT_INFO_CONFIRMED);

		return applicationCreationFinalizing(csrf, dto, acceptedDTO, accountApplication);
	}

	private ResponseEntity<ApplicationAcceptedDTO> applicationCreationFinalizing(String csrf,
																				 AccountApplicationDTO dto,
																				 ApplicationAcceptedDTO acceptedDTO,
																				 AccountApplication accountApplication)
			throws EmailDuplicateException {

		accountApplication.getClient().editEmail(dto.getContactInfo().getEmail(), false, false);

		String phone = accountApplication.getClient().getPhone();
		String inn = accountApplication.getClient().getNumber();
		if (StringUtils.isBlank(phone) || StringUtils.isBlank(inn)) {
			log.error("Телефон {} или ИНН {} пустой на моменте финализации регистрации заявки #{}",
					phone,  // мало вероятно
					inn, // тоже слабо верится
					accountApplication.getId()
			);

			return err(ERROR_RESPONSE, "empty_fields", HttpStatus.BAD_REQUEST);
		}

		// Проверка на дубликаты
		if (isDuplicate(accountApplication)) {
			bus.publishEvent(new AccountApplicationDuplicateAttemptEvent(accountApplication.getId(), phone, inn));
			return err(ERROR_RESPONSE, "dub", HttpStatus.BAD_REQUEST);
		}


		if (!setTaxNumber(dto, acceptedDTO, accountApplication)) {
			repositoryWrapper.saveAccountApplication(accountApplication); // чтобы не потерять введённый e-mail
			return ApplicationAcceptedDTO.error(acceptedDTO).response(HttpStatus.BAD_REQUEST);
		}
		anticorruption.fillPerson(accountApplication.getPerson(), dto);

		try {
			City city = anticorruption.obtainCity(dto.getContactInfo().getCity());
			accountApplication.editCity(city);
		} catch (AnticorruptionException ex) {
			// В худшем случае будет неизвестный город, просто логируем
			log.info("Финальная стадия создания заявки для ИНН '{}' и номером телефона '{}'. Город с именем '{}' не найден.",
					dto.getContactInfo().getInnOrOgrn(),
					dto.getContactInfo().getPhone(),
					dto.getContactInfo().getCity());
		}

		accountApplication = repositoryWrapper.saveAccountApplication(accountApplication).getSecond();

		log.info("Финальная стадия создания заявки для ИНН '{}' и номером телефона '{}'. Дубликатов не обнаружено.",
				dto.getContactInfo().getInnOrOgrn(),
				dto.getContactInfo().getPhone());

		bus.publishEvent(new AccountApplicationEntityCreatedEvent(accountApplication));

		if (checkForFreshman(csrf, acceptedDTO, accountApplication)) {
			return ApplicationAcceptedDTO.ok(acceptedDTO).response(HttpStatus.OK);
		}
		accountApplication.setClientState(ClientStates.NEW_CLIENT);
		stateMachineService.setState(new StateSetterDTO(accountApplication.getId(), ClientEvents.CE_SMS));

		//далее следует костыльный блок ввиду того, что поведение SM не вполне то, что было ожидаемо, но результата нужно добиться быстро

		//проводим проверки
		Optional<StatusInfoDTO> checkResult = stateMachineService.setState(new StateSetterDTO(accountApplication.getId(), ClientEvents.CHECKS));
		//проверяем, что не произошел автоотказ
		boolean isNotDeclined = !checkResult
				.map(StatusInfoDTO::getState)
				.map(clientStates -> Objects.equals(clientStates, AUTO_DECLINED))
				.orElse(false)
				&&
				!accountApplication.isErrAutoDeclineOld();
		Optional<Boolean> success;
		if (isNotDeclined) {
			//если автоотказа не было, то делаем попытку переключить в "ожидание документов"
			log.info("Попытка перевести созданную из анкеты карточку clientId = {} в WAIT_FOR_DOCS, текущий старый статус {}, новый статус {}",
					accountApplication.getId(), accountApplication.getStatus().getValue().name(), accountApplication.getClientState().name());
			success = stateMachineService.setState(new StateSetterDTO(accountApplication.getId(), ClientEvents.CHECKS_DONE, "Проверки для анкеты проведены успешно"))
					.map(StatusInfoDTO::getState)
					.map(statusInfoDTO -> Objects.equals(statusInfoDTO, WAIT_FOR_DOCS));
		} else {
			stateMachineService.setState(new StateSetterDTO(accountApplication.getId(), ClientEvents.AUTO_DECLINE, "Проверки для анкеты были провалены"));
			success = Optional.of(false);
		}

		if (!success.isPresent() || !Objects.equals(success.get(), Boolean.TRUE)) {
			String errorMessage = String.format("Не удалось перевести созданную из анкеты карточку clientId = %s в " +
							"WAIT_FOR_DOCS, текущий старый статус %s, новый статус %s",
					String.valueOf(accountApplication.getId()), accountApplication.getStatus().getValue().name(), accountApplication.getClientState().name());
			log.error(errorMessage);
			if (accountApplication.getClientState().equals(AUTO_DECLINED)) {
				return err(DECLINE_RESPONSE, errorMessage, HttpStatus.OK);
			}
			return err(DEFAULT_RESPONSE, errorMessage, HttpStatus.OK);
		}

//		try {
//			accountApplication.tryMove(Status.RESERVING.val());
//		} catch (KonturFailException e) {
//			return err(e.getUserMessage(), e.getMessage(), HttpStatus.OK);
//		} catch (P550FailException e) {
//			return err(e.getUserMessage(), e.getMessage(), HttpStatus.OK);
//		} catch (RiskyOkvedException e) {
//			// не исключение, просто у человека есть рискованный оквэд и ему надо сказать,
//			// чтобы он прикрепил необходимые документы
//			acceptedDTO.setDeveloperPayload("EXTRA");
//			acceptedDTO.setMessage(e.getMessage());
//			accountApplication.addHistoryRecord("Найден минимум один рискованный ОКВЭД при резервировании счёта. Запросили необходимые дополнительные документы");
//		} catch (NeedToEnterSmsCodeException e) {
//			log.error("Попытка запроса смс. Механизм работает для перевода холодной заявки по статусам"); // не должно срабатывать
//			acceptedDTO.setDeveloperPayload("NEED_SMS_CODE");
//			acceptedDTO.setMessage("Введите смс код подтверждения");
//		} finally {
//			accountApplication = repositoryWrapper.saveAccountApplication(accountApplication).getSecond();
//		}
//
//		if (!accountApplication.getStatus().is(Status.RESERVING)) {
//			return err();
//		}
//		try {
//			accountApplication.tryMove(Status.NEW.val());
//		} catch (KonturFailException e) {
//			return err(e.getUserMessage(), e.getMessage(), HttpStatus.OK);
//		} catch (P550FailException e) {
//			return err(e.getUserMessage(), e.getMessage(), HttpStatus.OK);
//		} catch (RiskyOkvedException e) {
//			// не исключение, просто у человека есть рискованный оквэд и ему надо сказать,
//			// чтобы он прикрепил необходимые документы
//			acceptedDTO.setDeveloperPayload("EXTRA");
//			acceptedDTO.setMessage(e.getMessage());
//			accountApplication.addHistoryRecord("Найден минимум один рискованный ОКВЭД при переводе в статус Новая. Запросили необходимые дополнительные документы");
//		} catch (NeedToEnterSmsCodeException e) {
//			log.error("Попытка запроса смс. Механизм работает для перевода холодной заявки по статусам"); // не должно срабатывать
//			acceptedDTO.setDeveloperPayload("NEED_SMS_CODE");
//			acceptedDTO.setMessage("Введите смс код подтверждения");
//		} finally {
//			accountApplication = repositoryWrapper.saveAccountApplication(accountApplication).getSecond();
//		}
//		if (!accountApplication.getStatus().is(Status.NEW)) {
//			return err();
//		}

		return fillSuccessfulReply(csrf, acceptedDTO, accountApplication);
	}

	/**
	 * Проверяет, является ли новая заявка дубликатом. Если да, то деактивируется текущая
	 * Также, деактивируются все активные заявки в любом статусе без ИНН, но с тем же номером телефона
	 *
	 * @param accountApplication текущая заявка
	 * @return является ли текущая заявка дубликатом
	 */
	private boolean isDuplicate(AccountApplication accountApplication) {
		final String taxNumber = StringUtils.trim(accountApplication.getClient().getNumber());
		final String phone = StringUtils.trim(accountApplication.getClient().getPhone());

		deactivateApplicationsWithoutTaxNumberButSamePhoneNumber(phone, taxNumber);

		return deactivateApplicationsIfDuplicatesByInnExist(accountApplication, taxNumber);
	}

	private AccountApplication getLastOrCreateNewAccountApplication(@Nonnull ContactInfoVerifyDTO dto,
																	@Nonnull Source source,
																	@Nullable String forwardedForHeader) {
		City city = anticorruption.obtainCity(dto.getCity());
		ClientValue client = anticorruption.obtainClient(dto.getPhone());
		List<AccountApplication> applications = accountApplicationRepository.findUnconfirmedByClientPhoneAndActiveIsTrueOrderByIdDesc(client.getPhone());

		AccountApplication application;
		if (applications.size() > 0) {
			log.info("Для номера {} было найдено {} заявок в статусе 'Создана'", client.getPhone(), applications.size());
			// берем первую найденную
			application = applications.get(0);
			if (!Objects.equals(application.getCity().getId(), city.getId())){
				application.editCity(city);
			}
			applications.remove(0);
			log.info("Берём самую свежую #{}", application.getId());
			log.info("Остальные деактивируем");
			StringBuilder others = new StringBuilder();
			String delimiter = ", ";
			for (AccountApplication accountApplication : applications) {
				others
						.append(properties.getLkUrl())
						.append(accountApplication.getId())
						.append(delimiter);
				accountApplication.addHistoryRecord("Заявка помечена, как неактивная, так как заведена более свежая заявка в статусе 'Создана'");
				accountApplication.setActive(false);
			}

			if (StringUtils.isNotBlank(others.toString())) {
				application.addHistoryRecord("При создании заявки были найдены дубли с таким же номером телефона: "
						+ StringUtils.substringBeforeLast(others.toString(), delimiter));
				log.info("Деактивированы заявки {} так как они находились в статусе Создана, но начала создаваться заявка на такой же номер телефона {}", others, client.getPhone());
			}
		} else {
			// или создаем
			log.info("Для номера {} нет дубликатов. Создаём свежую заявку", client.getPhone());
			application = new AccountApplication(city, client, source);
		}
		application.setUtm(getUtm(dto));
		if (application.getUtm() != null) {
			bus.publishEvent(new SendUtmEvent(createFrom(application.getUtm()), application.getClientState().getRuName(), application.getId()));
		}

//отключена (возможно временно) функция определения города по ip ввиду невостребованности и при этом увеличения времени обработки
//https://context.atlassian.net/browse/APIKUB-2179
//		// Если есть любой заголовок с ip от пользователя, пробуем определить город.
//		if (StringUtils.isNotBlank(forwardedForHeader)) {
//			Optional<String> cityOrDefault = externalConnectors.getCityByIpDetector()
//					.getCityByIpOrDefault(forwardedForHeader);
//			// Аппендим новую строку к каждой попытке создать заявку для этого телефона
//			// Могут быть разные города
//			if (cityOrDefault.isPresent()) {
//				application.addHistoryRecord(
//						String.format("Для IP клиента '%s' определен город '%s'",
//								forwardedForHeader,
//								cityOrDefault.get()));
//			} else {
//				application.addHistoryRecord(
//						String.format("Не удалось определить город для IP клиента '%s'",
//								forwardedForHeader));
//			}
//		}

		return application;
	}

	private ResponseEntity<ApplicationAcceptedDTO> fillUnexpectedReply(String csrf,
																	   ApplicationAcceptedDTO acceptedDTO,
																	   Exception ex) {
		acceptedDTO.setMessage(DEFAULT_RESPONSE);
		acceptedDTO.setDeveloperPayload(ex.getMessage());
		removeOldToken(csrf);
		return ApplicationAcceptedDTO.error(acceptedDTO).response(HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<ApplicationAcceptedDTO> fillSuccessfulReply(String csrf,
																	   ApplicationAcceptedDTO acceptedDTO,
																	   AccountApplication application) {
		acceptedDTO.setLoginUrl(getAccountLink(properties.getLkUrl(), application.getLoginURL()));
		acceptedDTO.setRequisites(application.getAccount().getAccountNumber());
			acceptedDTO.setMessage(String.format("Заявка одобрена! Вам зарезервирован расчетный счет № %s."
							+ "<br />В ближайшее время Вам позвонит наш менеджер."
							+ "<br />Прикрепите, пожалуйста, в Личном кабинете документы, необходимые для открытия счета. Мы отправили на вашу почту _письмо со всеми подробностями!",
					application.getAccount().getAccountNumber()));
		removeOldToken(csrf);
		return ApplicationAcceptedDTO.ok(acceptedDTO).response(HttpStatus.OK);
	}

	private boolean checkForFreshman(String csrf,
									 ApplicationAcceptedDTO acceptedDTO,
									 AccountApplication application) {
		if (StringUtils.isNotBlank(application.clientInfoAndUpdate().errorText)) {
			application.addHistoryRecord("Нужно связаться с клиентом. Данная компания с ИНН " + application.getClient().getNumber() + " не найдена в реестре");
			application.getClient().editName(NOT_FOUND_IN_REGISTRY); // чтобы в списке на главной менеджерам было видно клиентов c ещё не появившимся ИНН
			application = repositoryWrapper.saveAccountApplication(application).getSecond();
			log.info("Создана заявка, в которой информация о компании по ИНН {} не была найдена в реестре", application.getClient().getNumber());

			acceptedDTO.setMessage(DEFAULT_RESPONSE);
			acceptedDTO.setDeveloperPayload("Нужно связаться с клиентом, данная компания не найдена в реестре");

			removeOldToken(csrf);
			return true;
		}
		return false;
	}

	private boolean setTaxNumber(AccountApplicationDTO dto,
								 ApplicationAcceptedDTO acceptedDTO,
								 AccountApplication application) {

		log.info("Присвоение заявке {} ИНН/ОГРН номера {}", application.getId(), dto.getContactInfo().getInnOrOgrn());
		if (!application.getClient().setInnOgrn(dto.getContactInfo().getInnOrOgrn())) {
			log.warn("Попытка присвоить заявке {} невалидного ИНН/ОГРН номера {}", application.getId(), dto.getContactInfo().getInnOrOgrn());

			acceptedDTO.setMessage(DEFAULT_RESPONSE);
			acceptedDTO.setDeveloperPayload("ИНН/ОГРН " + dto.getContactInfo().getInnOrOgrn() + " невалиден");
			return false;
		}
		log.info("Successfully set tax number {} for {} and phone {}", dto.getContactInfo().getInnOrOgrn(), application.getId(), dto.getContactInfo().getPhone());
		return true;
	}

	private Utm getUtm(ContactInfoVerifyDTO dto) {
		if (properties.getIsUtmActive() == 1 && dto.getUtm() != null) {
			return Utm.createFrom(dto.getUtm());
		}
		return null;
	}

	/**
	 * Деактривирование заявки во всех статусах с таким же номером телефона, но без ИНН.
	 * Вызывается только если есть заявка с таким номером телоефона и заполненным ИНН
	 */
	private void deactivateApplicationsWithoutTaxNumberButSamePhoneNumber(@NotNull String phone, @NotNull String taxNumber) {
		log.info("Деактривирование заявки с таким же номером телефона, но без ИНН '{}'", phone);

		List<AccountApplication> allByClientPhoneAndClientInnIsEmptyAndOgrnIsEmpty = accountApplicationRepository
				.findAllByClientPhoneAndClientInnIsEmptyAndOgrnIsEmptyAndActiveIsTrue(ClientValue.normalizePhone(phone));

		for (AccountApplication accountApplication : allByClientPhoneAndClientInnIsEmptyAndOgrnIsEmpty) {
			accountApplication.addHistoryRecord(String.format("Заявка деактивирована так как у заявки нет ИНН, но на текущий номер телефона заведена другая заявка с ИНН %s", taxNumber));
			accountApplication.setActive(false);
			log.info("Заявка {} без ИНН деактивирована", accountApplication.getId());
			repositoryWrapper.saveAccountApplication(accountApplication);
		}
	}

	private boolean deactivateApplicationsIfDuplicatesByInnExist(@NotNull AccountApplication application, @NotNull String inn) {
		List<AccountApplication> byClientInnAndActive = accountApplicationRepository.findAllByClientTaxNumberSameAsOgrnAndActiveIsTrue(inn);
		if (byClientInnAndActive.size() > 1) {
			log.info("Деактривирование заявки с таким же ИНН '{}'", inn);
			// свежая заявка
			application.setActive(false);

			StringBuilder others = new StringBuilder();
			String delimiter = ", ";
			for (AccountApplication accountApplication : byClientInnAndActive) {
				others
						.append(properties.getLkUrl())
						.append(accountApplication.getId())
						.append(delimiter);
			}
			if (StringUtils.isNotBlank(others.toString())) {
				application.addHistoryRecord("Заявка помечена, как неактивная, так как есть минимум одна заявка с таким же ИНН: "
						+ StringUtils.substringBeforeLast(others.toString(), delimiter));
			}

			bus.publishEvent(new DeactivateClientEvent(application, others.toString()));
			repositoryWrapper.saveAccountApplication(application);
			return true;
		}
		return false;
	}

	private ResponseEntity<ApplicationAcceptedDTO> err() {
		ApplicationAcceptedDTO acceptedDTO = new ApplicationAcceptedDTO();
		acceptedDTO.setMessage(ERROR_RESPONSE_CALL);
		acceptedDTO.setDeveloperPayload("Не удалось автоматически зарезервировать заявку.");
		return ApplicationAcceptedDTO.error(acceptedDTO).response(HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<ApplicationAcceptedDTO> err(String message, String developerPayload, HttpStatus httpStatus) {
		ApplicationAcceptedDTO acceptedDTO = new ApplicationAcceptedDTO();
		acceptedDTO.setMessage(message);
		acceptedDTO.setDeveloperPayload(developerPayload);
		return ApplicationAcceptedDTO.error(acceptedDTO).response(httpStatus);
	}
}
