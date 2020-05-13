package online.prostobank.clients.api.dto.managers;

import lombok.Data;
import lombok.experimental.Accessors;
import online.prostobank.clients.api.dto.dictionary.CityDTO;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.managers.Manager;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.toList;

@Data
@Accessors(chain = true)
public class ManagerDTO {
	@NotNull
	private UUID uuid;
	@NotNull
	private String login;
	private boolean online;
	private Instant lastOnline;
	private List<ManagerHistoryItemDTO> historyItems;

	private String firstName;
	private String secondName;
	private String lastName;
	private CityDTO city;
	private String phone;
	private List<String> roles;

	public static ManagerDTO createFrom(Manager manager) {
		Instant lastOnline = manager.getLastOnline();
		boolean online = lastOnline != null && lastOnline.isAfter(Instant.now().minus(10, MINUTES));
		City city = manager.getCity();
		return new ManagerDTO()
				.setUuid(manager.getId())
				.setLogin(manager.getLogin())
				.setFirstName(manager.getFirstName())
				.setSecondName(manager.getSecondName())
				.setLastName(manager.getLastName())
				.setCity(city != null ? CityDTO.createFrom(city) : null)
				.setPhone(manager.getPhone())
				.setLastOnline(lastOnline)
				.setOnline(online);
	}

	public static ManagerDTO createFrom(Manager manager, List<String> roles) {
		return createFrom(manager)
				.setRoles(roles);
	}

	public static ManagerDTO createFullFrom(Manager manager) {
		List<ManagerHistoryItemDTO> historyItems = manager
				.getManagerHistoryItems()
				.stream()
				.map(ManagerHistoryItemDTO::createFrom)
				.collect(toList());

		return createFrom(manager)
				.setHistoryItems(historyItems);
	}

	public static ManagerDTO createFullFromWithRoles(Manager manager, List<String> roles) {

		List<ManagerHistoryItemDTO> historyItems = manager
				.getManagerHistoryItems()
				.stream()
				.map(ManagerHistoryItemDTO::createFrom)
				.collect(toList());

		return createFrom(manager, roles)
				.setHistoryItems(historyItems);
	}
}
