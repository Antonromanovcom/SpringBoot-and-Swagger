package online.prostobank.clients.domain.repository.business;

import online.prostobank.clients.domain.business_service.ClientServiceAvailableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientBusinessServiceRepository extends JpaRepository<ClientServiceAvailableEntity, Long> {

    @Query(value = "select e from ClientServiceAvailableEntity e where e.clientId = ?1")
    Optional<List<ClientServiceAvailableEntity>> clientListServices(long clientId);

    @Query(value = "select e from ClientServiceAvailableEntity e where e.clientId = ?1 and e.available = true")
    Optional<List<ClientServiceAvailableEntity>> clientListActiveServices(long clientId);

    @Query(value = "select e from ClientServiceAvailableEntity e where e.clientId = ?1 and e.businessService.id = ?2")
    Optional<ClientServiceAvailableEntity> getClientService(long clientId, long businessId);
}
