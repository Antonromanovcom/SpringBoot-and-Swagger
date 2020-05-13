package online.prostobank.clients.services.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.ResponseDTO;
import online.prostobank.clients.api.dto.client.*;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.api.dto.rest.AttachmentDTO;
import online.prostobank.clients.api.dto.state.StateSetterDTO;
import online.prostobank.clients.domain.*;
import online.prostobank.clients.domain.enums.AccountantNoSignPermission;
import online.prostobank.clients.domain.enums.BankId;
import online.prostobank.clients.domain.enums.Source;
import online.prostobank.clients.domain.enums.TaxationType;
import online.prostobank.clients.domain.events.ClientNeedRegisterInKeycloakFofDbo;
import online.prostobank.clients.domain.events.InformClientEvent;
import online.prostobank.clients.domain.events.MessageToClientEvent;
import online.prostobank.clients.domain.exceptions.EmailDuplicateException;
import online.prostobank.clients.domain.messages.MessageToClient;
import online.prostobank.clients.domain.messages.MessageToClientRepository;
import online.prostobank.clients.domain.repository.*;
import online.prostobank.clients.domain.state.event.ClientEvents;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.security.keycloak.KeycloakAdminClient;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import online.prostobank.clients.services.forui.AccountApplicationViewService;
import online.prostobank.clients.services.forui.ChecksService;
import online.prostobank.clients.services.forui.ColdApplicationService;
import online.prostobank.clients.services.state.StateMachineServiceI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static online.prostobank.clients.api.ApiConstants.EXCEPTION_MESSAGE;
import static online.prostobank.clients.domain.state.event.ClientEvents.ACCOUNT_OPEN;
import static online.prostobank.clients.domain.state.event.ClientEvents.CE_SMS;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.isAllowed;
import static online.prostobank.clients.services.GridUtils.*;
import static online.prostobank.clients.utils.Utils.UNKNOWN_CITY;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClientServiceImpl implements ClientService {
	private final AccountApplicationRepository accountApplicationRepository;
	private final CityRepository cityRepository;
	private final KeycloakAdminClient keycloakAdminClient;
	private final EmailMessagesRepository emailMessagesRepository;
	private final ChecksService checksService;
	private final AccountApplicationViewService accountApplicationViewService;
	private final ColdApplicationService coldApplicationService;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;
	private final ApplicationEventPublisher bus;
	private final MessageToClientRepository messageToClientRepository;
	private final HistoryRepository historyRepository;
	private final StateMachineServiceI stateMachineService;

	@Override
	public Optional<ClientEditDTO> saveEditClientInfo(ClientEditDTO dto) throws Exception {
		Optional<AccountApplication> byId = accountApplicationRepository.findById(dto.getClientId());
		if (byId.isPresent() && isAllowed(byId.get().getSource(), byId.get().getAssignedTo())) {
			AccountApplication savedAccount = saveAccountApplication(editFrom(byId.get(), dto)).getSecond();
			return Optional.of(ClientEditDTO.createFrom(savedAccount));
		}
		return Optional.empty();
	}

	@Override
	public Optional<QuestionnaireDTO> saveEditQuestionnaire(QuestionnaireDTO dto) {
		return accountApplicationRepository.findById(dto.getClientId())
				.filter(application -> isAllowed(application.getSource(), application.getAssignedTo()))
				.map(application -> saveAccountApplication(editFrom(application, dto)).getSecond())
				.map(QuestionnaireDTO::createFrom);
	}

	@Override
	public Optional<ClientGridResponse> getAll(ClientGridRequest request) {
		Collection<City> selectedCities = getSelectedCities(request.getFilters().getCities());
		Collection<ClientStates> selectedStatuses = getSelectedStatuses(request.getFilters().getStatuses());
		String searchString = getSearchString(request.getFilters().getSearch());
		List<Source> sources = SecurityContextHelper.getSources();
		List<String> assignedTo = SecurityContextHelper.getAssignedTo(request.getFilters().isOnlySelf(), keycloakAdminClient);
		Pair<Instant, Instant> updateFilterPeriod = getFilterPeriod(request.getFilters().getUpdatedAt());
		Pair<Instant, Instant> createFilterPeriod = getFilterPeriod(request.getFilters().getCreatedAt());

		PageRequest paging = getPaging(request.getPage(), getSorting(request.getSort(), CLIENT_ALIAS_MAP));

		Long total = accountApplicationRepository.countExtendFiltered(
				selectedCities,
				selectedStatuses,
				searchString,
				sources,
				assignedTo,
				updateFilterPeriod.getFirst(),
				updateFilterPeriod.getSecond(),
				createFilterPeriod.getFirst(),
				createFilterPeriod.getSecond()
		);

		List<ClientGridDTO> clients = accountApplicationRepository
				.findExtendedFiltered(
						selectedCities,
						selectedStatuses,
						searchString,
						sources,
						assignedTo,
						updateFilterPeriod.getFirst(),
						updateFilterPeriod.getSecond(),
						createFilterPeriod.getFirst(),
						createFilterPeriod.getSecond(),
						paging)
				.stream()
				.map(ClientGridDTO::createFrom)
				.collect(Collectors.toList());

		return Optional.of(new ClientGridResponse(total, clients));
	}

	@Override
	public Optional<ClientCardDTO> findById(long clientId, Collection<String> roles) {
		return accountApplicationRepository.findById(clientId)
				.filter(application -> isAllowed(application.getSource(), application.getAssignedTo()))
				.map(application -> {
					int duplicateSize = 0;
					int duplicateInactive = 0;
					String num = application.getClient().getNumber();
					if (num != null) {
						duplicateSize = accountApplicationRepository.countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCase(num, num) - 1;
						duplicateInactive = accountApplicationRepository.countByClientInnLikeIgnoreCaseOrClientOgrnLikeIgnoreCaseAndActiveFalse(num, num);
					}
					int emailDuplicateCount = Math.max(0, Optional.ofNullable(application.getClient().getEmail())
							.map(accountApplicationRepository::countByClientEmailLikeIgnoreCase)
							.orElse(0) - 1);
					DuplicateDTO duplicate = new DuplicateDTO(duplicateSize, duplicateInactive, emailDuplicateCount);

					List<EmailMessagesEntity> messagesFromClients = emailMessagesRepository.getEmailMessages(clientId);
					List<MessageToClient> messagesToClients = messageToClientRepository.findByClientId(clientId);
					return ClientCardDTO.createFrom(application, duplicate, roles, messagesFromClients, messagesToClients);
				});
	}

	@Override
	public Optional<ClientCardDTO> checkClient(Long id, CheckType check) {
		accountApplicationRepository.findById(id)
				.map(application -> check.getFunction().apply(checksService, application))
				.map(Pair::getSecond)
				.filter(it -> it.getStatus().getValue().equals(Status.ERR_AUTO_DECLINE))
				.ifPresent(it -> stateMachineService.setState(new StateSetterDTO(it.getId(), ClientEvents.AUTO_DECLINE)));
		return accountApplicationRepository.findById(id)
				.map(ClientCardDTO::createFrom);
	}

	@Override
	public Optional<ClientCardDTO> saveComment(Long id, String text, String username) {
		return accountApplicationRepository.findById(id)
				.map(application -> accountApplicationViewService.saveComment(application, text, username))
				.map(Pair::getSecond)
				.map(ClientCardDTO::createFrom);
	}

	@Override
	public Optional<Boolean> resendToUser(Long id) {
		return accountApplicationRepository.findById(id)
				.map(application -> {
					bus.publishEvent(new InformClientEvent(application));
					return application.getClientState() == ClientStates.AUTO_DECLINED;
				});
	}

	@Override
	public Optional<String> smsReminder(Long id) {
		return accountApplicationRepository.findById(id)
				.map(accountApplicationViewService::smsRemind)
				.map(Pair::getFirst);
	}

	@Override
	public Optional<byte[]> exportPdf(Long id) {
		return accountApplicationRepository.findById(id)
				.map(accountApplicationViewService::pdfListener);
	}

	@Override
	public Optional<Boolean> assignToMe(Long id, String user) {
		return accountApplicationRepository.findById(id)
				.map(application -> accountApplicationViewService.setAssignedTo(application, user))
				.map(Pair::getFirst);
	}

	@Override
	public Optional<Boolean> startWork(Long id, String name) {
		return accountApplicationRepository.findById(id)
				.map(application -> accountApplicationViewService.startWork(application, name))
				.map(Pair::getFirst);
	}

	@Override
	public Optional<Boolean> assignTo(Long id, String newUser) {
		return accountApplicationRepository.findById(id)
				.map((AccountApplication applicationId) -> accountApplicationViewService.setAssignedTo(applicationId, newUser))
				.map(Pair::getFirst);
	}

	@Override
	public Optional<String> smsCheck(Long id) {
		return accountApplicationRepository.findById(id)
				.map(application -> {
					accountApplicationViewService.decideAndSendSms(application);
					return application.getConfirmationCode();
				});
	}

	@Override
	public Optional<Boolean> smsConfirmation(Long id, String code) {
		return accountApplicationRepository.findById(id)
				.map(application -> {
					Pair<Boolean, AccountApplication> pair = accountApplicationViewService.smsConfirmationAddHistory(application, code);
					stateMachineService.setState(new StateSetterDTO(application.getId(), CE_SMS));
					return pair.getFirst();
				});
	}

	@Override
	public Optional<Boolean> resetSmsCheck(Long id) {
		return accountApplicationRepository.findById(id)
				.map(accountApplicationViewService::resetConfirmationCode)
				.map(Pair::getFirst);
	}

	@Override
	public Optional<Boolean> existApplications(String name) {
		long exist = accountApplicationRepository.countAllByClientEmailAndActiveIsTrue(name);
		return Optional.of(exist > 0);
	}

	@Override
	public Optional<List<AttachmentDTO>> attachmentList(String name) {
		return accountApplicationRepository.findByClientEmailLikeIgnoreCaseAndActiveIsTrue(name)
				.map(application -> AttachmentDTO.createListFrom(application.getAttachments(), application.getId())
				);
	}

	@Override
	public ResponseDTO createClientCard(ClientCardCreateDTO dto) {
		Optional<City> byId = Optional.ofNullable(dto.getCity())
				.flatMap(cityRepository::findById);
		if (!byId.isPresent()) {
			byId = cityRepository.findByNameIgnoreCase(UNKNOWN_CITY);
		}
		return byId
				.map(city -> coldApplicationService.createColdApplication(dto, city))
				.orElse(ResponseDTO.badResponse(EXCEPTION_MESSAGE));
	}

	@Override
	public Optional<?> fulfilled(Long clientId, Long appId) {
		stateMachineService.setState(new StateSetterDTO(clientId, ACCOUNT_OPEN));
		return Optional.of("");
	}

	@Override
	public Optional<?> reserved(Long clientId, Long appId, String accountNumber, String requestId) {
		accountApplicationRepository.findById(clientId)
				.ifPresent(application -> {
//					stateMachineService.setState(new StateSetterDTO(clientId, CHECKS_DONE));
					application.setAccount(new AccountValue(accountNumber, requestId, BankId.KUB));
					saveAccountApplication(application);
					ClientValue client = application.getClient();
					bus.publishEvent(new ClientNeedRegisterInKeycloakFofDbo(
							application.getId(),
							client.getEmail(),
							client.getPhone(),
							client.getFirstName(),
							client.getSurname(),
							client.getSecondName(),
							client.getInn()));
				});
		log.info("application setStatus NEW, application setAccount");
		return Optional.of("");
	}

	@Override
	public Optional<ClientGridResponse> getByAssignedTo(String assignedTo) {

		List<ClientGridDTO> clients = accountApplicationRepository
				.getAssignedAppsOnlyByUser(assignedTo)
				.stream()
				.map(ClientGridDTO::createFrom)
				.collect(Collectors.toList());

		return Optional.of(new ClientGridResponse(clients.size(), clients));
	}

	@Override
	public Optional<PassportDTO> editPassport(Long clientId, PassportDTO passportDTO) {
		if (clientId == null || passportDTO == null) {
			return Optional.empty();
		}
		return accountApplicationRepository.findById(clientId)
				.map(application -> {
					PersonValue personValue = new PersonValue(passportDTO);
					//нельзя непустой СНИЛС сделать пустым, только заменить другим валидным
					if (application.getPerson() != null
							&& StringUtils.isEmpty(personValue.getSnils())
							&& !StringUtils.isEmpty(application.getPerson().getSnils())) {
						personValue.setSnils(application.getPerson().getSnils());
					}
					application.setPerson(personValue);
					return saveAccountApplication(application);
				})
				.filter(Pair::getFirst)
				.map(pair -> PassportDTO.createFrom(pair.getSecond()));
	}

	@Override
	public Optional<Boolean> sendMessageToClient(MessageDTO message, UUID managerId, String name) {
		Long clientId = message.getClientId();
		return accountApplicationRepository.findById(clientId)
				.map(application -> {
					String text = message.getText();
					MessageToClient entity = new MessageToClient(clientId, managerId, name, text);
					messageToClientRepository.save(entity);

					ClientValue client = application.getClient();
					bus.publishEvent(new MessageToClientEvent(clientId, text, client.getEmail(), client.getPhone()));

					application.addHistoryRecord("Менеджер " + name + " отправил клиенту " + client.getName() + " сообщение: " + text);
					log.info("username {} send to clientId {} message {}", name, clientId, text);
					return repositoryWrapper.saveAccountApplication(application).getFirst();
				});
	}

	private Pair<Boolean, AccountApplication> saveAccountApplication(AccountApplication application) {
		return repositoryWrapper.saveAccountApplication(application, true);
	}

	private Collection<ClientStates> getSelectedStatuses(List<Integer> statuses) {
		return Arrays.stream(ClientStates.values())
				.filter(status -> statuses.contains(status.getIndex()))
				.collect(Collectors.toList());
	}

	private Collection<City> getSelectedCities(List<Long> cities) {
		return cityRepository.findAllByIdIn(cities);
	}

	private AccountApplication editFrom(AccountApplication application, QuestionnaireDTO dto) {
		Optional.ofNullable(dto.getTaxationType())
				.map(TaxationType::getRuName)
				.ifPresent(application::setTaxForm);
		Optional.ofNullable(dto.getMonthlyRevenue())
				.map(String::valueOf)
				.ifPresent(application::setIncome);
		Optional.ofNullable(dto.getInnOfSupplier())
				.ifPresent(application::setContragentsRecip);
		Optional.ofNullable(dto.getInnOfConsumer())
				.ifPresent(application::setContragents);

		QuestionnaireValue questionnaire = application.getQuestionnaireValue();
		Optional.ofNullable(dto.getRealCompanySize())
				.ifPresent(questionnaire::setRealCompanySize);
		Optional.ofNullable(dto.getIsChiefAccountantPresent())
				.ifPresent(isChiefAccountantPresent ->
						questionnaire.setIsChiefAccountantPresent(isChiefAccountantPresent ? "Да" : "Нет"));
		Optional.ofNullable(dto.getAccountNoSignPermission())
				.map(AccountantNoSignPermission::getCaption)
				.ifPresent(questionnaire::setAccountNoSignPermission);
		Optional.ofNullable(dto.getOfficialSite())
				.ifPresent(questionnaire::setOfficialSite);
		Optional.ofNullable(dto.getBusinessType())
				.ifPresent(questionnaire::setBusinessType);

		ClientValue client = application.getClient();
		Optional.ofNullable(dto.getRegistrationAddress())
				.ifPresent(client::setAddress);
		Optional.ofNullable(dto.getRealLocationAddress())
				.ifPresent(client::setResidentAddress);

		Contracts contractTypes = application.getContractTypes();
		Optional.ofNullable(dto.getContractType())
				.ifPresent(types ->
						types.forEach((key, value) ->
								key.getSetter().accept(contractTypes, value)));
		Optional.ofNullable(dto.getOtherText())
				.ifPresent(contractTypes::setOtherText);

		return application;
	}

	private AccountApplication editFrom(AccountApplication application, ClientEditDTO dto) throws Exception {
		ClientValue client = application.getClient();

		if (StringUtils.isBlank(dto.getInn()) && !StringUtils.isBlank(client.getInn())) {
			throw new Exception("Нельзя удалить ИНН/ОГРН");
		}

		if (!StringUtils.isBlank(dto.getInn())) {
			boolean isInnOrOgrnValid = client.setInnOgrn(dto.getInn());
			if (!isInnOrOgrnValid) {
				throw new Exception("Неверный формат ИНН/ОГРН");
			}
		}

		if (dto.getEmail() != null) {
			try {
				client.editEmail(dto.getEmail(), true, true);
			} catch (IllegalArgumentException | EmailDuplicateException e) {
				log.error(e.getLocalizedMessage());
				throw new Exception(e.getLocalizedMessage());
			}
		}

		if (dto.getPhone() != null) {
			try {
				client.editPhone(dto.getPhone());
			} catch (IllegalArgumentException e) {
				log.error(e.getLocalizedMessage());
				throw new Exception(e.getLocalizedMessage());
			}
		}

		Optional.ofNullable(dto.getCity())
				.map(CityDTO::getId)
				.flatMap(cityRepository::findById)
				.ifPresent(application::editCity);

		return application;
	}

	//период времени длительностью в одни сутки,  в который попадает указанный instant
	//если instant == null, то период он начала эпохи до текущего момента
	private Pair<Instant, Instant> getFilterPeriod(Instant instant) {
		if (instant != null) {
			Instant from = instant.truncatedTo(ChronoUnit.DAYS);
			return Pair.of(from, from.plus(Period.ofDays(1)));
		} else {
			return Pair.of(Instant.ofEpochSecond(0), Instant.now());
		}
	}

	@Override
	public void saveHistoryItem(HistoryItemDTO dto, String initiator) throws Exception {
		if (dto == null) {
			throw new Exception("Отсутствуют входные данные");
		}
		historyRepository.insertChangeHistory(dto.getClientId(), initiator, dto.getMessage(), dto.getItemType());
	}

	@Override
	public Optional<?> recheckP550(List<String> inns) {
		CompletableFuture.supplyAsync(() -> {
			log.info("start recheckP550, inns count: {}", inns.size());
			accountApplicationRepository.findByClientInnIn(inns)
					.stream()
					.map(it -> checksService.recheckP550(it).getSecond())
					.filter(it -> it.getStatus().getValue().equals(Status.ERR_AUTO_DECLINE))
					.forEach(it -> stateMachineService.setState(new StateSetterDTO(it.getId(), ClientEvents.AUTO_DECLINE)));
			log.info("finished recheckP550");
			return true;
		});
		return Optional.of("started");
	}

	@Override
	public Optional<Boolean> docsNeedSignal(Long clientId) {
		return accountApplicationRepository.findById(clientId)
				.map(it -> stateMachineService.setState(new StateSetterDTO(it.getId(), ClientEvents.NEED_DOCS)))
				.map(it -> true);
	}

	@Override
	public Optional<Boolean> accountDecline(Long clientId, String causeMessage) {
		return accountApplicationRepository.findById(clientId)
				.map(it -> {
					StateSetterDTO state = new StateSetterDTO(it.getId(), ClientEvents.ACCOUNT_CLOSE, causeMessage);
					return stateMachineService.setState(state)
							.map(result -> result.getState().equals(ClientStates.INACTIVE_CLIENT))
							.orElse(false);
				});
	}

	@Override
	public Optional<ClientAuxInfoDTO> setClientAuxInfo(Long clientId, ClientAuxInfoDTO dto) {
		return accountApplicationRepository.findById(clientId)
				.map(application -> {
					application.setClientTariffPlan(dto.getTariff());
					application.setClientCallback(dto.getRecallTimeMs() != null ? Instant.ofEpochMilli(dto.getRecallTimeMs()) : null);
					return repositoryWrapper.saveAccountApplication(application);
				})
				.map(Pair::getSecond)
				.map(ClientAuxInfoDTO::from);
	}
}
