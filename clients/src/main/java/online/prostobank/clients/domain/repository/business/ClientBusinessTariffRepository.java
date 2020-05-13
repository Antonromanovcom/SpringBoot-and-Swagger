package online.prostobank.clients.domain.repository.business;

import online.prostobank.clients.domain.business_service.ClientBusinessTariffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientBusinessTariffRepository extends JpaRepository<ClientBusinessTariffEntity, Long> {

    @Query(value = "select e from ClientBusinessTariffEntity e where e.clientServiceAvailable.clientId = ?1 ")
    Optional<List<ClientBusinessTariffEntity>> getClientListTariff(Long clientId);

    @Query(value = "select e from ClientBusinessTariffEntity e where e.clientServiceAvailable.clientId = ?2 and e.clientServiceAvailable.businessService.id = ?1")
    Optional<List<ClientBusinessTariffEntity>> getClientListServiceTariff(Long serviceId, Long clientId);

    @Query(value = "select e from ClientBusinessTariffEntity e where e.clientServiceAvailable.clientId = ?1 and e.businessTariff.id = ?2")
    Optional<List<ClientBusinessTariffEntity>> getClientTariffById(Long clientId, Long tariffId);


}
