package online.prostobank.clients.domain.repository.business;

import online.prostobank.clients.domain.business_service.BusinessTariffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessTariffRepository extends JpaRepository<BusinessTariffEntity, Long> {

    @Query(value = "select e from BusinessTariffEntity e where e.businessService.id = ?1")
    Optional<List<BusinessTariffEntity>> allTariffByService(Long serviceId);

}
