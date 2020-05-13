package online.prostobank.clients.domain.client;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientKeycloakRepository extends CrudRepository<ClientKeycloak, Long> {
	Optional<ClientKeycloak> findByLogin(String login);

	Optional<ClientKeycloak> findByKeycloakId(UUID keycloakId);
}
