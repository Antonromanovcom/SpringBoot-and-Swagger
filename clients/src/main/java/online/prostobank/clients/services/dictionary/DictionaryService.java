package online.prostobank.clients.services.dictionary;

import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.state.state.ClientStates;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DictionaryService {
	/**
	 * @param roles - роли юзера
	 * @return список доступных статусов
	 */
	Optional<Set<ClientStates>> getStatuses(Collection<String> roles);

	/**
	 * @return список городов
	 */
	Optional<List<City>> getCities();
}
