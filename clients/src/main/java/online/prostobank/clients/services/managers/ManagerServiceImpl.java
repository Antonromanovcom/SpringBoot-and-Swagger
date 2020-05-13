package online.prostobank.clients.services.managers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.prostobank.clients.api.dto.client.ClientGridRequest;
import online.prostobank.clients.api.dto.client.PageInfoDTO;
import online.prostobank.clients.api.dto.client.PageSortDTO;
import online.prostobank.clients.api.dto.managers.*;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.managers.Manager;
import online.prostobank.clients.domain.managers.ManagerHistoryItem;
import online.prostobank.clients.domain.managers.ManagerRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import online.prostobank.clients.domain.repository.AccountApplicationRepositoryWrapper;
import online.prostobank.clients.domain.repository.CityRepository;
import online.prostobank.clients.domain.repository.HistoryRepository;
import online.prostobank.clients.security.keycloak.KeycloakAdminClient;
import online.prostobank.clients.security.keycloak.SecurityContextHelper;
import online.prostobank.clients.services.GridUtils;
import online.prostobank.clients.services.client.ClientService;
import online.prostobank.clients.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static online.prostobank.clients.security.UserRoles.getHumanReadableByRoleName;
import static online.prostobank.clients.security.UserRolesConstants.ROLES_MANAGERS;
import static online.prostobank.clients.security.keycloak.SecurityContextHelper.getAllowedRolesForAdmins;
import static online.prostobank.clients.services.GridUtils.getSearchString;
import static online.prostobank.clients.services.GridUtils.getSorting;
import static online.prostobank.clients.services.forui.AccountApplicationViewServiceImpl.TEXT_ASSIGNED_TO;
import static online.prostobank.clients.utils.Utils.UNKNOWN_CITY;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {
	private final ManagerRepository managerRepository;
	private final KeycloakAdminClient keycloakClient;
	private final AccountApplicationRepository accountApplicationRepository;
	private final ClientService clientService;
	private final CityRepository cityRepository;
	private final AccountApplicationRepositoryWrapper repositoryWrapper;
	private final HistoryRepository historyRepository;

	@Scheduled(cron = "0 */5 * ? * *")
	@Override
	public void saveNotExists() {
		City city = cityRepository.findByNameIgnoreCase(UNKNOWN_CITY).orElse(null);
		Set<Manager> newManagers = keycloakClient.getUsersCache().entrySet().stream()
				.filter(entry -> isManager(entry.getValue().getLeft(), ROLES_MANAGERS))
				.filter(entry -> !managerRepository.existsById(UUID.fromString(entry.getKey().getId())))
				.map(Map.Entry::getKey)
				.map(user -> Manager.createFrom(user, city))
				.collect(Collectors.toSet());

		if (newManagers.size() > 0) {
			log.info("save new managers, count {}", newManagers.size());
			managerRepository.saveAll(newManagers);
		}

		List<Manager> managersOnline = managerRepository.findAll()
				.stream()
				.peek(manager ->
						getLastOnline(manager.getId().toString())
								.ifPresent(manager::setLastOnline)
				)
				.filter(manager -> manager.getLastOnline() != null)
				.collect(toList());

		if (managersOnline.size() > 0) {
			log.info("save managersOnline, count {}", managersOnline.size());
			managerRepository.saveAll(managersOnline);
		}
	}


	/*@Override
	public Optional<ManagerDTO> findById(UUID uuid) { // a.romanov - убрал, так как в поле role в карточке менеджера не грузятся роли
		return managerRepository.findById(uuid)
				.map(ManagerDTO::createFullFrom);
	}*/

	@Override
	public Optional<ManagerDTO> findById(UUID uuid) {
		return managerRepository.findById(uuid)
				.map(manager -> ManagerDTO.createFullFromWithRoles(manager,
						keycloakClient.rolesById(manager.getId().toString())
								.stream()
								.map(RoleRepresentation::getName)
								.filter(ROLES_MANAGERS::contains)
								.collect(toList())));
	}

	@Override
	@Transactional
	public Optional<ManagerDTO> saveEdit(ManagerDTO dto) {
		return managerRepository.findById(dto.getUuid())
				.map(manager -> saveChanges(dto, manager))
				.map(ManagerDTO::createFullFrom);
	}

	@Override
	public Optional<?> assignTo(ManagerAssignDTO dto) {
		return managerRepository.findById(dto.getManagerUuid())
				.map(manager -> {
							changeAssigner(
									manager.getLogin(),
									accountApplicationRepository.findByIdIn(dto.getClientIds()));
							return Optional.of("");
						}
				);
	}

	@Override
	public Optional<ManagerGridResponse> getAll(ManagerGridRequest dto) {
		Set<String> allowedRolesForAdmins = getAllowedRolesForAdmins();
		List<String> filterRoles = dto.getFilters().getRoles().stream()
				.filter(allowedRolesForAdmins::contains)
				.collect(toList());
		List<ManagerDTO> result = new ArrayList<>();

		Instant dateFrom = Instant.now().minus(10, MINUTES);
		String filterText = getSearchString(dto.getFilters().getSearch());
		Sort sorting = getSorting(dto.getSort(), GridUtils.MANAGER_ALIAS_MAP);
		List<Long> citiesIds = dto.getFilters().getCities();
		Collection<City> cities;
		if (isEmpty(citiesIds)) {
			cities = null;
		} else {
			cities = cityRepository.findAllByIdIn(citiesIds);
		}
		managerRepository.findAllByFilter(
				dto.getFilters().isOnline(),
				dateFrom,
				filterText == null ? StringUtils.EMPTY : filterText,
				cities,
				sorting
		)
				.forEach(manager -> {
					List<String> roles = keycloakClient.rolesById(manager.getId().toString())
							.stream()
							.map(RoleRepresentation::getName)
							.filter(ROLES_MANAGERS::contains)
							.collect(toList());

					if (!Collections.disjoint(filterRoles, roles)) {
						result.add(ManagerDTO.createFrom(manager, roles));
					}
				});

		sortByRole(result, dto.getSort());

		PageInfoDTO infoDTO = dto.getPage();
		int current = infoDTO.getCurrent();
		int size = infoDTO.getSize();
		int fromIndex = Math.min(result.size(), current * size);
		int toIndex = Math.min(result.size(), fromIndex + size);
		return Optional.of(new ManagerGridResponse()
				.setManagers(result.subList(fromIndex, toIndex))
				.setTotal(result.size()));
	}

	private void sortByRole(List<ManagerDTO> result, PageSortDTO sort) {
		if (sort != null && sort.getBy().equals("role")) {
			result.sort((o1, o2) -> {
				// роль может быть только одна
				String role1 = getHumanReadableByRoleName(o1.getRoles().get(0));
				String role2 = getHumanReadableByRoleName(o2.getRoles().get(0));
				return sort.isReverse()
						? role2.compareToIgnoreCase(role1)
						: role1.compareToIgnoreCase(role2);
			});
		}
	}

	@Override
	public Optional<ClientForManagerGridResponse> getAllClients(ClientGridRequest dto) {
		return clientService.getAll(dto)
				.map(clientsDTO -> {
					List<ClientForManagerGridDTO> collect = clientsDTO.getClients().stream()
							.map(clientGridDTO -> {
								List<String> managerRoles = managerRepository.findByLogin(clientGridDTO.getAssignedTo())
										.map(byLogin -> keycloakClient.rolesById(byLogin.getId().toString()))
										.map(roleRepresentations -> roleRepresentations.stream().map(RoleRepresentation::getName).collect(toList()))
										.orElseGet(Collections::emptyList);
								return ClientForManagerGridDTO.createFrom(clientGridDTO, managerRoles);
							})
							.collect(toList());
					return new ClientForManagerGridResponse()
							.setClients(collect)
							.setTotal(clientsDTO.getTotal());
				});
	}

	private Optional<Instant> getLastOnline(String uuid) {
		return keycloakClient.sessionsById(uuid)
				.stream()
				.map(UserSessionRepresentation::getLastAccess)
				.max(Long::compareTo)
				.map(Instant::ofEpochMilli);
	}

	private static boolean isManager(List<RoleRepresentation> roleRepresentations, Set<String> roleNames) {
		return roleRepresentations.stream()
				.anyMatch(roleRepresentation -> roleNames.contains(roleRepresentation.getName()));
	}

	private Manager saveChanges(ManagerDTO newDto, Manager oldEntity) {
		final String compareResult = oldEntity.compareFields(newDto);
		if (!compareResult.isEmpty()) {
			oldEntity.setFirstName(newDto.getFirstName())
					.setSecondName(newDto.getSecondName())
					.setLastName(newDto.getLastName())
					.setPhone(newDto.getPhone())
					.getManagerHistoryItems().add(new ManagerHistoryItem(oldEntity, compareResult));

			cityRepository.findById(newDto.getCity().getId())
					.ifPresent(oldEntity::setCity);
		}
		return managerRepository.save(oldEntity);
	}

	private void changeAssigner(String username, Set<AccountApplication> applications) {
		for (AccountApplication currentApp : applications) {
			String oldAssignedTo = currentApp.getAssignedTo();
			currentApp.setAssignedTo(username);
			Pair<Boolean, AccountApplication> pair = repositoryWrapper.saveAccountApplication(currentApp);
			if (pair.getFirst()) {
				AccountApplication second = pair.getSecond();
				String message = String.format(TEXT_ASSIGNED_TO, oldAssignedTo, username);
				historyRepository.insertChangeHistory(second.getId(), SecurityContextHelper.getCurrentUsername(), message);
				log.info("заявка {} {}. инициатор {} время: {}",
						second.getId(), message, SecurityContextHelper.getCurrentUsername(), Utils.dateFormat(Instant.now(), Utils.DATE_TIME_FORMAT));
			}
		}
	}
}
