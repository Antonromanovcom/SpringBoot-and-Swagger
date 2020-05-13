package online.prostobank.clients.domain.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.prostobank.clients.api.dto.managers.ManagerDTO;

import java.util.function.Function;

@Getter
@AllArgsConstructor
enum ManagerField {
	FIRST_NAME("firstName", "Имя", Manager::getFirstName, ManagerDTO::getFirstName),
	SECOND_NAME("secondName", "Отчество", Manager::getSecondName, ManagerDTO::getSecondName),
	LAST_NAME("lastName", "Фамилия", Manager::getLastName, ManagerDTO::getLastName),
	REGION("cityName", "Город", manager -> {
		return manager.getCity().getName();
	}, managerDTO -> {
		return managerDTO.getCity().getName();
	}),
	PHONE("phone", "Телефон", Manager::getPhone, ManagerDTO::getPhone),
	;

	private final String key;
	private final String ruName;
	private final Function<Manager, String> managerGetter;
	private final Function<ManagerDTO, String> managerDtoGetter;
}
