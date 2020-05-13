package online.prostobank.clients.domain.messages;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageToClientRepository extends CrudRepository<MessageToClient, Long> {
	List<MessageToClient> findByClientId(Long clientId);
}
