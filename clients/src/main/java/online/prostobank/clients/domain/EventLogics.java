package online.prostobank.clients.domain;

import club.apibank.connectors.analytics.event.impl.EventBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.anketa.UtmDTO;
import online.prostobank.clients.config.properties.DboProperties;
import online.prostobank.clients.config.properties.EventLogicsProperties;
import online.prostobank.clients.connectors.ExternalConnectors;
import online.prostobank.clients.connectors.email.EmarsysEvents;
import online.prostobank.clients.domain.enums.Event;
import online.prostobank.clients.domain.events.*;
import online.prostobank.clients.domain.repository.EmarsysSentStatusRepository;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statistics.StatisticsRepository;
import online.prostobank.clients.domain.statuses.ApplicationEmarsysStatus;
import online.prostobank.clients.domain.statuses.KycSystemForDeclineCode;
import online.prostobank.clients.services.PdfGenerator;
import online.prostobank.clients.services.ai.LoadAiScoreService;
import online.prostobank.clients.services.dbo.model.DboRequestCreateUserDto;
import online.prostobank.clients.services.dbo.service.DboService;
import online.prostobank.clients.utils.Utils;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static online.prostobank.clients.connectors.email.EmarsysEvents.MESSAGE_TO_CLIENT_EVENT;
import static online.prostobank.clients.domain.OkvedConstants.REALTOR_OKVEDS;
import static online.prostobank.clients.domain.OkvedConstants.TRAFFIC_OKVEDS;
import static online.prostobank.clients.domain.enums.EventName.KEYCLOAK_NAME;
import static online.prostobank.clients.domain.type.ParamsKeys.*;
import static online.prostobank.clients.utils.Utils.*;

@JsonLogger
@Slf4j
@RequiredArgsConstructor
@Service
public class EventLogics {

	private final EventLogicsProperties                          config;
    private final JmsTemplate                                    sender;
    private final PdfGenerator                                   pdfGenerator;
    private final ExternalConnectors 						     externalConnectors;
    private final EmarsysSentStatusRepository                    sentStatusRepository;
    private final TemplateEngine                                 tt;
    private final StatisticsRepository                           statsRepo;
	private final DboService                                     dboService;
    private final DboProperties                                  dboProperties;
    private final LoadAiScoreService                             aiScoreService;

	private <T> void sendEvent(@NonNull Event event,
							   @Nonnull T data){
		sender.convertAndSend(event.getName(), data);
		try{
			switch (event){
				case SMS: {
					Sms m = (Sms) data;
					String text = "Отправка СМС на номер " + m.phoneNumber + ", текст " + m.text;
					log.info(text);
					break;
				}
				case EMARSYS_CONTACT: {
					String text = "Отправка email " + data.toString();
					log.info(text);
					break;
				}
				case SYSTEM_EMAIL: {
					log.info("Отправка системного email " + data.toString());
				}
			}

		} catch (IllegalStateException ise) {
			log.warn(ise.getLocalizedMessage(), ise);
		} catch (ClassCastException e){
			log.warn("Ошибка при касте объекта в объект класса EventLogics.Sms", e);
		}
	}


	private void setContactValueAndSendTrigger(Email m) {
		sendEvent(Event.EMARSYS_CONTACT, m);
		sendEvent(Event.EMARSYS_TRIGGER, m);
	}


	@EventListener
	public void handleApplicationCreating(ApplicationCreator event){
		log.info("ApplicationCreator event");
		sendEvent(Event.CREATE, event.getClientId());
	}

	@EventListener
	public void handleApplicationChanging(ApplicationChanger event){
		log.info("ApplicationChanging event");
		sendEvent(Event.CHANGE_DATA, event.getClientId());
	}

	@EventListener
	public void handleDocumentNotifier(ApplicationDocumentNotifier event){
		log.info("ApplicationDocumentNotifier event");
		sendEvent(Event.DOCS_IS_DONE, event.getClientId());
	}

	/**
	 * Входящее событие о том, что была создана новая заявка (на успех)
	 */
	@EventListener
	public void handleAccAppCreated(AccountApplicationEntityCreatedEvent event) {
		log.info("Application created event");

		AccountApplication application = event.getAccountApplication();


		Map<String, Object> paramz = createBaseEmailParameters(application);
		//Важдый момент - создание контакта в emarsys, без контакта не работает функционал триггеров, рассылки почти и т.п.
		sendEvent(Event.EMARSYS_CONTACT, new Email(Email.EmailId.WELCOME, application.getId(),
				application.getClient().getEmail(),
				paramz));
	}

	/**
	 * Запрос кода подтверждения
	 */
	@EventListener
	public void onNewConfirmationCode(ConfirmationCodeGeneratedEvent event) {
		AccountApplication aa = event.getAccountApplication();

		Map<String, Object> paramz = new HashMap<>();
		paramz.put(CONFIRMATION_CODE.getKey(), aa.getConfirmationCode());
		paramz.put(EMAIL.getKey(), aa.getClient().getEmail());
		paramz.put(PHONE.getKey(), aa.getClient().getPhone());
		paramz.put(CRM_ID.getKey(), aa.getId());

		Context ctx = new Context();
		ctx.setVariables(paramz);

		if (config.getDevConfirmationCodeSwitch() == 0) {
			sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), tt.process("confirmation-code.sms", ctx)));
		} else {
			log.info("Default developer confirmation code is: {}", config.getDevConfirmationCode());
		}
	}

	/**
	 * Заявка перешла в резервирование
	 */
	@EventListener
	public void onReserving(AccountApplicationReservedEvent event) {
		log.info("AccountApplicationReservedEvent event");
		log.info("Резервирование заявки {}", event.getClientId());
		externalConnectors.getIAnalytics().send(
				EventBuilder.withType(config.getEventPrefix() + "Account reserved")
						.userId(event.getPhone())
						.build());
	}

	/**
	 * Отказ клиента
	 */
	@EventListener
	// todo по логике подходит?
	public void onDeclinedByClient(AccountApplicationDeclineByClientEvent event) {
		log.info("AccountApplicationDeclineByClientEvent event");
		AccountApplication accountApplication = event.getApp();
		Long clientId = accountApplication.getId();
		String phone = accountApplication.getClient().getPhone();
		String email = accountApplication.getClient().getEmail();
		String oldState = event.getOldState();

		Map<String, Object> params = new HashMap<>();
		params.put(CRM_ID.getKey(), clientId);
		params.put(PHONE.getKey(), phone);
		params.put(CLIENT_DENIED_COMMENT.getKey(), oldState);

		sendEvent(
				Event.EMARSYS_CONTACT,
				new Email(
						Email.EmailId.ERR_CLIENT_DECLINE,
						clientId,
						email,
						params
				)
		);
	}

	/**
	 * Недозвон
	 */
	@EventListener
	// todo по логике подходит?
	public void onNoAnswer(AccountApplicationClientNoAnswerEvent event) {
		log.info("AccountApplicationClientNoAnswerEvent event");
		AccountApplication accountApplication = event.getAccountApplication();
		Long clientId = accountApplication.getId();
		String phone = accountApplication.getClient().getPhone();
		String email = accountApplication.getClient().getEmail();

		Map<String, Object> params = new HashMap<>();
		params.put(CRM_ID.getKey(), clientId);
		params.put(PHONE.getKey(), phone);

		sendEvent(
				Event.EMARSYS_CONTACT,
				new Email(
						Email.EmailId.NO_ANSWER,
						clientId,
						email,
						params
				)
		);
	}

	/**
	 * Ожидание документов
	 */
	@EventListener
	// todo по логике подходит?
	public void onWaitForDocs(AccountApplicationWaitForDocsEvent event) {
		log.info("AccountApplicationWaitForDocsEvent event");
		AccountApplication accountApplication = event.getAccountApplication();
		Long clientId = accountApplication.getId();
		String phone = accountApplication.getClient().getPhone();
		String email = accountApplication.getClient().getEmail();

		Map<String, Object> params = new HashMap<>();
		params.put(CRM_ID.getKey(), clientId);
		params.put(PHONE.getKey(), phone);

		sendEvent(
				Event.EMARSYS_CONTACT,
				new Email(
						Email.EmailId.WAIT_FOR_DOCS,
						clientId,
						email,
						params
				)
		);

		// Дополнительное требование отправлять отбивку на ответственного менеджера
		informFrontManagers(TITLE_ON_WAIT_FOR_DOCS,
				String.format("Каких-то документов не хватает. Номер заявки - %d (%s%d), телефон клиента %s",
						clientId, config.getAppUrl(), clientId, phone));

		externalConnectors.getIAnalytics()
				.send(
						EventBuilder.withType(config.getEventPrefix() + "Await for docs")
								.userId(phone)
								.build()
				);
	}

	/**
	 * Попытка повторно завести заявку на ИНН, на который уже была заведена заявка
	 */
	@EventListener
	public void onDuplicateAttempt(AccountApplicationDuplicateAttemptEvent event) {
		informFrontManagers(TITLE_DUPLICATE_MANAGER,
				String.format("Данный клиент уже подавал заявку на оформление РКО (телефон %s, ИНН %s)",
						event.getPhone(),
						event.getInn()
				));
	}

	private void informFrontManagers(String title, String body) {
		informManagers(config.getEmailFront(), title, body);
	}

	private void informBackManagers(String title, String body) {
		informManagers(config.getEmailBack(), title, body);
	}

	private void informTechSupport(String title, String body) {
		informManagers(config.getEmailDebugger(), title, body);
	}

	private void informManagers(String emailAddresses, String title, String body) {
		if (StringUtils.isNotBlank(emailAddresses)) {
			for (String email : emailAddresses.split(",")) {
				sendEvent(Event.SYSTEM_EMAIL,
						new SystemEmail(email.trim(), title,
								body)
				);
			}
		}
	}


	/**
	 * Event для отправки уведомления в емарсис о том, что документы пользователя успешно добавлены через почту
	 */
	@EventListener
	public void onApplicationAttachmentSuccess(ApplicationAttachmentSuccessEvent event) {
		log.info("ApplicationAttachmentSuccessEvent event");


		AccountApplication aa = event.getAccountApplication();
		ClientValue client = aa.getClient();
		Map<String, Object> paramz = new HashMap<>();
        paramz.put(CRM_ID.getKey()         , aa.getId());
        paramz.put(EMAIL.getKey()          , client.getEmail());
        paramz.put(PHONE.getKey()          , client.getPhone());
        paramz.put(CITY.getKey()           , aa.getCity().getName());
        paramz.put(FIRST_NAME.getKey()     , client.isSP() ? client.getFirstName() : "");
        paramz.put(SECOND_NAME.getKey()    , client.isSP() ? client.getSecondName() : "");
        paramz.put(ACCOUNT_NUMBER.getKey() , aa.getAccount().getAccountNumber());
        paramz.put(ACCOUNT_LINK.getKey()   , Utils.getAccountLink(config.getLkUrl(), aa.getLoginURL()));
        paramz.put(COMPANY_NAME.getKey()   , client.getName());
        paramz.put(ORIGIN_SOURCE.getKey()  , aa.getSource());
        paramz.put(INN.getKey()            , client.getNumber());

		Email.EmailId specifiedAccountReserved;

		if (hasRiskyOkved(TRAFFIC_OKVEDS,
				client.getPrimaryCodes(), client.getSecondaryCodes(),
				client.getRiskyCodes(), client.getBlackListedCodes())) {
			// для емарсиса нужны два разные статуса для тех, у кого есть и нет перевозок
			specifiedAccountReserved = Email.EmailId.ATTACHMENT_DOCUMENTS_NEW;
		} else {
			specifiedAccountReserved = Email.EmailId.ATTACHMENT_DOCUMENTS_NEW_T;
		}

		setContactValueAndSendTrigger(new Email(specifiedAccountReserved, aa.getId(),
				client.getEmail(),
				paramz));

		aa.addHistoryRecord(String.format("Добавление документов через email пользователя %s прошло успешно", client.getEmail()));

		// Дополнительное требование отправлять отбивку на ответственного менеджера
		informFrontManagers(TITLE + " " + aa.getId(), "Добавлены документы. Номер заявки - "
				+ aa.getId() + " ("
				+ config.getAppUrl() + aa.getId()
				+ "), телефон клиента " + client.getPhone());

		externalConnectors.getIAnalytics().send(
				EventBuilder.withType(config.getEventPrefix() + "Docs attached")
						.userId(client.getPhone())
						.addProperty("sender", "client")
						.addProperty("source", "mail")
						.build());
	}

	/**
	 * Загрузка документа из редактора заявки или ЛК
	 */
	@EventListener
	public void onDocumentDownloaded(DocumentDownloaded event) {
		log.info("DocumentDownloaded event");

		AccountApplication aa = event.getAccountApplication();
		String sender = event.isDownloadedByManager() ? "manager" : "client";
		String source = event.isDownloadedByManager() ? "editor" : "lk";
		externalConnectors.getIAnalytics().send(
				EventBuilder.withType(config.getEventPrefix() + "Docs attached")
						.userId(aa.getClient().getPhone())
						.addProperty("document_type", event.getType().getFrontendKey())
						.addProperty("sender", sender)
						.addProperty("source", source)
						.build());
	}

	/**
	 * Счет зарезервирован, заявка перешла в состояние новой
	 */
	@EventListener
	public void onApplicationIsNew(ApplicationIsNewEvent event) {
		log.info("ApplicationIsNewEvent event");

		aiScoreService.startCalculateAiScoring();

		AccountApplication aa = event.getAccountApplication();
		ClientValue client = aa.getClient();

		Map<String, Object> paramz = createBaseEmailParameters(aa);

		Email.EmailId specifiedAccountReserved;
		String type;
		if (hasRiskyOkved(TRAFFIC_OKVEDS,
				client.getPrimaryCodes(), client.getSecondaryCodes(),
				client.getRiskyCodes(), client.getBlackListedCodes())) {
			// для емарсиса нужны два разные статуса для тех, у кого есть и нет перевозок
			specifiedAccountReserved = Email.EmailId.ACCOUNT_RESERVED_FOR_TRANSPORTATION;
			type = "с грузоперевозками (2.9b)";
		} else if (hasRiskyOkved(REALTOR_OKVEDS,
				client.getPrimaryCodes(), client.getSecondaryCodes(),
				client.getRiskyCodes(), client.getBlackListedCodes())) {
			paramz.put(EVENT_ID.getKey(), EmarsysEvents.ACCOUNT_RESERVED_FOR_REALTORS.getEmarsysEventNumber());
			specifiedAccountReserved = Email.EmailId.ACCOUNT_RESERVED_FOR_REALTORS;
			type = "с риэлторским оквэд";
		} else {
			specifiedAccountReserved = Email.EmailId.ACCOUNT_RESERVED;
			type = "без грузоперевозок (2.13)";
		}

		String smsText = prepareSms(specifiedAccountReserved, paramz);
		sendEvent(Event.SMS, new Sms(client.getPhone(), smsText));
		aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", client.getPhone(), smsText));

		addingAdditionalFields(aa, paramz);

		sendEvent(Event.EMARSYS_CONTACT, new Email(specifiedAccountReserved, aa.getId(),
				aa.getClient().getEmail(),
				paramz));

		aa.addHistoryRecord(String.format("Отправка письма на адрес %s о создании новой заявки %s", aa.getClient().getEmail(), type));

		// сложный механизм емарсиса. Ждём ответа на запрос - потом отправляем запрос на отправку письма
		CompletableFuture.supplyAsync(getApplicationEmarsysStatusSupplier(aa))
				.thenAccept(getApplicationEmarsysStatusConsumer(aa, paramz, specifiedAccountReserved)).join();
	}

	private Consumer<ApplicationEmarsysStatus> getApplicationEmarsysStatusConsumer(AccountApplication aa, Map<String, Object> paramz, Email.EmailId specifiedAccountReserved) {
		return emarsysStatus -> {
			log.info("EMARSYS STATUS ON CREATION LEAD : {}, applicationId: {}", emarsysStatus, aa.getId());
			if (!emarsysStatus.equals(ApplicationEmarsysStatus.NONE)) {
				ClientValue client = aa.getClient();
				Optional.ofNullable(paramz.get(EVENT_ID.getKey()))
						.ifPresent(o -> sendEvent(Event.EMARSYS_TRIGGER, new Email(specifiedAccountReserved, aa.getId(), client.getEmail(), paramz)));
				paramz.put(EVENT_ID.getKey(), EmarsysEvents.TO_NEW_OR_NEW_T_WITH_LOADING.getEmarsysEventNumber());
				sendEvent(Event.EMARSYS_TRIGGER, new Email(specifiedAccountReserved, aa.getId(), client.getEmail(), paramz));

				// Дополнительное требование отправлять отбивку на ответственного менеджера
				informFrontManagers(TITLE + " " + aa.getId(), "Создана новая заявка. Номер заявки - "
						+ aa.getId() + " ("
						+ config.getAppUrl() + aa.getId()
						+ "), телефон клиента " + aa.getClient().getPhone());
			} else {
				log.warn("Что-то произошло не так при отправке триггера. " +
						"Статус в емарсисе: "+ emarsysStatus + " " +
						"id заявки -  " + aa.getId());
			}
		};
	}

	private Supplier<ApplicationEmarsysStatus> getApplicationEmarsysStatusSupplier(AccountApplication aa) {
		return () ->{
			ApplicationEmarsysStatus applicationEmarsysStatus;
			final int MAX_COUNT = 10;
			int countOfTry = 0;
			do {
				Optional<EmarsysSentStatus> optionalEmarsysSentStatus = sentStatusRepository.findByAccountApplication(aa.getId());
					applicationEmarsysStatus = optionalEmarsysSentStatus
							.map(EmarsysSentStatus::getEmarsysStatus)
							.orElse(ApplicationEmarsysStatus.NONE);

				countOfTry++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.warn("SOME WENT WRONG IN FUTURE, DETECTION EMARSYS STATUS: {}", e.getMessage());
				}
			}
			while (applicationEmarsysStatus.equals(ApplicationEmarsysStatus.NONE) && countOfTry < MAX_COUNT);
			log.info("EMARSYS STATUS IS {}", applicationEmarsysStatus);
			return applicationEmarsysStatus;
		};
	}

    private boolean hasRiskyOkved(List<String> riskyOkveds, String... clientCodes) {
        if (Arrays.stream(clientCodes)
                .allMatch(StringUtils::isBlank)) return false;
        return Arrays.stream(clientCodes)
                .flatMap(code -> Arrays.stream(code.split(COMMA_DELIMITER)))
                .anyMatch(code -> riskyOkveds.stream().anyMatch(code::startsWith));
    }

	@EventListener
	// todo по логике подходит?
	public void onApplicationIsClosed(ApplicationCloseEvent event) {
		log.info("ApplicationCloseEvent event");


		AccountApplication aa = event.getAccountApplication();
		Map<String, Object> paramz = new HashMap<>();
		fillBaseParams(aa, paramz);
		addingAdditionalFields(aa, paramz);

		Email.EmailId specifiedAccountReserved = Email.EmailId.CLOSED;

		sendEvent(Event.EMARSYS_CONTACT, new Email(specifiedAccountReserved, aa.getId(),
				aa.getClient().getEmail(),
				paramz));
	}

	/**
	 * Деактивация заявки в Емарсисе
	 */
	@EventListener
	public void deactivateApplicationInEmarsys(DeactivateClientEvent event) {
		log.info("DeactivateClientEvent event. Деактивирование заявки {} в емарсисе", event.getAccountApplication().getId());

		AccountApplication accountApplication = event.getAccountApplication();

		Map<String, Object> paramz = new HashMap<>();
		paramz.put(CRM_ID.getKey(), accountApplication.getId());
		paramz.put(PHONE.getKey(), accountApplication.getClient().getPhone());
		paramz.put(ACCEPTANCE.getKey(), "2");

		setContactValueAndSendTrigger(new Email(Email.EmailId.DELETE, accountApplication.getId(),
				accountApplication.getClient().getEmail(),
				paramz));

		accountApplication.addHistoryRecord(String.format("Отправка тригерного письма на адрес %s", accountApplication.getClient().getEmail()));

		informBackManagers(TITLE_DUPLICATE + " " + accountApplication.getId(), "Заявка помечена как дубль. Номер заявки - "
				+ accountApplication.getId() + " ("
				+ config.getAppUrl() + accountApplication.getId()
				+ "), телефон клиента " + accountApplication.getClient().getPhone()
				+ "\n  Другие заявки с таким ИНН " + event.getOthers());
	}

	/**
	 * Генерация текста СМС по месту
	 */
	private String prepareSms(Email.EmailId e, Map<String, Object> paramz) {
		Context ctx = new Context();
		ctx.setVariables(paramz);
		return tt.process(e.getTheme()+ ".sms", ctx);
	}

	@EventListener
	public void sendUtm(SendUtmEvent event) {
		UtmDTO utm = event.getUtm();

		if (utm != null) {
			String url = config.getGoogleAnalyticsUrl()
					+ "?v=" + config.getGoogleAnalyticsVersion()
					+ "&tid=" + config.getGoogleAnalyticsId()
					+ "&cid=" + event.getAppId()
					+ "&t=" + config.getGoogleAnalyticsType()
					+ "&ec=" + config.getGoogleAnalyticsEventCategory()
					+ "&ea=" + event.getClientState()
					+ "&cs=" + utm.getSource() + "_" + utm.getMedium()
					+ "&cm=" + utm.getCampaign() + "_" + utm.getContent()
					+ "&ck=" + utm.getTerm();

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));

			HttpEntity http = new HttpEntity<>(null, headers);

			RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

			ResponseEntity responseEntity;
			try {
				responseEntity = restTemplate.exchange(url, HttpMethod.POST, http, String.class);
				log.debug("Обмен с google: {}", responseEntity.getStatusCodeValue());
			} catch (HttpStatusCodeException e) {
				log.error("Ошибка обмена с google: {}, ответ вернулся: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
			}
		}
	}

	@EventListener
	public void handleInformClient(@Nonnull InformClientEvent event) {
		AccountApplication application = event.getAccountApplication();
		ClientStates clientState = application.getClientState();
		switch (clientState) {
			case MANAGER_PROCESSING:
				handleInformNewStatus(application, clientState.getRuName());
				break;
			case ACTIVE_CLIENT: {
				handleInformFulfilledStatus(application, clientState.getRuName());
				break;
			}
			case WAIT_FOR_DOCS: {
				handleInformWaitForDocsStatus(application, clientState.getRuName());
				break;
			}
			case AUTO_DECLINED: {
				handleInformErrAutoDeclineStatus(application);
				break;
			}
		}
	}

	private void handleInformErrAutoDeclineStatus(AccountApplication aa) {
		Optional.ofNullable(aa.getStatus().getSubcode())
				.flatMap(subcode -> Optional.ofNullable(KycSystemForDeclineCode.getByKycCode(subcode)))
				.map(byKycCode -> {
					switch (byKycCode) {
						case P550: {
							return Email.EmailId.P550;
						}
						case KONTUR_KYC: {
							return Email.EmailId.KYC;
						}
						case KONTUR_SCORING: {
							return Email.EmailId.SCORING;
						}
						case OKVED: {
							return Email.EmailId.OKVEDS;
						}
						default:
							throw new IllegalStateException("Unexpected value: " + byKycCode);
					}
				})
				.ifPresent(emailId -> handleInform(aa, emailId));
	}

	private void handleInformWaitForDocsStatus(AccountApplication aa, String status) {
		log.info("Sending trigger information for status: " + status);
		handleInform(aa, getEmailId(aa.getClient()));
	}

	private void handleInformFulfilledStatus(AccountApplication aa, String status) {
		log.info("Sending trigger information for status: " + status);

		Map<String, Object> params = createParams(aa);
		addingAdditionalFields(aa, params);
		handleInform(aa, params, Email.EmailId.FULFILLED_MANUAL_NOTIFY);
	}

	private void handleInformNewStatus(AccountApplication aa, String status) {
		log.info("Sending trigger information for status: " + status);
		ClientValue client = aa.getClient();

		Map<String, Object> params = createBaseEmailParameters(aa);
		addingAdditionalFields(aa, params);
		handleInform(aa, params, getEmailId(client));

		// Дополнительное требование отправлять отбивку на ответственного менеджера
		informFrontManagers(
				TITLE + " " + aa.getId(),
				String.format(
						"Создана новая заявка. Номер заявки - %d (%s), телефон клиента %s",
						aa.getId(), config.getAppUrl() + aa.getId(), client.getPhone()
				)
		);
	}

	private Email.EmailId getEmailId(ClientValue client) {
		// для емарсиса нужны два разные статуса для тех, у кого есть и нет перевозок
		return hasRiskyOkved(
				TRAFFIC_OKVEDS,
				client.getPrimaryCodes(), client.getSecondaryCodes(),
				client.getRiskyCodes(), client.getBlackListedCodes()
		)
				? Email.EmailId.ACCOUNT_RESERVED_FOR_TRANSPORTATION
				: Email.EmailId.ACCOUNT_RESERVED;
	}

	private void handleInform(AccountApplication aa, Email.EmailId emailId) {
		handleInform(aa, createParamsForTriggerEvents(aa), emailId);
	}

	private void handleInform(AccountApplication aa, Map<String, Object> params, Email.EmailId emailId) {
		log.info("Sending trigger information for :: {}", emailId.name());
		String email = aa.getClient().getEmail();
		sendEvent(Event.EMARSYS_TRIGGER,
				new Email(
						emailId,
						aa.getId(),
						email,
						params
				)
		);
		aa.addHistoryRecord(String.format("Ручная отправка письма на адрес %s", email));
	}

	@EventListener
	public void handleKonturGetScoringError(KonturGetScorringErrorEvent event) {
		log.info("Get kontur scoring info error event");
		AccountApplication accountApplication = event.getAccountApplication();

		informTechSupport("Ошибка получения скоринга для заявки с инн/огрн " + accountApplication.getClient().getNumber(),
				"Возникла проблема с получением скоринга для заявки с инн/огрн "
						+ accountApplication.getClient().getNumber()
						+ " " + config.getAppUrl() + accountApplication.getId()
						+ "\n\nТекст ошибки: '" + event.getException() + "'");
	}

	@EventListener
	public void handleKonturGetInfoError(KonturLoadInfoErrorEvent event) {
		log.info("Load kontur info error event");
		informTechSupport("Ошибка получения информации по инн/огрн " + event.getSource(),
				"Возникла проблема с получением информации по инн/огрн "
						+ event.getSource()
						+ "\n\nТекст ошибки: '" + event.getErrorText() + "'");
	}

	/**
	 * Отказ
	 */
	@EventListener
	public void handleDeclined(ChecksDeclined event) {
		log.info("ChecksDeclined event {}", event.getReason());
		AccountApplication aa = event.getAccountApplication();

		HashMap<String, Object> ctxMap = new HashMap<>();
		ctxMap.put(CRM_ID.getKey(), aa.getId());
		ctxMap.put(PHONE.getKey(), aa.getClient().getPhone());
		String smsText;

		switch (event.getReason()) {
			case ARREST:
				fillParams(aa, ctxMap);
				smsText = prepareSms(Email.EmailId.ARREST_DECLINE, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));
				setContactValueAndSendTrigger(new Email(Email.EmailId.ARREST_DECLINE, aa.getId(), aa.getClient().getEmail(), ctxMap));
				aa.addHistoryRecord(String.format("Отправка письма на адрес %s об отказе по причине наличия арестов", aa.getClient().getEmail()));
				break;
			case PASSPORT:
				ctxMap.put(ORIGIN_SOURCE.getKey(), aa.getSource());
				smsText = prepareSms(Email.EmailId.PASSPORT, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));
				sendEvent(Event.EMARSYS_CONTACT, new Email(Email.EmailId.PASSPORT, aa.getId(), aa.getClient().getEmail(), ctxMap));
				break;
			case P550:
				fillParams(aa, ctxMap);
				smsText = prepareSms(Email.EmailId.P550, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));
				setContactValueAndSendTrigger(new Email(Email.EmailId.P550, aa.getId(), aa.getClient().getEmail(), ctxMap));
				aa.addHistoryRecord(String.format("Отправка письма на адрес %s об отказе по 550-П (2.4а)", aa.getClient().getEmail()));
				break;
			case SECURITY:
				smsText = prepareSms(Email.EmailId.ERR_SECURITY_DECLINE, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));
				setContactValueAndSendTrigger(new Email(Email.EmailId.ERR_SECURITY_DECLINE, aa.getId(), aa.getClient().getEmail(), ctxMap));
				aa.addHistoryRecord(String.format("Отправка письма на адрес %s об отказе СБ (2.4а)", aa.getClient().getEmail()));

				break;
			case FNS:
				ctxMap.put(ORIGIN_SOURCE.getKey(), aa.getSource());
				smsText = prepareSms(Email.EmailId.FNS, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));

				sendEvent(Event.EMARSYS_CONTACT, new Email(Email.EmailId.FNS, aa.getId(), aa.getClient().getEmail(), ctxMap));

				break;
			case BANKRUPCY:
				ctxMap.put(ORIGIN_SOURCE.getKey(), aa.getSource());
				smsText = prepareSms(Email.EmailId.BANKRUPCY, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));

				sendEvent(Event.EMARSYS_CONTACT, new Email(Email.EmailId.BANKRUPCY, aa.getId(), aa.getClient().getEmail(), ctxMap));

				break;
			case OKVEDS: {
				fillParams(aa, ctxMap);
				smsText = prepareSms(Email.EmailId.OKVEDS, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));
				setContactValueAndSendTrigger(new Email(Email.EmailId.OKVEDS, aa.getId(), aa.getClient().getEmail(), ctxMap));
				aa.addHistoryRecord(String.format("Отправка письма на адрес %s об отказе по ОКВЭД (2.10.1)", aa.getClient().getEmail()));

				createPdfOnApplicationDeclinedWithOkved(event.getAccountApplication());
				break;
			}
			case KYC:
				fillParams(aa, ctxMap);
				smsText = prepareSms(Email.EmailId.KYC, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));
				setContactValueAndSendTrigger(new Email(Email.EmailId.KYC, aa.getId(), aa.getClient().getEmail(), ctxMap));

				createPdfOnApplicationDeclinedWithFailedCompanyFeature(event.getAccountApplication());
				break;
			case SCORING:
				fillParams(aa, ctxMap);
				smsText = prepareSms(Email.EmailId.SCORING, ctxMap);
				sendEvent(Event.SMS, new Sms(aa.getClient().getPhone(), smsText));
				aa.addHistoryRecord(String.format("Отправка смс на номер %s: %s", aa.getClient().getPhone(), smsText));
				setContactValueAndSendTrigger(new Email(Email.EmailId.SCORING, aa.getId(), aa.getClient().getEmail(), ctxMap));

				createPdfOnApplicationDeclinedWithCommonScoringFailed(event.getAccountApplication());
				break;
		}
	}

	private static final String TITLE_ERR = "Просто|Банк - Ошибка в заявке";
	private static final String TITLE = "Просто|Банк - Новая заявка";
	private static final String TITLE_NEW_START = "Просто|Банк - Начинает создаваться новая заявка";
	private static final String TITLE_COLD_NEW_START = "Просто|Банк - Создана новая холодная заявка";
	private static final String TITLE_CONTACT_INFO_CONFIRMED = "Просто|Банк - Новая заявка на рассмотрении";
	private static final String TITLE_RESERVING = "Просто|Банк - Новая заявка зарезервирована";
	private static final String TITLE_ON_SIGNING = "Просто|Банк - Смена статуса: На подписание";
	private static final String TITLE_ON_WAIT_FOR_DOCS = "Просто|Банк - Смена статуса: Ожидание документов";
	private static final String TITLE_MANAGER_PROCESSING = "Просто|Банк - Смена статуса: в работе менеджера";
	private static final String TITLE_GO_OPEN = "Просто|Банк - Смена статуса: На открытие";
	private static final String TITLE_DUPLICATE = "Просто|Банк - Дубль";
	private static final String TITLE_DUPLICATE_MANAGER = "Повторная подача на оформление РКО";

	private static final String TITLE_CANCELLATION = "Выезд к клиенту совершать не нужно";


	/**
	 * Оповещение сотрудников о смене сим-карты пользователем
	 */
	@EventListener
	public void handleIMSINotification(IMSINotificationEvent event) {
		log.info("IMSINotificationEvent event");
		for (String imsiNotificationRecipientUser : event.getImsiNotification().getImsiNotificationRecipientUser()) {
			for (ImsiNotificationPhoneNumberOwner imsiNotificationPhoneNumberOwner : event.getImsiNotification().getImsiNotificationPhoneNumberOwners()) {
				log.info("Sending message to {} about client with phone: '{}' and inn '{}'",
						imsiNotificationRecipientUser,
						imsiNotificationPhoneNumberOwner.getPhoneNumber(),
						imsiNotificationPhoneNumberOwner.getTaxNumber());
				sendEvent(Event.SYSTEM_EMAIL, new SystemEmail(imsiNotificationRecipientUser.trim(),
								"Смена SIM-карты клиента iSimple",
								String.format("У клиента с ИНН \"%s\" ( %s ) на момент \"%s\" зафиксирована смена IMSI для номера телефона \"%s\".",
										imsiNotificationPhoneNumberOwner.getTaxNumber(),
										imsiNotificationPhoneNumberOwner.getFullName(),
										D_MMM_YYYY_HH_MM_F.format(event.getImsiNotification().getCreated()),
										imsiNotificationPhoneNumberOwner.getPhoneNumber()
								)
						)
				);
			}
		}
	}

	@EventListener
	public void handleStatusChange(StatusChangedEvent event) {
		statsRepo.writeStatusTransition(event.getAccountApplication(), event.prev, event.next);
	}

	@EventListener
	public void handleScoringApprovedEvent(ScoringApprovedEvent event) {
		createPdfOnApplicationDeclinedWithCommonScoringSuccess(event.getAccountApplication());
	}

	@EventListener
	public void handleEmarsysSaveEditEvent(EmarsysSaveEditEvent event) {
		log.info("EmarsysSaveEditEvent event");
		AccountApplication accountApplication = event.getAccountApplication();
		ClientValue client = accountApplication.getClient();

		Map<String, Object> params = new HashMap<>();
		params.put(CRM_ID.getKey(), accountApplication.getId());
		params.put(EMAIL.getKey(), client.getEmail());
		params.put(PHONE.getKey(), client.getPhone());
		params.put(INN.getKey(), client.getInn());

		sendEvent(Event.EMARSYS_CONTACT, new Email(null, accountApplication.getId(), client.getEmail(), params));
	}

	@EventListener
	public void handleMessageToClientEvent(MessageToClientEvent event) {
		log.info("MessageToClientEvent event");
		Long clientId = event.getClientId();
		String email = event.getEmail();

		Map<String, Object> params = new HashMap<>();
		params.put(EVENT_ID.getKey(), MESSAGE_TO_CLIENT_EVENT.getEmarsysEventNumber());
		params.put(CRM_ID.getKey(), clientId);
		params.put(EMAIL.getKey(), email);
		params.put(PHONE.getKey(), event.getPhone());
		params.put(MESSAGE.getKey(), event.getMessage());

		sendEvent(Event.EMARSYS_TRIGGER, new Email(null, clientId, email, params));
	}

	@EventListener
	public void handleSmsReminderEvent(SmsReminderEvent event) {
		log.info("SmsReminderEvent event");
		ClientValue client = event.getAccountApplication().getClient();

		Map<String, Object> paramz = new HashMap<>();
		paramz.put(HREF.getKey(), event.getHref());
		Context ctx = new Context();
		ctx.setVariables(paramz);

		sendEvent(Event.SMS, new Sms(client.getPhone(), tt.process("reminder.sms", ctx)));
	}

	@EventListener
	public void handleClientRegisteredInKeycloakFofDbo(ClientRegisteredInKeycloakFofDbo event) {
		log.info("ClientRegisteredInKeycloakFofDbo event");
		String phone = event.getPhone();
		String login = event.getLogin();
		String tempPassword = event.getTempPassword();

		Map<String, Object> params = new HashMap<>();
		params.put(PASSWORD.getKey(), tempPassword);
		Context ctx = new Context();
		ctx.setVariables(params);

		sendEvent(Event.SMS, new Sms(phone, tt.process("reserved.sms", ctx)));

		dboService.sentNewUser((DboRequestCreateUserDto) event.getSource());
	}

	@EventListener
	public void handleInnCheckResultForAnalytics(InnCheckResult event) {
		log.info("InnCheckResult event");
		String result = event.isSuccess() ? "passed" : "not_passed";
		String source = event.isScheduler() ? "scheduler" : "once";
		String type = event.isIs550() ? "550" : "kontur";
		externalConnectors.getIAnalytics().send(
				EventBuilder.withType(config.getEventPrefix() + "Inn validation")
						.userId(event.getAccountApplication().getClient().getPhone())
						.addProperty("result", result)
						.addProperty("source", source)
						.addProperty("type", type)
						.addProperty("message", event.getMessage())
						.build());
	}

	@EventListener
	// todo по логике подходит?
	public void handleCreateCallBackForAnalytics(CallBackForAnalytics event) {
		log.info("handleCreateCallBackForAnalytics event");
		String type = event.isTimeChanged() ? "change_time" : "change_date";
		String value = event.isTimeChanged() ? event.getNewTime() : event.getNewDate();
		externalConnectors.getIAnalytics().send(
				EventBuilder.withType(config.getEventPrefix()+ "Callback later")
						.userId(event.getAccountApplication().getClient().getPhone())
						.addProperty("type", type)
						.addProperty("value", value)
						.build());
	}

	@EventListener
	public void handleClientNeedRegister(ClientNeedRegisterInKeycloakFofDbo event) {
		log.info("ClientNeedRegisterInKeycloakFofDbo event");
		if (!dboProperties.getEnable()) {
			log.info("Dbo integration disabled");
		} else {
			sender.convertAndSend(KEYCLOAK_NAME, event);
		}
	}

	private void createPdfOnApplicationDeclinedWithOkved(AccountApplication accountApplication) {
		ClientValue client = accountApplication.getClient();
		Map<String, String> result = new HashMap<>();

		Map<String, Object> data = new HashMap<>();
		data.put(COMPANY_NAME.getKey(), client.getName());
		data.put(TAX_NUMBER.getKey(), client.getNumber());
		data.put(DATE_TIME.getKey(), V_FORMATTER.format(Instant.now()));
		for (String code : accountApplication.getClient().getBlackListedCodes().split(",")) {
			result.put(code, ClientValueCompanyHelper.okveds.get(code));
		}
		data.put(OKVED_LIST.getKey(), result);

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			pdfGenerator.createPdf(tt, "black-okved.pdf", data, byteArrayOutputStream);
			accountApplication.addBankAttachment("Протокол обработки заявки в системе ApiBank.pdf", byteArrayOutputStream.toByteArray(), "report");

		} catch (Exception e) {
			log.error("error on pdf stream", e);
		}
	}

	private void createPdfOnApplicationDeclinedWithFailedCompanyFeature(AccountApplication accountApplication) {
		ClientValue client = accountApplication.getClient();
		Map<String, String> result = new HashMap<>();

		Map<String, Object> data = new HashMap<>();
		data.put(COMPANY_NAME.getKey(), client.getName());
		data.put(TAX_NUMBER.getKey(), client.getNumber());
		data.put(DATE_TIME.getKey(), V_FORMATTER.format(Instant.now()));
		for (String feature : client.getKonturFeature().getFailedFeatures().split(",")) {
			result.put(feature, ClientValueCompanyHelper.companyFeatures.get(feature));
		}
		data.put(FEATURE_LIST.getKey(), result);

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			pdfGenerator.createPdf(tt, "company-kontur-feature.pdf", data, byteArrayOutputStream);
			accountApplication.addBankAttachment("Протокол проверки по признакам 1.pdf", byteArrayOutputStream.toByteArray(), "report");

		} catch (Exception e) {
			log.error("error on pdf stream", e);
		}
	}

	private void createPdfOnApplicationDeclinedWithCommonScoringFailed(AccountApplication accountApplication) {
		ClientValue client = accountApplication.getClient();
		Map<String, String> result = new HashMap<>();

		Map<String, Object> data = new HashMap<>();
		data.put(COMPANY_NAME.getKey(), client.getName());
		data.put(TAX_NUMBER.getKey(), client.getNumber());
		data.put(DATE_TIME.getKey(), V_FORMATTER.format(Instant.now()));
		for (String feature : client.getCompanyKycScoring().getFailedKycScoring().split(",")) {
			result.put(feature, ClientValueCompanyHelper.companyScoring.get(feature));
		}
		data.put(SCORING_LIST.getKey(), result);
		data.put(POINTS.getKey(), client.getCompanyKycScoring().getCalculatedTotalScore());

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			pdfGenerator.createPdf(tt, "kyc-scoring-fail.pdf", data, byteArrayOutputStream);
			accountApplication.addBankAttachment("Протокол отклонения заявки.pdf", byteArrayOutputStream.toByteArray(), "report");

		} catch (Exception e) {
			log.error("error on pdf stream", e);
		}
	}

	private void createPdfOnApplicationDeclinedWithCommonScoringSuccess(AccountApplication accountApplication) {
		ClientValue client = accountApplication.getClient();
		Map<String, String> result = new HashMap<>();

		Map<String, Object> data = new HashMap<>();
		data.put(COMPANY_NAME.getKey(), client.getName());
		data.put(TAX_NUMBER.getKey(), client.getNumber());
		data.put(DATE_TIME.getKey(), V_FORMATTER.format(Instant.now()));
		for (String feature : client.getCompanyKycScoring().getFailedKycScoring().split(",")) {
			result.put(feature, ClientValueCompanyHelper.companyScoring.get(feature));
		}
		data.put(SCORING_LIST.getKey(), result);
		data.put(POINTS.getKey(), client.getCompanyKycScoring().getCalculatedTotalScore());

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			pdfGenerator.createPdf(tt, "kyc-scoring-success.pdf", data, byteArrayOutputStream);
			accountApplication.addBankAttachment("Протокол одобрения заявки.pdf", byteArrayOutputStream.toByteArray(), "report");

		} catch (Exception e) {
			log.error("error on pdf stream", e);
		}
	}


	private Map<String, Object> createParams(AccountApplication aa) {
		Map<String, Object> params = new HashMap<>();

		fillBaseParams(aa, params);

		params.put(ACCOUNT_NUMBER.getKey(), aa.getAccount().getAccountNumber());
		params.put(DBO_LINK.getKey(), config.getDboUrl());
		params.put(PASSWORD.getKey(), aa.getSimplePassword());

		return params;
	}

	private Map<String, Object> createBaseEmailParameters(AccountApplication aa) {
		Map<String, Object> params = createParamsForTriggerEvents(aa);

		params.put(INN.getKey(), aa.getClient().getNumber());
		params.put(ORIGIN_SOURCE.getKey(), aa.getSource());
		params.put(ACCOUNT_LINK.getKey(), Utils.getAccountLink(config.getLkUrl(), aa.getLoginURL()));

		return params;
	}

	private Map<String, Object> createParamsForTriggerEvents(AccountApplication aa) {
		Map<String, Object> params = addParams(aa, new HashMap<>());

		params.put(CITY.getKey(), aa.getCity().getName());
		params.put(ACCOUNT_NUMBER.getKey(), aa.getAccount().getAccountNumber());

		return params;
	}

	private Map<String, Object> addParams(AccountApplication aa, Map<String, Object> params) {
		ClientValue client = aa.getClient();
		fillBaseParams(aa, params);

		params.put(FIRST_NAME.getKey(), client.isSP() ? client.getFirstName() : "");
		params.put(SECOND_NAME.getKey(), client.isSP() ? client.getSecondName() : "");
		params.put(COMPANY_NAME.getKey(), client.getName());
		return params;
	}

	private void fillBaseParams(AccountApplication aa, Map<String, Object> params) {
		params.put(CRM_ID.getKey(), aa.getId());
		params.put(EMAIL.getKey(), aa.getClient().getEmail());
		params.put(PHONE.getKey(), aa.getClient().getPhone());
	}

	private void fillParams(AccountApplication aa, Map<String, Object> params) {
		addParams(aa, params);

		params.put(INN.getKey(), aa.getClient().getNumber());
		params.put(ORIGIN_SOURCE.getKey(), aa.getSource());
	}

	private void addingAdditionalFields(AccountApplication aa, Map<String, Object> params) {
		if (aa.getClient().isSP()) {
			params.put(KOD_ORG_NUMBER.getKey(), "ОГРНИП");
			params.put(KPP_OGRNIP.getKey(), aa.getClient().getOgrn());
		} else {
			params.put(KOD_ORG_NUMBER.getKey(), "КПП");
			params.put(KPP_OGRNIP.getKey(), aa.getClient().getKpp());
		}
	}
}
