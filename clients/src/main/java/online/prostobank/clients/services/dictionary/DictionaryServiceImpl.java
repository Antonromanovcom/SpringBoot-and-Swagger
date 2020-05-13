package online.prostobank.clients.services.dictionary;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.domain.City;
import online.prostobank.clients.domain.repository.CityRepository;
import online.prostobank.clients.domain.state.state.ClientStates;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class DictionaryServiceImpl implements DictionaryService {
	private final CityRepository cityRepository;

	@Override
	public Optional<Set<ClientStates>> getStatuses(Collection<String> roles) {
		return Optional.of(new HashSet<>(Arrays.asList(ClientStates.values())));
	}

	@Override
	public Optional<List<City>> getCities() {
		return Optional.of(cityRepository.findInSpecialOrderIsServiced());
	}
}
