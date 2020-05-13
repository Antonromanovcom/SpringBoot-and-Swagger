package online.prostobank.clients.services.dbo.service;

import club.apibank.connectors.domain.RegisterUserDTO;
import club.apibank.connectors.domain.UserType;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.config.properties.DboProperties;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ClientValue;
import online.prostobank.clients.domain.client.ClientKeycloak;
import online.prostobank.clients.domain.client.ClientKeycloakRepository;
import online.prostobank.clients.domain.events.ClientNeedRegisterInKeycloakFofDbo;
import online.prostobank.clients.domain.events.ClientRegisteredInKeycloakFofDbo;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.state.state.ClientStates;
import online.prostobank.clients.security.UserService;
import online.prostobank.clients.security.keycloak.KeycloakAdminClient;
import online.prostobank.clients.services.ExcelGenerator;
import online.prostobank.clients.services.dbo.model.DboRequestCreateUserDto;
import online.prostobank.clients.services.email.EmailService;
import online.prostobank.clients.utils.aspects.JsonLogger;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static club.apibank.connectors.KeycloakApiConnectorImpl.*;
import static java.util.stream.Collectors.toList;
import static online.prostobank.clients.domain.enums.EventName.KEYCLOAK_NAME;
import static online.prostobank.clients.services.dbo.model.DboRole.DBO_DEMO_USER;
import static online.prostobank.clients.services.dbo.model.DboRole.DBO_USER;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@JsonLogger
@Slf4j
@Service
@AllArgsConstructor
public class ClientRegisterServiceImpl implements ClientRegisterService {
	private final UserService userService;
	private final AccountApplicationRepository accountApplicationRepository;
	private final AccountApplicationJdbcRepository applicationJdbcRepository;
	private final ApplicationEventPublisher bus;
	private final DboProperties dboProperties;
	private final JmsTemplate sender;
	private final KeycloakAdminClient keycloakClient;
	private final ExcelGenerator excelGenerator;
	private final EmailService emailService;
	private final ClientKeycloakRepository clientKeycloakRepository;

	@Override
	public int clientRegisterInKeycloak(ClientNeedRegisterInKeycloakFofDbo event) {
		// APIKUB-1380 Сразу после успеха резервирования счета ПОС должен зарегистрировать учетную запись клиента в Keycloak.
		// Логин - почта, пароль - сгенерировать и поставить как одноразовый.
		log.info("Создаём пользователя в Keycloak");
		String login = event.getLogin().toLowerCase();
		if (isEmpty(login.trim())) {
			log.info("Пустой логин");
			return 0;
		}
		String firstName = event.getFirstName();
		String lastName = event.getLastName();
		String middleName = event.getMiddleName();
		String inn = event.getInn();
		String phone = event.getPhone();
		String tempPassword = RandomStringUtils.randomAlphabetic(10);
		RegisterUserDTO userDTO = new RegisterUserDTO(login, login, firstName, lastName, middleName, inn, phone, tempPassword, true, UserType.CLIENT);

		Map<String, Object> registerResultMap = userService.registerKeycloakUser(userDTO, Collections.singleton(DBO_DEMO_USER.getName()));
		int registerStatus = (int) registerResultMap.get(USER_REGISTER_STATUS_CODE);
		if (registerStatus == SUCCESS_STATUS) {
			log.info("клиент {} успешно создан в keycloak", login);
			Integer registerStatusRoles = (Integer) registerResultMap.get(SET_ROLES_STATUS_CODE);
			log.info("статус при заведении ролей {}", registerStatusRoles);
			String keycloakId = (String) registerResultMap.get(USER_ID);
			Long appId = event.getAppId();
			clientKeycloakRepository.save(new ClientKeycloak(appId, login, UUID.fromString(keycloakId), Instant.now()));
			accountApplicationRepository
					.findById(appId)
					.map(application -> {
						bus.publishEvent(new ClientRegisteredInKeycloakFofDbo(DboRequestCreateUserDto.createFrom(application, keycloakId), tempPassword, login, event.getPhone()));
						return "";
					})
					.orElseGet(() -> {
						log.error("not found app for request to dbo");
						return "";
					});
		} else if (registerStatus == LOGIN_EXIST_STATUS) {
			log.info("такой клиент: {} уже есть в keycloak", login);
			List<UserRepresentation> userByUsername = keycloakClient.getUserByUsername(login);
			if (!userByUsername.isEmpty()) {
				UserRepresentation userRepresentation = userByUsername.get(0);
				Long appId = event.getAppId();
				UUID keycloakId = UUID.fromString(userRepresentation.getId());
				Optional<ClientKeycloak> byKeycloakId = clientKeycloakRepository.findByKeycloakId(keycloakId);
				Optional<ClientKeycloak> byId = clientKeycloakRepository.findById(appId);
				if (!byKeycloakId.isPresent() && !byId.isPresent()) {
					Instant createdAt = Instant.ofEpochMilli(userRepresentation.getCreatedTimestamp());
					ClientKeycloak entity = new ClientKeycloak(appId, login, keycloakId, createdAt);
					clientKeycloakRepository.save(entity);
				}
			}
		} else {
			log.info("произошла ошибка при заведении клиента {} в keycloak, статус ошибки {}", login, registerStatus);
		}
		return registerStatus;
	}

	@Override
	public void clientChangeRoleInKeycloak(Long applicationId) {
		if (!dboProperties.getEnable()) {
			log.info("Dbo integration disabled");
			return;
		}
		clientKeycloakRepository.findById(applicationId)
				.map(clientKeycloak -> {
					String keycloakId = clientKeycloak.getKeycloakId().toString();
					changeRole(keycloakId);
					return "";
				})
				.orElseGet(() -> {
					String email = applicationJdbcRepository.getEmail(applicationId).toLowerCase();
					List<UserRepresentation> userByUsername = keycloakClient.getUserByUsername(email);
					if (!userByUsername.isEmpty()) {
						UserRepresentation userRepresentation = userByUsername.get(0);
						String keycloakId = userRepresentation.getId();
						Instant createdAt = Instant.ofEpochMilli(userRepresentation.getCreatedTimestamp());
						UUID uuid = UUID.fromString(keycloakId);
						Optional<ClientKeycloak> byKeycloakId = clientKeycloakRepository.findByKeycloakId(uuid);
						if (!byKeycloakId.isPresent()) {
							clientKeycloakRepository.save(new ClientKeycloak(applicationId, email, uuid, createdAt));
							changeRole(keycloakId);
						}
					}
					return "";
				});
	}

	private void changeRole(String keycloakId) {
		int deleteUserRoles = userService.deleteUserRoles(keycloakId, Collections.singleton(DBO_DEMO_USER.getName()));
		int setUserRoles = userService.setUserRoles(keycloakId, Collections.singleton(DBO_USER.getName()));
		log.info("deleteUserRoles: {}, setUserRoles: {}", deleteUserRoles, setUserRoles);
	}

	@Scheduled(cron = "${dbo.checkOld.cron}")
	@Override
	public void findClientAndRegisterInKeycloak() {
		if (dboProperties.getEnable()) {
			log.info("findClientAndRegisterInKeycloak started");
			List<AccountApplication> newAndNullKeycloak = accountApplicationRepository.findNewAndNullKeycloak(
					ImmutableSet.of(
							ClientStates.WAIT_FOR_DOCS,
							ClientStates.DOCUMENTS_EXISTS,
							ClientStates.REQUIRED_DOCS,
							ClientStates.MANAGER_PROCESSING,
							ClientStates.ACTIVE_CLIENT,
							ClientStates.NO_ANSWER
					));

			Map<String, UserRepresentation> nameIds = keycloakClient.getUsersCache().keySet().stream()
					.collect(Collectors.toMap(UserRepresentation::getUsername, Function.identity()));

			Set<AccountApplication> toExcel = new HashSet<>();
			newAndNullKeycloak.forEach(application -> {
				String email = application.getClient().getEmail().toLowerCase();
				if (nameIds.containsKey(email)) { // для клиентов email - это логин (APIKUB-1435)
					UserRepresentation userRepresentation = nameIds.get(email);
					String id = userRepresentation.getId();
					UUID uuid = UUID.fromString(id);
					Optional<ClientKeycloak> byKeycloakId = clientKeycloakRepository.findByKeycloakId(uuid);
					if (!byKeycloakId.isPresent()) {
						Instant createdAt = Instant.ofEpochMilli(userRepresentation.getCreatedTimestamp());
						ClientKeycloak entity = new ClientKeycloak(application.getId(), email, uuid, createdAt);
						clientKeycloakRepository.save(entity);
					}
				} else {
					for (UserRepresentation userRepresentation : nameIds.values()) {
						if (email.equalsIgnoreCase(userRepresentation.getEmail())) {
							log.warn("Попытка создать пользователя с почтой, которая уже используется, email: {}, appId: {}", email, application.getId());
							return;
						}
					}
					toExcel.add(application);
				}
			});

			processToExcel(toExcel);
		}
	}

	private void processToExcel(Set<AccountApplication> toExcel) {
		if (!dboProperties.getCheckOld() || toExcel.isEmpty()) {
			return;
		}
		log.info("need register in keycloak and export to excel clients, count {}", toExcel.size());
		toExcel.forEach(application -> {
			ClientValue client = application.getClient();
			sender.convertAndSend(KEYCLOAK_NAME, new ClientNeedRegisterInKeycloakFofDbo(
					application.getId(),
					client.getEmail(),
					client.getPhone(),
					client.getFirstName(),
					client.getSurname(),
					client.getSecondName(),
					client.getInn()));
		});

		try {
			Thread.sleep(1000 * 120);
		} catch (InterruptedException e) {
			log.error(e.getLocalizedMessage(), e);
		}

		Set<String> toExcelEmails = toExcel.stream()
				.map(application -> application.getClient().getEmail().toLowerCase())
				.collect(Collectors.toSet());

		Map<String, UserRepresentation> nameIds = keycloakClient.getUsersCache().keySet().stream()
				.filter(key -> toExcelEmails.contains(key.getUsername()))
				.collect(Collectors.toMap(UserRepresentation::getUsername, Function.identity()));

		List<ClientKeycloak> excelDto =
				toExcel.stream()
						.filter(application -> nameIds.containsKey(application.getClient().getEmail().toLowerCase()))
						.map(application -> {
							Long clientId = application.getId();
							String login = application.getClient().getEmail().toLowerCase();
							UserRepresentation userRepresentation = nameIds.get(login);
							UUID keycloakId = UUID.fromString(userRepresentation.getId());
							Instant createdAt = Instant.ofEpochMilli(userRepresentation.getCreatedTimestamp());
							return new ClientKeycloak(clientId, login, keycloakId, createdAt);
						})
						.collect(toList());

		log.info("after register in keycloak, export to excel clients, count {}, was {}", excelDto.size(), toExcel.size());
		if (excelDto.size() > 0) {
			XSSFWorkbook workBook = excelGenerator.createKeycloakWorkBook(excelDto);
			emailService.sendWorkBook(workBook,
					"Выгрузка keycloak данных клиентов",
					"Информация о регистрация в keycloak тех, у кого зарезервирован счет",
					"d.ilyina@apibank.online"
			);
		}
	}
}
